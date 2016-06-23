package org.ethereum.core;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.DROPPED;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.INCLUDED;
import static org.ethereum.listener.EthereumListener.PendingTransactionState.PENDING;
import static org.ethereum.util.BIUtil.toBI;
import static org.ethereum.util.blockchain.EtherUtil.Unit.ETHER;
import static org.ethereum.util.blockchain.EtherUtil.convert;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2015
 */
public class PendingStateTest {

    @BeforeClass
    public static void setup() {
        SystemProperties.CONFIG.setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.CONFIG.setBlockchainConfig(MainNetConfig.INSTANCE);
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
        Assert.assertEquals(txUpd.getMiddle(), PENDING);
        Assert.assertTrue(txUpd.getLeft().isValid());
        txUpd = l.pollTxUpdate(tx3);
        Assert.assertEquals(txUpd.getMiddle(), PENDING);
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

        for (int i = 0; i < SystemProperties.CONFIG.txOutdatedThreshold() + 1; i++) {
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

        Assert.assertEquals(l.pollTxUpdateState(tx1), PENDING);
        Assert.assertEquals(l.pollTxUpdateState(tx2), PENDING);
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
    public void testRebranch2_() throws InterruptedException {
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

        Assert.assertEquals(l.pollTxUpdateState(tx1), PENDING);
        Assert.assertEquals(l.pollTxUpdateState(tx2), PENDING);
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
}
