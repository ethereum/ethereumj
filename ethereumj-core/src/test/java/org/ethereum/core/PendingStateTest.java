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
package org.ethereum.core;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.*;
import static org.ethereum.util.blockchain.EtherUtil.Unit.ETHER;
import static org.ethereum.util.blockchain.EtherUtil.convert;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2015
 */
public class PendingStateTest {

    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

    static class PendingListener extends EthereumListenerAdapter {
        public BlockingQueue<Pair<Block, List<TransactionReceipt>>> onBlock = new LinkedBlockingQueue<>();
        public BlockingQueue<Object> onPendingStateChanged = new LinkedBlockingQueue<>();
//        public BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>> onPendingTransactionUpdate = new LinkedBlockingQueue<>();

        Map<ByteArrayWrapper, BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>>>
                onPendingTransactionUpdate = new HashMap<>();

        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            System.out.println("PendingStateTest.onBlock:" + "block = [" + block.getShortDescr() + "]");
            onBlock.add(Pair.of(block, receipts));
        }

        @Override
        public void onPendingStateChanged(PendingState pendingState) {
            System.out.println("PendingStateTest.onPendingStateChanged.");
            onPendingStateChanged.add(new Object());
        }

        @Override
        public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
            System.out.println("PendingStateTest.onPendingTransactionUpdate:" + "txReceipt.err = [" + txReceipt.getError() + "], state = [" + state + "], block: " + block.getShortDescr());
            getQueueFor(txReceipt.getTransaction()).add(Triple.of(txReceipt, state, block));
        }

        public synchronized BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>> getQueueFor(Transaction tx) {
            ByteArrayWrapper hashW = new ByteArrayWrapper(tx.getHash());
            BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>> queue = onPendingTransactionUpdate.get(hashW);
            if (queue == null) {
                queue = new LinkedBlockingQueue<>();
                onPendingTransactionUpdate.put(hashW, queue);
            }
            return queue;
        }

        public PendingTransactionState pollTxUpdateState(Transaction tx) throws InterruptedException {
            return getQueueFor(tx).poll(5, SECONDS).getMiddle();
        }
        public Triple<TransactionReceipt, PendingTransactionState, Block> pollTxUpdate(Transaction tx) throws InterruptedException {
            return getQueueFor(tx).poll(5, SECONDS);
        }
    }

    @Test
    public void testSimple() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();

        bc.sendEther(new byte[20], BigInteger.valueOf(100000));
        bc.sendEther(new byte[20], BigInteger.valueOf(100000));

        bc.createBlock();
        l.onBlock.poll(5, SECONDS);

        Transaction tx1 = bc.createTransaction(100, new byte[32], 1000, new byte[0]);
        pendingState.addPendingTransaction(tx1);
        // dropped due to large nonce
        Assert.assertEquals(l.pollTxUpdateState(tx1), DROPPED);

        Transaction tx1_ = bc.createTransaction(0, new byte[32], 1000, new byte[0]);
        pendingState.addPendingTransaction(tx1_);
        // dropped due to low nonce
        Assert.assertEquals(l.pollTxUpdateState(tx1_), DROPPED);

        Transaction tx2 = bc.createTransaction(2, alice.getAddress(), 1000000, new byte[0]);
        Transaction tx3 = bc.createTransaction(3, alice.getAddress(), 1000000, new byte[0]);
        pendingState.addPendingTransaction(tx2);
        pendingState.addPendingTransaction(tx3);

        txUpd = l.pollTxUpdate(tx2);
        Assert.assertEquals(txUpd.getMiddle(), NEW_PENDING);
        Assert.assertTrue(txUpd.getLeft().isValid());
        txUpd = l.pollTxUpdate(tx3);
        Assert.assertEquals(txUpd.getMiddle(), NEW_PENDING);
        Assert.assertTrue(txUpd.getLeft().isValid());
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000 - 100000)) > 0);

        pendingState.addPendingTransaction(tx2);  // double transaction submit
        Assert.assertTrue(l.getQueueFor(tx2).isEmpty());

        bc.createBlock();

        Assert.assertEquals(l.pollTxUpdateState(tx2), PENDING);
        Assert.assertEquals(l.pollTxUpdateState(tx3), PENDING);

        bc.submitTransaction(tx2);
        Block b3 = bc.createBlock();

        txUpd = l.pollTxUpdate(tx2);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertEquals(txUpd.getRight(), b3);
        Assert.assertEquals(l.pollTxUpdateState(tx3), PENDING);

        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000 - 100000)) > 0);

        for (int i = 0; i < SystemProperties.getDefault().txOutdatedThreshold() + 1; i++) {
            bc.createBlock();
            txUpd = l.pollTxUpdate(tx3);
            if (txUpd.getMiddle() != PENDING) break;
        }

        // tx3 dropped due to timeout
        Assert.assertEquals(txUpd.getMiddle(), DROPPED);
        Assert.assertEquals(txUpd.getLeft().getTransaction(), tx3);
        Assert.assertFalse(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000 - 100000)) > 0);
    }

    @Test
    public void testRebranch1() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();
        ECKey charlie = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));
        bc.sendEther(charlie.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        pendingState.addPendingTransaction(tx1);
        Transaction tx2 = bc.createTransaction(charlie, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);;
        pendingState.addPendingTransaction(tx2);

        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);
        Assert.assertEquals(l.pollTxUpdateState(tx2), NEW_PENDING);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000)) == 0);

        bc.submitTransaction(tx1);
        Block b2 = bc.createBlock();

        Assert.assertEquals(l.pollTxUpdateState(tx1), INCLUDED);
        Assert.assertEquals(l.pollTxUpdateState(tx2), PENDING);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000)) == 0);

        bc.submitTransaction(tx2);
        Block b3 = bc.createBlock();

        Assert.assertEquals(l.pollTxUpdateState(tx2), INCLUDED);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000)) == 0);

        Block b2_ = bc.createForkBlock(b1);
        Block b3_ = bc.createForkBlock(b2_);

        bc.submitTransaction(tx2);
        Block b4_ = bc.createForkBlock(b3_);

        Assert.assertEquals(l.pollTxUpdateState(tx1), PENDING);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
        Assert.assertEquals(l.pollTxUpdateState(tx2), INCLUDED);
        Assert.assertTrue(l.getQueueFor(tx2).isEmpty());
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000)) == 0);

        bc.submitTransaction(tx1);
        Block b5_ = bc.createForkBlock(b4_);

        Assert.assertEquals(l.pollTxUpdateState(tx1), INCLUDED);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
        Assert.assertTrue(l.getQueueFor(tx2).isEmpty());
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000)) == 0);
    }

    @Test
    public void testRebranch2() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();
        ECKey charlie = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));
        bc.sendEther(charlie.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        pendingState.addPendingTransaction(tx1);
        Transaction tx2 = bc.createTransaction(charlie, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);;
        pendingState.addPendingTransaction(tx2);

        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);
        Assert.assertEquals(l.pollTxUpdateState(tx2), NEW_PENDING);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(2000000)) == 0);

        bc.submitTransaction(tx1);
        bc.sendEther(alice.getAddress(), BigInteger.valueOf(1000000));
        Block b2 = bc.createBlock();
        Transaction tx3 = b2.getTransactionsList().get(1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), INCLUDED);
        Assert.assertEquals(l.pollTxUpdateState(tx2), PENDING);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
        Assert.assertTrue(l.getQueueFor(tx2).isEmpty());
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(3000000)) == 0);

        bc.sendEther(alice.getAddress(), BigInteger.valueOf(1000000));
        bc.submitTransaction(tx2);
        Block b3 = bc.createBlock();
        Transaction tx4 = b3.getTransactionsList().get(0);

        Assert.assertEquals(l.pollTxUpdateState(tx2), INCLUDED);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(4000000)) == 0);

        bc.submitTransaction(tx2);
        Block b2_ = bc.createForkBlock(b1);
        bc.submitTransaction(tx1);
        Block b3_ = bc.createForkBlock(b2_);

        Block b4_ = bc.createForkBlock(b3_); // becoming the best branch

        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertEquals(txUpd.getRight(), b3_);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
        txUpd = l.pollTxUpdate(tx2);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertEquals(txUpd.getRight(), b2_);
        Assert.assertTrue(l.getQueueFor(tx2).isEmpty());
        Assert.assertEquals(l.pollTxUpdateState(tx3), PENDING);
        Assert.assertEquals(l.pollTxUpdateState(tx4), PENDING);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(4000000)) == 0);

        // rebranching back
        Block b4 = bc.createForkBlock(b3);
        Block b5 = bc.createForkBlock(b4);

        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertEquals(txUpd.getRight(), b2);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
        txUpd = l.pollTxUpdate(tx2);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertEquals(txUpd.getRight(), b3);
        Assert.assertTrue(l.getQueueFor(tx2).isEmpty());
        Assert.assertEquals(l.pollTxUpdateState(tx3), INCLUDED);
        Assert.assertEquals(l.pollTxUpdateState(tx4), INCLUDED);
        Assert.assertTrue(pendingState.getRepository().getBalance(alice.getAddress()).
                compareTo(BigInteger.valueOf(4000000)) == 0);
    }

    @Test
    public void testRebranch3() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();
        ECKey charlie = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));
        bc.sendEther(charlie.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        pendingState.addPendingTransaction(tx1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);

        bc.submitTransaction(tx1);
        Block b2 = bc.createBlock();

        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        Block b3 = bc.createBlock();
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        bc.submitTransaction(tx1);
        Block b2_ = bc.createForkBlock(b1);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        Block b3_ = bc.createForkBlock(b2_);
        Block b4_ = bc.createForkBlock(b3_);
        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertArrayEquals(txUpd.getRight().getHash(), b2_.getHash());

        Block b4 = bc.createForkBlock(b3);
        Block b5 = bc.createForkBlock(b4);
        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertArrayEquals(txUpd.getRight().getHash(), b2.getHash());
    }

    @Test
    public void testOldBlockIncluded() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();
        ECKey charlie = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        for (int i = 0; i < 16; i++) {
            bc.createBlock();
        }

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        pendingState.addPendingTransaction(tx1);
        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);

        bc.submitTransaction(tx1);
        Block b2_ = bc.createForkBlock(b1);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        bc.submitTransaction(tx1);
        Block b18 = bc.createBlock();
        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertArrayEquals(txUpd.getRight().getHash(), b18.getHash());
    }

    @Test
    public void testBlockOnlyIncluded() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        bc.submitTransaction(tx1);
        Block b2 = bc.createBlock();

        Block b2_ = bc.createForkBlock(b1);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
        Block b3_ = bc.createForkBlock(b2_);
        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), PENDING);
    }

    @Test
    public void testTrackTx1() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();
        Block b2 = bc.createBlock();
        Block b3 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        bc.submitTransaction(tx1);
        Block b2_ = bc.createForkBlock(b1);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        pendingState.trackTransaction(tx1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);

        Block b3_ = bc.createForkBlock(b2_);
        Block b4_ = bc.createForkBlock(b3_);
        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertArrayEquals(txUpd.getRight().getHash(), b2_.getHash());
    }

    @Test
    public void testPrevBlock() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();

        SolidityContract contract = bc.submitNewContract("contract A {" +
                "  function getPrevBlockHash() returns (bytes32) {" +
                "    return block.blockhash(block.number - 1);" +
                "  }" +
                "}");

        bc.sendEther(bob.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();
        Block b2 = bc.createBlock();
        Block b3 = bc.createBlock();

        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd;

        contract.callFunction("getPrevBlockHash");
        bc.generatePendingTransactions();
        txUpd = l.onPendingTransactionUpdate.values().iterator().next().poll();

        Assert.assertArrayEquals(txUpd.getLeft().getExecutionResult(), b3.getHash());
    }

    @Test
    public void testTrackTx2() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));
        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        bc.submitTransaction(tx1);
        Block b2 = bc.createBlock();
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        pendingState.trackTransaction(tx1);

        txUpd = l.pollTxUpdate(tx1);
        Assert.assertEquals(txUpd.getMiddle(), INCLUDED);
        Assert.assertArrayEquals(txUpd.getRight().getHash(), b2.getHash());

        Block b2_ = bc.createForkBlock(b1);
        Block b3_ = bc.createForkBlock(b2_);
        Assert.assertEquals(l.pollTxUpdateState(tx1), PENDING);
    }

    @Test
    public void testRejected1() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();
        ECKey charlie = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));
        bc.sendEther(charlie.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        pendingState.addPendingTransaction(tx1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);

        bc.submitTransaction(tx1);
        Block b2_ = bc.createForkBlock(b1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), INCLUDED);

        Block b2 = bc.createForkBlock(b1);
        Block b3 = bc.createForkBlock(b2);
        Assert.assertEquals(l.pollTxUpdateState(tx1), PENDING);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());

        for (int i = 0; i < 16; i++) {
            bc.createBlock();
            EthereumListener.PendingTransactionState state = l.pollTxUpdateState(tx1);
            if (state == EthereumListener.PendingTransactionState.DROPPED) {
                break;
            }
            if (i == 15) {
                throw new RuntimeException("Transaction was not dropped");
            }
        }
    }

    @Test
    public void testIncludedRejected() throws InterruptedException {
        // check INCLUDED => DROPPED state transition when a new (long) fork without
        // the transaction becomes the main chain
        StandaloneBlockchain bc = new StandaloneBlockchain();
        PendingListener l = new PendingListener();
        bc.addEthereumListener(l);
        Triple<TransactionReceipt, EthereumListener.PendingTransactionState, Block> txUpd = null;
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();
        ECKey charlie = new ECKey();

        bc.sendEther(bob.getAddress(), convert(100, ETHER));
        bc.sendEther(charlie.getAddress(), convert(100, ETHER));

        Block b1 = bc.createBlock();

        Transaction tx1 = bc.createTransaction(bob, 0, alice.getAddress(), BigInteger.valueOf(1000000), new byte[0]);
        pendingState.addPendingTransaction(tx1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), NEW_PENDING);

        bc.submitTransaction(tx1);
        Block b2 = bc.createForkBlock(b1);

        Assert.assertEquals(l.pollTxUpdateState(tx1), INCLUDED);

        for (int i = 0; i < 10; i++) {
            bc.createBlock();
        }

        Block b_ = bc.createForkBlock(b1);

        for (int i = 0; i < 11; i++) {
            b_ = bc.createForkBlock(b_);
        }

        Assert.assertEquals(l.pollTxUpdateState(tx1), DROPPED);
        Assert.assertTrue(l.getQueueFor(tx1).isEmpty());
    }

    @Test
    public void testInvalidTransaction() throws InterruptedException {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        final CountDownLatch txHandle = new CountDownLatch(1);
        PendingListener l = new PendingListener() {
            @Override
            public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
                assert !txReceipt.isSuccessful();
                assert txReceipt.getError().toLowerCase().contains("invalid");
                assert txReceipt.getError().toLowerCase().contains("receive address");
                txHandle.countDown();
            }
        };
        bc.addEthereumListener(l);
        PendingStateImpl pendingState = (PendingStateImpl) bc.getBlockchain().getPendingState();

        ECKey alice = new ECKey();
        Random rnd = new Random();
        Block b1 = bc.createBlock();
        byte[] b = new byte[21];
        rnd.nextBytes(b);

        Transaction tx1 = bc.createTransaction(alice, 0, b, BigInteger.ONE, new byte[0]);
        pendingState.addPendingTransaction(tx1);

        assert txHandle.await(3, TimeUnit.SECONDS);
    }
}
