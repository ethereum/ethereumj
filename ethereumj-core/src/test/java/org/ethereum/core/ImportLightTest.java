package org.ethereum.core;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.fork.MainForkConfig;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.AdminInfo;
import org.ethereum.mine.Ethash;
import org.ethereum.util.ByteUtil;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 29.12.2015.
 */
public class ImportLightTest {

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
        SystemProperties.CONFIG.setBlockchainConfig(MainForkConfig.INSTANCE);
    }

    @Test
    public void createFork() throws Exception {
        // importing forked chain
        BlockchainImpl blockchain = createBlockchain(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light.json")));
        blockchain.setMinerCoinbase(Hex.decode("ee0250c19ad59305b2bdb61f34b45b72fe37154f"));
        Block parent = blockchain.getBestBlock();

        System.out.println("Mining #1 ...");
        Block b1 = blockchain.createNewBlock(parent, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Ethash.getForBlock(b1.getNumber()).mineLight(b1).get();
        ImportResult importResult = blockchain.tryToConnect(b1);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2 ...");
        Block b2 = blockchain.createNewBlock(b1, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Ethash.getForBlock(b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #3 ...");
        Block b3 = blockchain.createNewBlock(b2, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Ethash.getForBlock(b3.getNumber()).mineLight(b3).get();
        importResult = blockchain.tryToConnect(b3);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2' ...");
        Block b2_ = blockchain.createNewBlock(b1, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        b2_.setExtraData(new byte[]{77, 77}); // setting extra data to differ from block #2
        Ethash.getForBlock(b2_.getNumber()).mineLight(b2_).get();
        importResult = blockchain.tryToConnect(b2_);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_NOT_BEST);

        System.out.println("Mining #3' ...");
        Block b3_ = blockchain.createNewBlock(b2_, Collections.EMPTY_LIST, Collections.singletonList(b2.getHeader()));
        Ethash.getForBlock(b3_.getNumber()).mineLight(b3_).get();
        importResult = blockchain.tryToConnect(b3_);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_NOT_BEST);
    }

    @Test
    public void doubleTransactionTest() throws Exception {
        // Testing that blocks containing tx with invalid nonce are rejected

        BlockchainImpl blockchain = createBlockchain(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light.json")));
        blockchain.setMinerCoinbase(Hex.decode("ee0250c19ad59305b2bdb61f34b45b72fe37154f"));
        Block parent = blockchain.getBestBlock();

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
        byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");

        System.out.println("Mining #1 ...");

        Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(0),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{77}, new byte[0]);
        tx.sign(senderKey.getPrivKeyBytes());

        Block b1 = blockchain.createNewBlock(parent, Collections.singletonList(tx), Collections.EMPTY_LIST);
        Ethash.getForBlock(b1.getNumber()).mineLight(b1).get();
        ImportResult importResult = blockchain.tryToConnect(b1);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2 (bad) ...");
        Block b2 = blockchain.createNewBlock(b1, Collections.singletonList(tx), Collections.EMPTY_LIST);
        Ethash.getForBlock(b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        Assert.assertTrue(importResult == ImportResult.INVALID_BLOCK);

        System.out.println("Mining #2 (bad) ...");
        Transaction tx1 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(1),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{77}, new byte[0]);
        tx1.sign(senderKey.getPrivKeyBytes());
        b2 = blockchain.createNewBlock(b1, Arrays.asList(tx1, tx1), Collections.EMPTY_LIST);
        Ethash.getForBlock(b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        Assert.assertTrue(importResult == ImportResult.INVALID_BLOCK);

        System.out.println("Mining #2 ...");
        Transaction tx2 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(2),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{77}, new byte[0]);
        tx2.sign(senderKey.getPrivKeyBytes());
        b2 = blockchain.createNewBlock(b1, Arrays.asList(tx1, tx2), Collections.EMPTY_LIST);
        Ethash.getForBlock(b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2 (fork) ...");
        tx1 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(1),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx1.sign(senderKey.getPrivKeyBytes());
        Block b2f = blockchain.createNewBlock(b1, Arrays.asList(tx1), Collections.EMPTY_LIST);
        Ethash.getForBlock(b2f.getNumber()).mineLight(b2f).get();
        importResult = blockchain.tryToConnect(b2f);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_NOT_BEST);

        System.out.println("Mining #3 ...");
        tx1 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(3),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx1.sign(senderKey.getPrivKeyBytes());
        tx2 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(4),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx2.sign(senderKey.getPrivKeyBytes());
        Transaction tx3 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(5),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx3.sign(senderKey.getPrivKeyBytes());
        Block b3 = blockchain.createNewBlock(b2, Arrays.asList(tx1, tx2, tx3), Collections.EMPTY_LIST);
        Ethash.getForBlock(b3.getNumber()).mineLight(b3).get();
        importResult = blockchain.tryToConnect(b3);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);
    }

    public static BlockchainImpl createBlockchain(Genesis genesis) {
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null, null);

        Repository repository = new RepositoryImpl(new HashMapDB(), new HashMapDB());

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        EthereumListenerAdapter listener = new EthereumListenerAdapter();

        BlockchainImpl blockchain = new BlockchainImpl(
                blockStore,
                repository,
                new Wallet(),
                new AdminInfo(),
                listener,
                new CommonConfig().parentHeaderValidator()
        );
        blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
        blockchain.setProgramInvokeFactory(programInvokeFactory);
        programInvokeFactory.setBlockchain(blockchain);

        blockchain.byTest = true;

        PendingStateImpl pendingState = new PendingStateImpl(listener, blockchain);

        pendingState.init();

        pendingState.setBlockchain(blockchain);
        blockchain.setPendingState(pendingState);

        Repository track = repository.startTracking();
        for (ByteArrayWrapper key : genesis.getPremine().keySet()) {
            track.createAccount(key.getData());
            track.addBalance(key.getData(), genesis.getPremine().get(key).getBalance());
        }

        track.commit();

        blockStore.saveBlock(genesis, genesis.getCumulativeDifficulty(), true);

        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        return blockchain;
    }
}
