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

    /*
    @Deprecated
    public void execute() {


        final String txHash = Hex.toHexString(tx.getHash());
        logger.info("applyTransaction: [{}]", txHash);

        TransactionReceipt receipt = new TransactionReceipt();

        // VALIDATE THE SENDER
        byte[] senderAddress = tx.getSender();
//      AccountState senderAccount = repository.getAccountState(senderAddress);
        logger.info("tx.sender: [{}]", Hex.toHexString(tx.getSender()));


        // GET TOTAL ETHER VALUE AVAILABLE FOR TX FEE
        BigInteger gasPrice = toBI(tx.getGasPrice());
        BigInteger gasDebit = toBI(tx.getGasLimit()).multiply(gasPrice);
        logger.info("Gas price limited to [{} units]", gasDebit.toString());

        // Debit the actual total gas value from the sender
        // the purchased gas will be available for
        // the contract in the execution state,
        // it can be retrieved using GAS op
        BigInteger txValue = toBI(tx.getValue());
        if (track.getBalance(senderAddress).compareTo(gasDebit.add(txValue)) == -1) {
            logger.debug("No gas to start the execution: sender={}",
                    Hex.toHexString(senderAddress));

            receipt.setCumulativeGas(0);
            this.receipt = receipt;
            return;
        }

        // FIND OUT THE TRANSACTION TYPE
        final byte[] receiverAddress;
        final byte[] code;
        boolean isContractCreation = tx.isContractCreation();
        if (isContractCreation) {
            receiverAddress = tx.getContractAddress();
            code = tx.getData(); // init code
        } else {
            receiverAddress = tx.getReceiveAddress();
            code = track.getCode(receiverAddress);

            if (code != EMPTY_BYTE_ARRAY) {
                if (stateLogger.isDebugEnabled())
                    stateLogger.debug("calling for existing contract: address={}",
                            Hex.toHexString(receiverAddress));
            }
        }

        // THE SIMPLE VALUE/BALANCE CHANGE
        if (!(isContractCreation || code != EMPTY_BYTE_ARRAY)) // if code invoke transfer will be done latter
            // for rollback purposes
            if (isCovers(track.getBalance(senderAddress), txValue)) {

                transfer(track, senderAddress, receiverAddress, txValue);

                if (stateLogger.isDebugEnabled())
                    stateLogger.debug("Update value balance \n "
                                    + "sender={}, receiver={}, value={}",
                            Hex.toHexString(senderAddress),
                            Hex.toHexString(receiverAddress),
                            new BigInteger(tx.getValue()));
            }


        // UPDATE THE NONCE
        track.increaseNonce(senderAddress);
        logger.info("increased nonce to: [{}], addr: [{}]",
                track.getNonce(senderAddress), Hex.toHexString(senderAddress));

        // CHARGE FOR GAS
        track.addBalance(senderAddress, gasDebit.negate());

        // The coinbase get the gas cost
        if (coinbase != null)
            track.addBalance(coinbase, gasDebit);

        if (stateLogger.isDebugEnabled())
            stateLogger.debug(
                    "Before contract execution debit the sender address with gas total cost, "
                            + "\n sender={} \n gas_debit= {}",
                    Hex.toHexString(senderAddress), gasDebit);

        //Check: Do not execute if transaction has debit amount of 0 and there is code
        if (isZero(gasDebit) && tx.getData() != null) {
            logger.debug("Transaction gas debits are zero! Cannot execute any code: sender={}",
                    Hex.toHexString(senderAddress));

            receipt.setCumulativeGas(0);
            this.receipt = receipt;
            return;
        }

        // CREATE AND/OR EXECUTE CONTRACT
        long gasUsed = 0;
        if (isContractCreation || code != EMPTY_BYTE_ARRAY) {

            // START TRACKING FOR REVERT CHANGES OPTION
            Program program = null;
            Repository trackTx = track.startTracking();
            trackTx.addBalance(receiverAddress, BigInteger.ZERO); // the contract created for anycase but SUICIDE call

            // THE SIMPLE VALUE/BALANCE CHANGE
            if (isCovers(trackTx.getBalance(senderAddress), txValue)) {

                transfer(trackTx, senderAddress, receiverAddress, txValue);

                if (stateLogger.isDebugEnabled())
                    stateLogger.debug("Update value balance \n "
                                    + "sender={}, receiver={}, value={}",
                            Hex.toHexString(senderAddress),
                            Hex.toHexString(receiverAddress),
                            new BigInteger(tx.getValue()));
            }

            logger.info("Start tracking VM run");
            try {

                // CREATE NEW CONTRACT ADDRESS AND ADD TX VALUE
                if (isContractCreation) {
                    if (stateLogger.isDebugEnabled())
                        stateLogger.debug("new contract created address={}",
                                Hex.toHexString(receiverAddress));
                }

                ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currentBlock, trackTx, blockStore);

                VM vm = new VM();
                program = new Program(code, programInvoke);

                if (CONFIG.playVM())
                    vm.play(program);

                result = program.getResult();
                gasUsed = applyProgramResult(result, gasDebit, gasPrice, trackTx,
                        senderAddress, receiverAddress, coinbase, isContractCreation);

                postExecute(gasUsed);

                List<LogInfo> logs = result.getLogInfoList();
                receipt.setLogInfoList(logs);

            } catch (RuntimeException e) {
                trackTx.rollback();
                receipt.setCumulativeGas(tx.getGasLimit());
                this.receipt = receipt;
                return;
            } finally {
                if (CONFIG.vmTrace()) {
                    String trace = (program == null) ? "{}" : program.getProgramTrace()
                            .result(result.getHReturn())
                            .error(result.getException())
                            .toString();


                    if (CONFIG.vmTraceCompressed()) {
                        trace = zipAndEncode(trace);
                    }

                    saveProgramTraceFile(txHash, trace);

                    listener.onVMTraceCreated(txHash, trace);
                }
            }
            trackTx.commit();
        } else {

            // REFUND GAS_DEBIT EXCEPT FOR FEE (500 + 5*TX_NO_ZERO_DATA)
            long dataCost = tx.getData() == null ? 0 :
                    tx.nonZeroDataBytes() * GasCost.TX_NO_ZERO_DATA +
                            tx.zeroDataBytes() * GasCost.TX_ZERO_DATA;
            gasUsed = GasCost.TRANSACTION + dataCost;

            BigInteger refund = gasDebit.subtract(toBI(gasUsed).multiply(gasPrice));
            if (isPositive(refund))
                transfer(track, coinbase, senderAddress, refund);

        }

        receipt.setCumulativeGas(gasUsed);
        this.receipt = receipt;
    }

    private void postExecute(long gasUsed) {
        BigInteger gasPrice = toBI(tx.getGasPrice());
        BigInteger feesEarned = toBI(gasUsed).multiply(gasPrice);
        transfer(track, tx.getSender(), currentBlock.getCoinbase(), feesEarned);
    }

    */
/**
     * After any contract code finish the run the certain result should take place,
     * according to the given circumstances.
     *//*

    private long applyProgramResult(ProgramResult result, BigInteger gasDebit,
                                    BigInteger gasPrice, Repository repository, byte[] senderAddress,
                                    byte[] contractAddress, byte[] coinbase, boolean initResults) {

        long gasUsed = result.getGasUsed();

        if (result.getException() != null) {
            stateLogger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                    Hex.toHexString(contractAddress),
                    result.getException());
            throw result.getException();
        }

        BigInteger refund = gasDebit.subtract(toBI(result.getGasUsed()).multiply(gasPrice));

        // accumulate refunds for suicides
        result.futureRefundGas(
                GasCost.SUICIDE_REFUND * (result.getDeleteAccounts() == null ? 0 : result.getDeleteAccounts().size()));

        if (isPositive(refund)) {
            if (stateLogger.isDebugEnabled())
                stateLogger
                        .debug("After contract execution the sender address refunded with gas leftover, "
                                        + "\n sender={} \n contract={}  \n gas_refund= {}",
                                Hex.toHexString(senderAddress),
                                Hex.toHexString(contractAddress), refund);
            // gas refund
            transfer(repository, coinbase, senderAddress, refund);
        }

        if (result.getFutureRefund() > 0) {

            //TODO #POC9 add getGasFree() as method to ProgramResult?
            BigInteger gasFree = gasDebit.subtract(toBI(result.getGasUsed()));

            long futureRefund = Math.min(result.getFutureRefund(), gasDebit.subtract(gasFree).longValue() / 2);
            BigInteger futureRefundBI = toBI(futureRefund);
            BigInteger futureRefundVal = futureRefundBI.multiply(gasPrice);

            if (stateLogger.isDebugEnabled())
                stateLogger
                        .debug("After contract execution the sender address refunded with storage save refunds, "
                                        + "\n sender={} \n contract={}  \n gas_refund= {}",
                                Hex.toHexString(senderAddress),
                                Hex.toHexString(contractAddress), futureRefundVal);

            transfer(repository, coinbase, senderAddress, futureRefundVal);
        }

        if (initResults) {
            // Save the code created by init
            byte[] bodyCode = null;
            if (result.getHReturn() != null && result.getHReturn().length > 0) {
                bodyCode = result.getHReturn();
            }

            if (bodyCode != null) {
                if (stateLogger.isDebugEnabled())
                    stateLogger
                            .debug("saving code of the contract to the db:\n contract={} code={}",
                                    Hex.toHexString(contractAddress),
                                    Hex.toHexString(bodyCode));

                BigInteger returnDataGasValue = BigInteger.valueOf(bodyCode.length * GasCost.CREATE_DATA);
                gasUsed += returnDataGasValue.longValue();

                BigInteger storageCost = gasPrice.multiply(returnDataGasValue);
                BigInteger balance = repository.getBalance(senderAddress);

                // check if can be charged for the contract data save
                if (isCovers(balance, storageCost))
                    transfer(repository, senderAddress, coinbase, storageCost);
                else
                    bodyCode = EMPTY_BYTE_ARRAY;

                repository.saveCode(contractAddress, bodyCode);
            }
        }

        // delete the marked to die accounts
        if (result.getDeleteAccounts() != null)
            for (DataWord address : result.getDeleteAccounts()) {
                repository.delete(address.getLast20Bytes());
            }

        return gasUsed;
    }
*/


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
