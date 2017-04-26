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
package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.vm.DataWord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by Anton Nashatyrev on 08.04.2016.
 */
public class TransactionStoreTest {

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

    @Test
    public void simpleTest() {
        String contractSrc =
                "contract Adder {" +
                "  function add(int a, int b) returns (int) {return a + b;}" +
                "}";
        HashMapDB<byte[]> txDb = new HashMapDB<>();

        StandaloneBlockchain bc = new StandaloneBlockchain();
        bc.getBlockchain().withTransactionStore(new TransactionStore(txDb));
        SolidityContract contract = bc.submitNewContract(contractSrc);
        bc.createBlock();
        contract.callFunction("add", 555, 222);
        Block b2 = bc.createBlock();
        contract.callFunction("add", 333, 333);
        Block b3 = bc.createBlock();
        Transaction tx1 = b2.getTransactionsList().get(0);
        TransactionInfo tx1Info = bc.getBlockchain().getTransactionInfo(tx1.getHash());
        byte[] executionResult = tx1Info.getReceipt().getExecutionResult();
        Assert.assertArrayEquals(new DataWord(777).getData(), executionResult);

        System.out.println(txDb.keys().size());
        bc.getBlockchain().flush();
        System.out.println(txDb.keys().size());

        TransactionStore txStore = new TransactionStore(txDb);
        TransactionInfo tx1Info_ = txStore.get(tx1.getHash()).get(0);
        executionResult = tx1Info_.getReceipt().getExecutionResult();
        Assert.assertArrayEquals(new DataWord(777).getData(), executionResult);

        TransactionInfo highIndex = new TransactionInfo(tx1Info.getReceipt(), tx1Info.getBlockHash(), 255);
        TransactionInfo highIndexCopy = new TransactionInfo(highIndex.getEncoded());
        Assert.assertArrayEquals(highIndex.getBlockHash(), highIndexCopy.getBlockHash());
        Assert.assertEquals(highIndex.getIndex(), highIndexCopy.getIndex());
    }

    @Test
    public void forkTest() {
        // check that TransactionInfo is always returned from the main chain for
        // transaction which included into blocks from different forks

        String contractSrc =
                "contract Adder {" +
                "  int public lastResult;" +
                "  function add(int a, int b) returns (int) {lastResult = a + b; return lastResult; }" +
                "}";
        HashMapDB txDb = new HashMapDB();

        StandaloneBlockchain bc = new StandaloneBlockchain();
        TransactionStore transactionStore = new TransactionStore(txDb);
        bc.getBlockchain().withTransactionStore(transactionStore);
        SolidityContract contract = bc.submitNewContract(contractSrc);
        Block b1 = bc.createBlock();
        contract.callFunction("add", 555, 222);
        Block b2 = bc.createBlock();
        Transaction tx1 = b2.getTransactionsList().get(0);
        TransactionInfo txInfo = bc.getBlockchain().getTransactionInfo(tx1.getHash());
        Assert.assertTrue(Arrays.equals(txInfo.getBlockHash(), b2.getHash()));

        Block b2_ = bc.createForkBlock(b1);
        contract.callFunction("add", 555, 222); // tx with the same hash as before
        Block b3_ = bc.createForkBlock(b2_);
        TransactionInfo txInfo_ = bc.getBlockchain().getTransactionInfo(tx1.getHash());
        Assert.assertTrue(Arrays.equals(txInfo_.getBlockHash(), b3_.getHash()));

        Block b3 = bc.createForkBlock(b2);
        Block b4 = bc.createForkBlock(b3);
        txInfo = bc.getBlockchain().getTransactionInfo(tx1.getHash());
        Assert.assertTrue(Arrays.equals(txInfo.getBlockHash(), b2.getHash()));
    }

    @Test
    public void backwardCompatibleDbTest() {
        // check that we can read previously saved entries (saved with legacy code)

        HashMapDB txDb = new HashMapDB();
        TransactionStore transactionStore = new TransactionStore(txDb);
        StandaloneBlockchain bc = new StandaloneBlockchain();
        bc.getBlockchain().withTransactionStore(transactionStore);

        bc.sendEther(new byte[20], BigInteger.valueOf(1000));
        Block b1 = bc.createBlock();
        Transaction tx = b1.getTransactionsList().get(0);
        TransactionInfo info = transactionStore.get(tx.getHash()).get(0);

        HashMapDB<byte[]> txDb1 = new HashMapDB<>();
        txDb1.put(tx.getHash(), info.getEncoded()); // legacy serialization
        TransactionStore transactionStore1 = new TransactionStore(txDb1);
        TransactionInfo info1 = transactionStore1.get(tx.getHash()).get(0);
        Assert.assertArrayEquals(info1.getReceipt().getPostTxState(), info.getReceipt().getPostTxState());
    }
}
