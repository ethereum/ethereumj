package org.ethereum.core;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.db.*;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.mine.Ethash;
import org.ethereum.util.ByteUtil;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Anton Nashatyrev on 29.12.2015.
 */
public class ImportLightTest {

    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
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
        Ethash.getForBlock(SystemProperties.getDefault(), b1.getNumber()).mineLight(b1).get();
        ImportResult importResult = blockchain.tryToConnect(b1);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2 ...");
        Block b2 = blockchain.createNewBlock(b1, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #3 ...");
        Block b3 = blockchain.createNewBlock(b2, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b3.getNumber()).mineLight(b3).get();
        importResult = blockchain.tryToConnect(b3);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2' ...");
        Block b2_ = blockchain.createNewBlock(b1, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        b2_.setExtraData(new byte[]{77, 77}); // setting extra data to differ from block #2
        Ethash.getForBlock(SystemProperties.getDefault(), b2_.getNumber()).mineLight(b2_).get();
        importResult = blockchain.tryToConnect(b2_);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_NOT_BEST);

        System.out.println("Mining #3' ...");
        Block b3_ = blockchain.createNewBlock(b2_, Collections.EMPTY_LIST, Collections.singletonList(b2.getHeader()));
        Ethash.getForBlock(SystemProperties.getDefault(), b3_.getNumber()).mineLight(b3_).get();
        importResult = blockchain.tryToConnect(b3_);
        System.out.println("Best: " + blockchain.getBestBlock().getShortDescr());
        Assert.assertTrue(importResult == ImportResult.IMPORTED_NOT_BEST);
    }

    @Test
    public void invalidBlockTest() throws Exception {
        // testing that bad block import effort doesn't affect the repository state

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

        Block b1bad = blockchain.createNewBlock(parent, Collections.singletonList(tx), Collections.EMPTY_LIST);
        // making the block bad
        b1bad.getStateRoot()[0] = 0;
        b1bad.setStateRoot(b1bad.getStateRoot()); // invalidate block

        Ethash.getForBlock(SystemProperties.getDefault(), b1bad.getNumber()).mineLight(b1bad).get();
        ImportResult importResult = blockchain.tryToConnect(b1bad);
        Assert.assertTrue(importResult == ImportResult.INVALID_BLOCK);
        Block b1 = blockchain.createNewBlock(parent, Collections.singletonList(tx), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b1.getNumber()).mineLight(b1).get();
        importResult = blockchain.tryToConnect(b1);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);
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
        tx.sign(senderKey);

        Block b1 = blockchain.createNewBlock(parent, Collections.singletonList(tx), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b1.getNumber()).mineLight(b1).get();
        ImportResult importResult = blockchain.tryToConnect(b1);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2 (bad) ...");
        Block b2 = blockchain.createNewBlock(b1, Collections.singletonList(tx), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        Assert.assertTrue(importResult == ImportResult.INVALID_BLOCK);

        System.out.println("Mining #2 (bad) ...");
        Transaction tx1 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(1),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{77}, new byte[0]);
        tx1.sign(senderKey);
        b2 = blockchain.createNewBlock(b1, Arrays.asList(tx1, tx1), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        Assert.assertTrue(importResult == ImportResult.INVALID_BLOCK);

        System.out.println("Mining #2 ...");
        Transaction tx2 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(2),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{77}, new byte[0]);
        tx2.sign(senderKey);
        b2 = blockchain.createNewBlock(b1, Arrays.asList(tx1, tx2), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b2.getNumber()).mineLight(b2).get();
        importResult = blockchain.tryToConnect(b2);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);

        System.out.println("Mining #2 (fork) ...");
        tx1 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(1),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx1.sign(senderKey);
        Block b2f = blockchain.createNewBlock(b1, Collections.singletonList(tx1), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b2f.getNumber()).mineLight(b2f).get();
        importResult = blockchain.tryToConnect(b2f);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_NOT_BEST);

        System.out.println("Mining #3 ...");
        tx1 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(3),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx1.sign(senderKey);
        tx2 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(4),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx2.sign(senderKey);
        Transaction tx3 = new Transaction(ByteUtil.intToBytesNoLeadZeroes(5),
                ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[]{88}, new byte[0]);
        tx3.sign(senderKey);
        Block b3 = blockchain.createNewBlock(b2, Arrays.asList(tx1, tx2, tx3), Collections.EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b3.getNumber()).mineLight(b3).get();
        importResult = blockchain.tryToConnect(b3);
        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);
    }

    @Test
    public void invalidBlockTotalDiff() throws Exception {
        // Check that importing invalid block doesn't affect totalDifficulty

        BlockchainImpl blockchain = createBlockchain(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light.json")));
        blockchain.setMinerCoinbase(Hex.decode("ee0250c19ad59305b2bdb61f34b45b72fe37154f"));
        Block parent = blockchain.getBestBlock();

        System.out.println("Mining #1 ...");

        BigInteger totalDifficulty = blockchain.getTotalDifficulty();

        Block b1 = blockchain.createNewBlock(parent, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        b1.setStateRoot(new byte[32]);
        Ethash.getForBlock(SystemProperties.getDefault(), b1.getNumber()).mineLight(b1).get();
        ImportResult importResult = blockchain.tryToConnect(b1);
        Assert.assertTrue(importResult == ImportResult.INVALID_BLOCK);
        Assert.assertEquals(totalDifficulty, blockchain.getTotalDifficulty());

    }

    @Test
    public void createContractFork() throws Exception {
        //  #1 (Parent) --> #2 --> #3 (Child) ----------------------> #4 (call Child)
        //    \-------------------------------> #2' (forked Child)
        //
        // Testing the situation when the Child contract is created by the Parent contract
        // first on the main chain with one parameter (#3) and then on the fork with another parameter (#2')
        // so their storages are different. Check that original Child storage is not broken
        // on the main chain (#4)

        String contractSrc =
                "contract Child {" +
                "  int a;" +
                "  int b;" +
                "  int public c;" +
                "  function Child(int i) {" +
                "    a = 333 + i;" +
                "    b = 444 + i;" +
                "  }" +
                "  function sum() {" +
                "    c = a + b;" +
                "  }" +
                "}" +
                "contract Parent {" +
                "  address public child;" +
                "  function createChild(int a) returns (address) {" +
                "    child = new Child(a);" +
                "    return child;" +
                "  }" +
                "}";

        StandaloneBlockchain bc = new StandaloneBlockchain();
        SolidityContract parent = bc.submitNewContract(contractSrc, "Parent");
        Block b1 = bc.createBlock();
        Block b2 = bc.createBlock();
        parent.callFunction("createChild", 100);
        Block b3 = bc.createBlock();
        byte[] childAddress = (byte[]) parent.callConstFunction("child")[0];
        parent.callFunction("createChild", 200);
        Block b2_ = bc.createForkBlock(b1);
        SolidityContract child = bc.createExistingContractFromSrc(contractSrc, "Child", childAddress);
        child.callFunction("sum");
        Block b4 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(100 + 333 + 100 + 444), child.callConstFunction("c")[0]);
    }

    @Test
    public void createContractFork1() throws Exception {
        // Test creation of the contract on forked branch with different storage
        String contractSrc =
                "contract A {" +
                "  int public a;" +
                "  function A() {" +
                "    a = 333;" +
                "  }" +
                "}" +
                "contract B {" +
                "  int public a;" +
                "  function B() {" +
                "    a = 111;" +
                "  }" +
                "}";

        {
            StandaloneBlockchain bc = new StandaloneBlockchain();
            Block b1 = bc.createBlock();
            Block b2 = bc.createBlock();
            SolidityContract a = bc.submitNewContract(contractSrc, "A");
            Block b3 = bc.createBlock();
            SolidityContract b = bc.submitNewContract(contractSrc, "B");
            Block b2_ = bc.createForkBlock(b1);
            Assert.assertEquals(BigInteger.valueOf(333), a.callConstFunction("a")[0]);
            Assert.assertEquals(BigInteger.valueOf(111), b.callConstFunction(b2_, "a")[0]);
            Block b3_ = bc.createForkBlock(b2_);
            Block b4_ = bc.createForkBlock(b3_);
            Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("a")[0]);
            Assert.assertEquals(BigInteger.valueOf(333), a.callConstFunction(b3, "a")[0]);

        }
    }

    @Test
    public void createValueTest() throws IOException, InterruptedException {
        // checks that correct msg.value is passed when contract internally created with value
        String contract =
                "contract B {" +
                "  uint public valReceived;" +
                "  function B() {" +
                "    valReceived = msg.value;" +
                "  }" +
                "}" +
                "contract A {" +
                "    address public child;" +
                "    function create() {" +
                "        child = (new B).value(20)();" +
                "    }" +
                "}";
        StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = bc.submitNewContract(contract, "A");
        bc.sendEther(a.getAddress(), BigInteger.valueOf(10000));
        a.callFunction(10, "create");
        byte[] childAddress = (byte[]) a.callConstFunction("child")[0];
        SolidityContract b = bc.createExistingContractFromSrc(contract, "B", childAddress);
        BigInteger val = (BigInteger) b.callConstFunction("valReceived")[0];
        Assert.assertEquals(20, val.longValue());
    }

    @Test
    public void contractCodeForkTest() throws IOException, InterruptedException {
        String contractA =
                "contract A {" +
                "  function call() returns (uint) {" +
                "    return 111;" +
                "  }" +
                "}";

        String contractB =
                "contract B {" +
                "  function call() returns (uint) {" +
                "    return 222222;" +
                "  }" +
                "}";

        StandaloneBlockchain bc = new StandaloneBlockchain();
        Block b1 = bc.createBlock();
        SolidityContract a = bc.submitNewContract(contractA);
        Block b2 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("call")[0]);
        SolidityContract b = bc.submitNewContract(contractB);
        Block b2_ = bc.createForkBlock(b1);
        Block b3 = bc.createForkBlock(b2);
        Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("call")[0]);
        Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction(b2, "call")[0]);
        Assert.assertEquals(BigInteger.valueOf(222222), b.callConstFunction(b2_, "call")[0]);
    }

    @Test
    public void getBalanceTest() throws IOException, InterruptedException {
        // checking that addr.balance doesn't cause the account to be created
        // and the subsequent call to that non-existent address costs 25K gas
        byte[] addr = Hex.decode("0101010101010101010101010101010101010101");
        String contractA =
                "contract B { function dummy() {}}" +
                "contract A {" +
                "  function call() returns (uint) {" +
                "    address addr = 0x" + Hex.toHexString(addr) + ";" +
                "    uint bal = addr.balance;" +
                "    B b = B(addr);" +
                "    b.dummy();" +
                "  }" +
                "}";

        StandaloneBlockchain bc = new StandaloneBlockchain().withGasPrice(1);
        SolidityContract a = bc.submitNewContract(contractA, "A");
        bc.createBlock();
        BigInteger balance1 = bc.getBlockchain().getRepository().getBalance(bc.getSender().getAddress());
        a.callFunction("call");
        bc.createBlock();
        BigInteger balance2 = bc.getBlockchain().getRepository().getBalance(bc.getSender().getAddress());
        long spent = balance1.subtract(balance2).longValue();
        Assert.assertEquals(46634, spent);
    }

    @Test
    public void deepRecursionTest() throws Exception {
        String contractA =
                "contract A {" +
                "  function recursive(){" +
                "    this.recursive();" +
                "  }" +
                "}";

        StandaloneBlockchain bc = new StandaloneBlockchain().withGasLimit(5_000_000);
        SolidityContract a = bc.submitNewContract(contractA, "A");
        bc.createBlock();
        a.callFunction("recursive");
        bc.createBlock();

        // no StackOverflowException
    }

    public static BlockchainImpl createBlockchain(Genesis genesis) {
        IndexedBlockStore blockStore = new IndexedBlockStore();
        blockStore.init(new HashMapDB(), new HashMapDB());

        RepositoryImpl repository = new RepositoryImpl(new HashMapDB(), new HashMapDB());

        ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
        EthereumListenerAdapter listener = new EthereumListenerAdapter();

        BlockchainImpl blockchain = new BlockchainImpl(blockStore, repository)
                .withParentBlockHeaderValidator(new CommonConfig().parentHeaderValidator());
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
        repository.commitBlock(genesis.getHeader());

        blockStore.saveBlock(genesis, genesis.getCumulativeDifficulty(), true);

        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        return blockchain;
    }
}
