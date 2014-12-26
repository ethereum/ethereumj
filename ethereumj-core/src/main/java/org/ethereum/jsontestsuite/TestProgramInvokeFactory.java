package org.ethereum.jsontestsuite;

import org.ethereum.core.*;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Repository;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.*;

import java.math.BigInteger;

/**
 * www.ethergit.com
 *
 * @author Roman Mandeleil
 * @since 19.12.2014
 */

public class TestProgramInvokeFactory implements ProgramInvokeFactory {

    Env env;

    TestProgramInvokeFactory(Env env) {
        this.env = env;
    }


    @Override
    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository) {
        return generalInvoke(tx, repository);
    }

    @Override
    public ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn, Repository repository) {
        return null;
    }


    private ProgramInvoke generalInvoke(Transaction tx, Repository repository) {

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
        byte[] callValue = tx.getValue() == null ? new byte[]{0} : tx.getValue();

        /***     CALLDATALOAD  op   ***/
        /***     CALLDATACOPY  op   ***/
        /***     CALLDATASIZE  op   ***/
        byte[] data = tx.getData() == null ? ByteUtil.EMPTY_BYTE_ARRAY : tx.getData();

        /***    PREVHASH  op  ***/
        byte[] lastHash = env.getPreviousHash();

        /***   COINBASE  op ***/
        byte[] coinbase = env.getCurrentCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = ByteUtil.byteArrayToLong(env.getCurrentTimestamp());

        /*** NUMBER  op  ***/
        long number = ByteUtil.byteArrayToLong(env.getCurrentNumber());

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = env.getCurrentDifficlty();

        /*** GASLIMIT op ***/
        long gaslimit = ByteUtil.byteArrayToLong(env.getCurrentGasLimit());

        return new ProgramInvokeImpl(address, origin, caller, balance,
                gasPrice, gas, callValue, data, lastHash, coinbase,
                timestamp, number, difficulty, gaslimit, repository);
    }

}
