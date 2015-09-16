package org.ethereum.core;

import org.ethereum.db.BlockStore;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.*;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.util.BIUtil.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.toHexString;
import static org.ethereum.vm.VMUtils.saveProgramTraceFile;
import static org.ethereum.vm.VMUtils.zipAndEncode;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger("execute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    private Transaction tx;
    private Repository track;
    private Repository cacheTrack;
    private BlockStore blockStore;
    private final long gasUsedInTheBlock;
    private boolean readyToExecute = false;

    private ProgramInvokeFactory programInvokeFactory;
    private byte[] coinbase;

    private TransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    private Block currentBlock;

    private final EthereumListener listener;

    private VM vm;
    private Program program;

    PrecompiledContracts.PrecompiledContract precompiledContract;

    long m_endGas = 0;
    long basicTxCost = 0;
    List<LogInfo> logs = null;

    boolean localCall = false;

    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                               ProgramInvokeFactory programInvokeFactory, Block currentBlock) {

        this(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock, new EthereumListenerAdapter(), 0);
    }

    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                               ProgramInvokeFactory programInvokeFactory, Block currentBlock,
                               EthereumListener listener, long gasUsedInTheBlock) {

        this.tx = tx;
        this.coinbase = coinbase;
        this.track = track;
        this.cacheTrack = track.startTracking();
        this.blockStore = blockStore;
        this.programInvokeFactory = programInvokeFactory;
        this.currentBlock = currentBlock;
        this.listener = listener;
        this.gasUsedInTheBlock = gasUsedInTheBlock;
    }


    /**
     * Do all the basic validation, if the executor
     * will be ready to run the transaction at the end
     * set readyToExecute = true
     */
    public void init() {

        if (localCall) {
            readyToExecute = true;
            return;
        }

        long txGasLimit = toBI(tx.getGasLimit()).longValue();

        boolean cumulativeGasReached = (gasUsedInTheBlock + txGasLimit > currentBlock.getGasLimit());
        if (cumulativeGasReached) {

            if (logger.isWarnEnabled())
                logger.warn("Too much gas used in this block: Require: {} Got: {}", currentBlock.getGasLimit() - toBI(tx.getGasLimit()).longValue(), toBI(tx.getGasLimit()).longValue());

            // TODO: save reason for failure
            return;
        }

        basicTxCost = tx.transactionCost();
        if (basicTxCost > txGasLimit) {

            if (logger.isWarnEnabled())
                logger.warn("Not enough gas for transaction execution: Require: {} Got: {}", basicTxCost, txGasLimit);


            // TODO: save reason for failure
            return;
        }

        BigInteger reqNonce = track.getNonce(tx.getSender());
        BigInteger txNonce = toBI(tx.getNonce());
        if (isNotEqual(reqNonce, txNonce)) {

            if (logger.isWarnEnabled())
                logger.warn("Invalid nonce: required: {} , tx.nonce: {}", reqNonce, txNonce);


            // TODO: save reason for failure
            return;
        }

        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(toBI(txGasLimit));
        BigInteger totalCost = toBI(tx.getValue()).add(txGasCost);
        BigInteger senderBalance = track.getBalance(tx.getSender());

        if (!isCovers(senderBalance, totalCost)) {

            if (logger.isWarnEnabled())
                logger.warn("Not enough cash: Require: {}, Sender cash: {}", totalCost, senderBalance);

            // TODO: save reason for failure
            return;
        }

        readyToExecute = true;
    }

    public void execute() {

        if (!readyToExecute) return;

        if (!localCall) {
            track.increaseNonce(tx.getSender());

            long txGasLimit = toBI(tx.getGasLimit()).longValue();
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(toBI(txGasLimit));
            track.addBalance(tx.getSender(), txGasCost.negate());

            if (logger.isInfoEnabled())
                logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    private void call() {
        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();
        precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(targetAddress));

        if (precompiledContract != null) {

            long requiredGas = precompiledContract.getGasForData(tx.getData());
            long txGasLimit = toBI(tx.getGasLimit()).longValue();

            if (!localCall && requiredGas > txGasLimit) {
                // no refund
                // no endowment
                return;
            } else {

                m_endGas = txGasLimit - requiredGas - basicTxCost;
//                BigInteger refundCost = toBI(m_endGas * toBI( tx.getGasPrice() ).longValue() );
//                track.addBalance(tx.getSender(), refundCost);

                // FIXME: save return for vm trace
                byte[] out = precompiledContract.execute(tx.getData());
            }

        } else {

            byte[] code = track.getCode(targetAddress);
            if (isEmpty(code)) {
                m_endGas = toBI(tx.getGasLimit()).longValue() - basicTxCost;
            } else {
                ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

                this.vm = new VM();
                this.program = new Program(code, programInvoke, tx);
            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), targetAddress, endowment);
    }

    private void create() {
        byte[] newContractAddress = tx.getContractAddress();
        if (isEmpty(tx.getData())) {
            m_endGas = toBI(tx.getGasLimit()).longValue() - basicTxCost;
            cacheTrack.createAccount(tx.getContractAddress());
        } else {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

            this.vm = new VM();
            this.program = new Program(tx.getData(), programInvoke, tx);
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), newContractAddress, endowment);
    }

    public void go() {
        if (!readyToExecute) return;

        // TODO: transaction call for pre-compiled  contracts
        if (vm == null) return;

        try {

            // Charge basic cost of the transaction
            program.spendGas(tx.transactionCost(), "TRANSACTION COST");

            if (CONFIG.playVM())
                vm.play(program);

            result = program.getResult();
            m_endGas = toBI(tx.getGasLimit()).subtract(toBI(result.getGasUsed())).longValue();

            if (tx.isContractCreation()) {

                int returnDataGasValue = getLength(result.getHReturn()) * GasCost.CREATE_DATA;
                if (returnDataGasValue <= m_endGas) {

                    m_endGas -= BigInteger.valueOf(returnDataGasValue).longValue();
                    cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());
                } else {
                    result.setHReturn(EMPTY_BYTE_ARRAY);
                }
            }

            if (result.getException() != null) {
                result.getDeleteAccounts().clear();
                result.getLogInfoList().clear();
                result.resetFutureRefund();

                throw result.getException();
            }

        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            cacheTrack.rollback();
            m_endGas = 0;
        }
    }

    public void finalization() {
        if (!readyToExecute) return;

        cacheTrack.commit();

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(toBI(m_endGas));

        if (result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * GasCost.SUICIDE_REFUND);
            long gasRefund = Math.min(result.getFutureRefund(), result.getGasUsed() / 2);
            byte[] addr = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();
            m_endGas += gasRefund;

            summaryBuilder
                    .gasUsed(toBI(result.getGasUsed()))
                    .gasRefund(toBI(gasRefund))
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions())
                    .storageDiff(track.getContractDetails(addr).getStorage());

            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();

        // Refund for gas leftover
        track.addBalance(tx.getSender(), summary.getLeftover().add(summary.getRefund()));
        logger.info("Pay total refund to sender: [{}], refund val: [{}]", Hex.toHexString(tx.getSender()), summary.getRefund());

        // Transfer fees to miner
        track.addBalance(coinbase, summary.getFee());
        logger.info("Pay fees to miner: [{}], feesEarned: [{}]", Hex.toHexString(coinbase), summary.getFee());

        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }
        }


        listener.onTransactionExecuted(summary);

        if (CONFIG.vmTrace() && program != null && result != null) {
            String trace = program.getTrace()
                    .result(result.getHReturn())
                    .error(result.getException())
                    .toString();


            if (CONFIG.vmTraceCompressed()) {
                trace = zipAndEncode(trace);
            }

            String txHash = toHexString(tx.getHash());
            saveProgramTraceFile(txHash, trace);
            listener.onVMTraceCreated(txHash, trace);
        }
    }

    public TransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public List<LogInfo> getVMLogs() {
        return logs;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        return toBI(tx.getGasLimit()).longValue() - m_endGas;
    }

}
