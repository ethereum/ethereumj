package org.ethereum.vm;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.ByteUtil;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 08/06/2014 09:59
 */

public class ProgramInvokeFactory {

    // Invocation by the other program
    public static ProgramInvoke createProgramInvoke(Program program){

        return null;
    }


        // Invocation by the wire tx
    public static ProgramInvoke createProgramInvoke(Transaction tx, Block lastBlock){

        // https://ethereum.etherpad.mozilla.org/26

        /***         ADDRESS op       ***/
        // YP: Get address of currently executing account.
        byte[] address  =  (tx.isContractCreation())? tx.getContractAddress(): tx.getReceiveAddress();

        /***         ORIGIN op       ***/
        // YP: This is the sender of original transaction; it is never a contract.
        byte[] origin  = tx.getSender();

        /***         CALLER op       ***/
        // YP: This is the address of the account that is directly responsible for this execution.
        byte[] caller = tx.getSender();

        /***         BALANCE op       ***/
        byte[] addressStateData = WorldManager.instance.worldState.get(address);

        byte[] balance = null;
        if (addressStateData.length == 0)
            balance = new byte[]{0};
        else
            balance = new AccountState(addressStateData).getBalance().toByteArray();


        /***         GASPRICE op       ***/
        byte[] gasPrice = tx.getGasPrice();

        /*** GAS op ***/
        byte[] gas = tx.getGasLimit();

        /***        CALLVALUE op      ***/
        byte[] callValue = tx.getValue();


        /***     CALLDATALOAD  op   ***/
        /***     CALLDATACOPY  op   ***/
        /***     CALLDATASIZE  op   ***/
        byte[] data = tx.getData();
        if (data == null) data = new byte[]{};

        /***    PREVHASH  op  ***/
        byte[] lastHash = lastBlock.getHash();

        /***   COINBASE  op ***/
        byte[] coinbase = lastBlock.getCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = lastBlock.getTimestamp();

        /*** NUMBER  op  ***/
        long number = lastBlock.getNumber();

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = lastBlock.getDifficulty();

        /*** GASLIMIT op ***/
        long gaslimit = lastBlock.getGasLimit();


        ProgramInvoke programInvoke =
            new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue, data,
                    lastHash,  coinbase,  timestamp,  number,  difficulty,  gaslimit);

        return programInvoke;
    }
}
