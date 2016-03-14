package org.ethereum.core;

import org.ethereum.config.CommonConfig;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.ReceiptStore;
import org.ethereum.db.ReceiptStoreImpl;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.AdminInfo;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.ethereum.util.BIUtil.toBI;
import static org.junit.Assert.*;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2015
 */
public class PendingStateTest {

    private PendingState pendingState;

    private Blockchain blockchain;

    private Repository repository;

    @Before
    public void setUp() {
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null, null);

        repository = new RepositoryImpl(new HashMapDB(), new HashMapDB());
        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        EthereumListenerAdapter listener = new EthereumListenerAdapter();

        KeyValueDataSource ds = new HashMapDB();
        ds.init();
        ReceiptStore receiptStore = new ReceiptStoreImpl(ds);

        blockchain = new BlockchainImpl(blockStore, repository, new Wallet(), new AdminInfo(), listener, new CommonConfig().parentHeaderValidator(), receiptStore);
        PendingStateImpl pendingState = new PendingStateImpl(listener, (BlockchainImpl) blockchain);
        pendingState.setBlockchain(blockchain);
        pendingState.init();

        this.pendingState = pendingState;
    }

    @Test // basic run
    public void test_1() {

        Block parent = new Block(Hex.decode("f9021bf90216a063af6caaa028f7fdd8088199c7a6741f9b58dfa571a0f0780260045fab2f94e0a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794157c6f2431eb1a95797ee86f7d17b3c0fd47a247a05412c03b1c22d01fe37fc92d721ab617f94699f9d59a6e68479145412af3edaea056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008601535dc6078e82b45882534e808455c427c79b476574682f76312e302e312f77696e646f77732f676f312e342e32a0cb54fe0fab2f63fbcee34ac87750402496a7b2cb90ac946f1b0daf54745ad70f8854ebbc061731f2d3c0c0"));
        Block block = new Block(Hex.decode("f9028bf90216a0040d1cae2f64a48916a4c21c7e404bb2a9077b0df21d3380874d53e8a9cbf946a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794e9a411144a689aa2137b0efe7b6273491e636d8ca024a3c69df04ba13066824e3289ec11ba42551d9a86b190146843174277058aa5a05e84a6f6b13b9b0bf9d34eb464bb77ec898471c80a7eb8bee796db70fbb67fd3a03a77c2406f28436905e8a91f40ab703c479cd6883b097c93a9c22e181122fe4eb9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000860153335a4ece82b4598253618252088455c427d599476574682f76312e302e312f6c696e75782f676f312e342e32a00e400227693c630a5a0b6dbd46caf240a557750fbb4f3daad8b5b48326d36297885ab261ffb22071fef86ff86d8085d3d4d32816825208945c12a8e43faf884521c2454f39560e6c265a68c88901142b0090b6460000801ba06bf8f2ac14eb21a072f51a3cc75ee8aec5125255f06702ce3d40d4386de825f3a012799e552161d4730177fecd66c2e286d39505a855017eb1f05fa9fd4075e3ccc0"));

        blockchain.setBestBlock(parent);

        Transaction tx = block.getTransactionsList().get(0);
        Repository track = repository.startTracking();
        track.createAccount(tx.getSender());
        track.getAccountState(tx.getSender()).addToBalance(new BigInteger("1000000000000000000000"));
        track.commit();

        Repository pending = pendingState.getRepository();

        BigInteger balanceBefore = pending.getAccountState(tx.getReceiveAddress()).getBalance();
        BigInteger balanceAfter = balanceBefore.add(toBI(tx.getValue()));

        pendingState.addPendingTransaction(tx);
        assertEquals(pendingState.getPendingTransactions().size(), 1);

        pending = pendingState.getRepository();
        assertEquals(balanceAfter, pending.getAccountState(tx.getReceiveAddress()).getBalance());

        pendingState.processBest(block);
        assertEquals(pendingState.getPendingTransactions().size(), 0);
    }


    ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
    byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");

    private Transaction createTx(int nonce, int val) {
        Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, ByteUtil.intToBytesNoLeadZeroes(val), new byte[0]);
        tx.sign(senderKey.getPrivKeyBytes());
        return tx;
    }

    @Test
    public void changeForkTest() {
        // testing that PendingState correctly handles situation when the best fork switches
        // and the new block contains another set of transactions

        BlockchainImpl blockchain = ImportLightTest.createBlockchain(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light.json")));
        blockchain.setMinerCoinbase(Hex.decode("ee0250c19ad59305b2bdb61f34b45b72fe37154f"));

        PendingStateImpl pendingState = new PendingStateImpl(new EthereumListenerAdapter(), blockchain);
        pendingState.setBlockchain(blockchain);
        pendingState.init();

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
        byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");

        Transaction tx1 = createTx(2, 77);
        Block b1 = blockchain.createNewBlock(blockchain.getBestBlock(), Arrays.asList(tx1), Collections.EMPTY_LIST);

        pendingState.addPendingTransaction(tx1);
        assertEquals(pendingState.getPendingTransactions().size(), 1);

        pendingState.processBest(b1);
        assertEquals(pendingState.getPendingTransactions().size(), 0);

        Block b1_ = blockchain.createNewBlock(blockchain.getBestBlock(), Arrays.<Transaction>asList(), Collections.EMPTY_LIST);
        pendingState.processBest(b1_);
        // b1_ has no transactions but it is now the BEST instead of b1 so the tx1 should be returned
        // back to the pending state
        assertEquals(pendingState.getWireTransactions().size(), 1);
        assertTrue(Arrays.equals(pendingState.getWireTransactions().get(0).getHash(), tx1.getHash()));
    }
}
