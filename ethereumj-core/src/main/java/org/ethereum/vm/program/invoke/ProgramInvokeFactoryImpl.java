package org.ethereum.vm.program.invoke;

import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * @author Roman Mandeleil
 * @since 08.06.2014
 */
@Component("ProgramInvokeFactory")
public class ProgramInvokeFactoryImpl implements ProgramInvokeFactory {

    private static final Logger logger = LoggerFactory.getLogger("VM");

    @Autowired
    private Blockchain blockchain;


    // Invocation by the wire tx
    @Override
    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository,
                                             BlockStore blockStore) {

        // https://ethereum.etherpad.mozilla.org/26
        Block lastBlock = blockchain.getBestBlock();

        /***         ADDRESS op       ***/
        // YP: Get address of currently executing account.
        byte[] address = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();

        /***         ORIGIN op       ***/
        // YP: This is the sender of original transaction; it is never a contract.
        byte[] origin = tx.getSender();

        /***         CALLER op       ***/
        // YP: This is the address of the account that is directly responsible for this execution.
        byte[] caller = tx.getSender();

        /***         BALANCE op       ***/
        byte[] balance = repository.getBalance(address).toByteArray();

        /***         GASPRICE op       ***/
        byte[] gasPrice = tx.getGasPrice();

        /*** GAS op ***/
        byte[] gas = tx.getGasLimit();

        /***        CALLVALUE op      ***/
        byte[] callValue = nullToEmpty(tx.getValue());

        /***     CALLDATALOAD  op   ***/
        /***     CALLDATACOPY  op   ***/
        /***     CALLDATASIZE  op   ***/
        byte[] data = tx.isContractCreation() ? ByteUtil.EMPTY_BYTE_ARRAY : nullToEmpty(tx.getData());

        /***    PREVHASH  op  ***/
        byte[] lastHash = lastBlock.getHash();

        /***   COINBASE  op ***/
        byte[] coinbase = block.getCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = block.getTimestamp();

        /*** NUMBER  op  ***/
        long number = block.getNumber();

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = block.getDifficulty();

        /*** GASLIMIT op ***/
        long gaslimit = block.getGasLimit();

        if (logger.isInfoEnabled()) {
            logger.info("Top level call: \n" +
                            "address={}\n" +
                            "origin={}\n" +
                            "caller={}\n" +
                            "balance={}\n" +
                            "gasPrice={}\n" +
                            "gas={}\n" +
                            "callValue={}\n" +
                            "data={}\n" +
                            "lastHash={}\n" +
                            "coinbase={}\n" +
                            "timestamp={}\n" +
                            "blockNumber={}\n" +
                            "difficulty={}\n" +
                            "gaslimit={}\n",

                    Hex.toHexString(address),
                    Hex.toHexString(origin),
                    Hex.toHexString(caller),
                    new BigInteger(balance).longValue(),
                    new BigInteger(gasPrice).longValue(),
                    new BigInteger(gas).longValue(),
                    new BigInteger(callValue).longValue(),
                    Hex.toHexString(data),
                    Hex.toHexString(lastHash),
                    Hex.toHexString(coinbase),
                    timestamp,
                    number,
                    Hex.toHexString(difficulty),
                    gaslimit);
        }

        return new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue, data,
                lastHash, coinbase, timestamp, number, difficulty, gaslimit,
                repository, blockStore);
    }

    /**
     * This invocation created for contract call contract
     */
    @Override
    public ProgramInvoke createProgramInvoke(Program program, DataWord toAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore, boolean byTestingSuite) {

        DataWord address = toAddress;
        DataWord origin = program.getOriginAddress();
        DataWord caller = program.getOwnerAddress();

        DataWord balance = new DataWord(balanceInt.toByteArray());
        DataWord gasPrice = program.getGasPrice();
        DataWord gas = inGas;
        DataWord callValue = inValue;

        byte[] data = dataIn;
        DataWord lastHash = program.getPrevHash();
        DataWord coinbase = program.getCoinbase();
        DataWord timestamp = program.getTimestamp();
        DataWord number = program.getNumber();
        DataWord difficulty = program.getDifficulty();
        DataWord gasLimit = program.getGasLimit();

        if (logger.isInfoEnabled()) {
            logger.info("Internal call: \n" +
                            "address={}\n" +
                            "origin={}\n" +
                            "caller={}\n" +
                            "balance={}\n" +
                            "gasPrice={}\n" +
                            "gas={}\n" +
                            "callValue={}\n" +
                            "data={}\n" +
                            "lastHash={}\n" +
                            "coinbase={}\n" +
                            "timestamp={}\n" +
                            "blockNumber={}\n" +
                            "difficulty={}\n" +
                            "gaslimit={}\n",
                    Hex.toHexString(address.getLast20Bytes()),
                    Hex.toHexString(origin.getLast20Bytes()),
                    Hex.toHexString(caller.getLast20Bytes()),
                    balance.toString(),
                    gasPrice.longValue(),
                    gas.longValue(),
                    Hex.toHexString(callValue.getNoLeadZeroesData()),
                    data == null ? "" : Hex.toHexString(data),
                    Hex.toHexString(lastHash.getData()),
                    Hex.toHexString(coinbase.getLast20Bytes()),
                    timestamp.longValue(),
                    number.longValue(),
                    Hex.toHexString(difficulty.getNoLeadZeroesData()),
                    gasLimit.bigIntValue());
        }

        return new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue,
                data, lastHash, coinbase, timestamp, number, difficulty, gasLimit,
                repository, program.getCallDeep() + 1, blockStore, byTestingSuite);
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }
}
