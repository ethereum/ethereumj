/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.jsontestsuite.suite;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.BlockStore;
import org.ethereum.core.Repository;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeImpl;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public class TestProgramInvokeFactory implements ProgramInvokeFactory {

    private final Env env;

    public TestProgramInvokeFactory(Env env) {
        this.env = env;
    }


    @Override
    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository, BlockStore blockStore) {
        return generalInvoke(tx, repository, blockStore);
    }

    @Override
    public ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore, boolean byTestingSuite) {
        return null;
    }


    private ProgramInvoke generalInvoke(Transaction tx, Repository repository, BlockStore blockStore) {

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
        byte[] data = tx.isContractCreation() ? ByteUtil.EMPTY_BYTE_ARRAY :( tx.getData() == null ? ByteUtil.EMPTY_BYTE_ARRAY : tx.getData() );
//        byte[] data =  tx.getData() == null ? ByteUtil.EMPTY_BYTE_ARRAY : tx.getData() ;

        /***    PREVHASH  op  ***/
        byte[] lastHash = env.getPreviousHash();

        /***   COINBASE  op ***/
        byte[] coinbase = env.getCurrentCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = ByteUtil.byteArrayToLong(env.getCurrentTimestamp());

        /*** NUMBER  op  ***/
        long number = ByteUtil.byteArrayToLong(env.getCurrentNumber());

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = env.getCurrentDifficulty();

        /*** GASLIMIT op ***/
        byte[] gaslimit = env.getCurrentGasLimit();

        return new ProgramInvokeImpl(address, origin, caller, balance,
                gasPrice, gas, callValue, data, lastHash, coinbase,
                timestamp, number, difficulty, gaslimit, repository, blockStore);
    }

}
