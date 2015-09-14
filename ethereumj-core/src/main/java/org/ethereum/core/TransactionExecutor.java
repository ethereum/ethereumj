package org.ethereum.core;

import org.ethereum.db.BlockStore;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

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

        basicTxCost = GasCost.TRANSACTION + (tx.getData() == null ? 0 :
                tx.nonZeroDataBytes() * GasCost.TX_NO_ZERO_DATA + tx.zeroDataBytes() * GasCost.TX_ZERO_DATA);

        boolean basicCostCovered = (txGasLimit >= basicTxCost);
        if (!basicCostCovered) {

            if (logger.isWarnEnabled())
                logger.warn("Too much gas used in this block: Require: {} Got: {}", txGasLimit, basicTxCost);


            // TODO: save reason for failure
            return;
        }

        BigInteger reqNonce = track.getNonce(tx.getSender());
        BigInteger txNonce = toBI(tx.getNonce());

        boolean validNonce = (txNonce.compareTo(reqNonce) == 0);
        if (!validNonce) {

            if (logger.isWarnEnabled())
                logger.warn("Invalid nonce: required: {} , tx.nonce: {}", reqNonce, txNonce);


            // TODO: save reason for failure
            return;
        }

        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(toBI(txGasLimit));
        BigInteger totalCost = toBI(tx.getValue()).add(txGasCost);
        BigInteger senderBalance = track.getBalance(tx.getSender());

        boolean canAfford = isCovers(senderBalance, totalCost);
        if (!canAfford) {

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

        if (tx.isContractCreation())
            create();
        else
            call();
    }

    private void call() {

        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();

        precompiledContract =
                PrecompiledContracts.getContractForAddress(new DataWord(targetAddress));

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
            if (code.length > 0) {
                ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

                this.vm = new VM();
                this.program = new Program(code, programInvoke);
            } else {

                m_endGas = toBI(tx.getGasLimit()).longValue() - basicTxCost;
            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), targetAddress, endowment);
    }

    private void create() {

        byte[] newContractAddress = tx.getContractAddress();
        if (tx.getData() != null && !(tx.getData().length == 0)) {

            ProgramInvoke programInvoke =
                    programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

            this.vm = new VM();
            this.program = new Program(tx.getData(), programInvoke);
        } else {
            m_endGas = toBI(tx.getGasLimit()).longValue() - basicTxCost;
            cacheTrack.createAccount(tx.getContractAddress());
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

                byte[] initCode = EMPTY_BYTE_ARRAY;
                if (result.getHReturn().length * GasCost.CREATE_DATA <= m_endGas) {

                    BigInteger returnDataGasValue = BigInteger.valueOf(result.getHReturn().length * GasCost.CREATE_DATA);
                    m_endGas -= returnDataGasValue.longValue();
                    initCode = result.getHReturn();
                    cacheTrack.saveCode(tx.getContractAddress(), initCode);
                } else {
                    result.setHReturn(initCode);
                }
            }

            if (result.getException() != null){
                if (result.getDeleteAccounts() != null) result.getDeleteAccounts().clear();
                if (result.getLogInfoList() != null) result.getLogInfoList().clear();
                result.futureRefundGas(0);
                throw result.getException();
            }

        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            cacheTrack.rollback();
            m_endGas = 0;
        } finally {
            if (CONFIG.vmTrace()) {
                String trace = (program == null) ? "{}" : program.getProgramTrace()
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
    }


    public void finalization() {
        if (!readyToExecute ) return;

        cacheTrack.commit();

        // Accumulate refunds for suicides
        if (result != null)
            result.futureRefundGas(
                    GasCost.SUICIDE_REFUND * (result.getDeleteAccounts() == null ? 0 : result.getDeleteAccounts().size()));


        // SSTORE refunds...
        // must be done before the miner gets the fees.
        long gasLimit = toBI(tx.getGasLimit()).longValue();

        if (result != null)
            m_endGas += Math.min(result.getFutureRefund(), result.getGasUsed() / 2);

        BigInteger endGasBI = toBI(m_endGas);
        BigInteger gasPrice = toBI(tx.getGasPrice());
        BigInteger futureRefundVal = endGasBI.multiply(gasPrice);

        // Refund for gas leftover
        track.addBalance(tx.getSender(), futureRefundVal);
        logger.info("Pay total refund to sender: [{}], refund val: [{}]", Hex.toHexString(tx.getSender()), futureRefundVal);

        // Transfer fees to miner
        BigInteger feesEarned = toBI(gasLimit).subtract(endGasBI).multiply(gasPrice);

        track.addBalance(coinbase, feesEarned);
        logger.info("Pay fees to miner: [{}], feesEarned: [{}]", Hex.toHexString(coinbase), feesEarned);

        // Traverse list of suicides
        if (result.getDeleteAccounts() != null)
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }

        if (result != null)
            logs = result.getLogInfoList();

        if (result.getLogInfoList() != null){

        }
    }

    public TransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


    public TransactionReceipt getReceipt() {
        return receipt;
    }
    public List<LogInfo> getVMLogs() { return logs;  }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        return toBI(tx.getGasLimit()).longValue() - m_endGas;
    }

}
