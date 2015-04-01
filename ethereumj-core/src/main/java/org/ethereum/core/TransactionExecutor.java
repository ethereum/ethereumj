package org.ethereum.core;

import org.ethereum.db.BlockStore;
import org.ethereum.facade.Repository;
import org.ethereum.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.util.BIUtil.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger("execute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    private Transaction tx;
    private Repository track;
    private BlockStore blockStore;

    private ProgramInvokeFactory programInvokeFactory;
    private byte[] coinbase;

    private TransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    private Block currentBlock;


    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                               ProgramInvokeFactory programInvokeFactory, Block currentBlock) {

        this.tx = tx;
        this.coinbase = coinbase;
        this.track = track;
        this.blockStore = blockStore;
        this.programInvokeFactory = programInvokeFactory;
        this.currentBlock = currentBlock;
    }

/* jeff:
    execution happens like this:
    create account, transfer value (if any), create a snapshot,
    run INIT code, if err, rollback to snapshot, if success set return value.
*/

//        https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L55


    public void execute() {


        logger.info("applyTransaction: [{}]", Hex.toHexString(tx.getHash()));

        TransactionReceipt receipt = new TransactionReceipt();

        // VALIDATE THE SENDER
        byte[] senderAddress = tx.getSender();
//      AccountState senderAccount = repository.getAccountState(senderAddress);
        logger.info("tx.sender: [{}]", Hex.toHexString(tx.getSender()));

        // VALIDATE THE NONCE
        BigInteger nonce = track.getNonce(senderAddress);
        BigInteger txNonce = new BigInteger(1, tx.getNonce());
        if (isNotEqual(nonce, txNonce)) {
            if (stateLogger.isWarnEnabled())
                stateLogger.warn("Invalid nonce account.nonce={} tx.nonce={}",
                        nonce, txNonce);

            receipt.setCumulativeGas(0);
            this.receipt = receipt;
            return;
        }

        //Insert gas cost protection
        BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());
        if (isZero(txGasLimit)) {  // isZero()
            logger.debug("No gas limit set on transaction: hash={}",
                    Hex.toHexString(tx.getHash()));

            receipt.setCumulativeGas(0);
            this.receipt = receipt;
            return;
        }

        //Check: gas limit is not lessthan total tx cost
        BigInteger txDataCost = new BigInteger(Long.toString(GasCost.TRANSACTION +
                GasCost.TX_NO_ZERO_DATA * tx.nonZeroDataBytes() +
                GasCost.TX_ZERO_DATA * tx.zeroDataBytes()), 10);
        if (isLessThan(txGasLimit, txDataCost)) {
            logger.debug("Not enough gas to pay for the transaction: hash: {}\n required: {}, found: {}",
                    Hex.toHexString(tx.getHash()), txDataCost, txGasLimit);

            receipt.setCumulativeGas(0);
            this.receipt = receipt;
            return;
        }

        BigInteger blockGasUsed = toBI(currentBlock.getGasUsed());
        BigInteger blockGasLimit = toBI(currentBlock.getGasLimit());
        if (isNotCovers(blockGasLimit, sum(blockGasUsed, txGasLimit))) {
            logger.debug("Too much gas used in this block: require={}", blockGasLimit.toString());
            receipt.setCumulativeGas(0);
            this.receipt = receipt;
            return;
        }

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
                Program program = new Program(code, programInvoke);

                if (CONFIG.playVM())
                    vm.play(program);

                program.saveProgramTraceToFile(Hex.toHexString(tx.getHash()));
                result = program.getResult();
                applyProgramResult(result, gasDebit, gasPrice, trackTx,
                        senderAddress, receiverAddress, coinbase, isContractCreation);

                List<LogInfo> logs = result.getLogInfoList();
                receipt.setLogInfoList(logs);

            } catch (RuntimeException e) {
                trackTx.rollback();
                receipt.setCumulativeGas(tx.getGasLimit());
                this.receipt = receipt;
                return;
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

    /**
     * After any contract code finish the run the certain result should take place,
     * according to the given circumstances.
     */
    private void applyProgramResult(ProgramResult result, BigInteger gasDebit,
                                    BigInteger gasPrice, Repository repository, byte[] senderAddress,
                                    byte[] contractAddress, byte[] coinbase, boolean initResults) {

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

                BigInteger storageCost = gasPrice.multiply(BigInteger.valueOf(bodyCode.length * GasCost
                        .CREATE_DATA));
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
        if (result.getDeleteAccounts() == null) return;
        for (DataWord address : result.getDeleteAccounts()) {
            repository.delete(address.getLast20Bytes());
        }

    }


    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public ProgramResult getResult() {
        return result;
    }
}
