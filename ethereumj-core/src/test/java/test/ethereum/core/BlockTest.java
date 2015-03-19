package test.ethereum.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.facade.Blockchain;
import org.ethereum.manager.WorldManager;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import test.ethereum.TestContext;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class BlockTest {

    private static final Logger logger = LoggerFactory.getLogger("test");


    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext {
        static {
            SystemProperties.CONFIG.setDataBaseDir("test_db/" + BlockTest.class);
        }
    }

    @Autowired
    WorldManager worldManager;

    // https://github.com/ethereum/tests/blob/71d80bd63aaf7cee523b6ca9d12a131698d41e98/BasicTests/genesishashestest.json
    private String POC9_GENESIS_HEX_RLP_ENCODED = "f90219f90214a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a09178d0f23c965d81f0834a4c72c6253ce6830f4022b1359aaebfc1ecba442d4ea056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008302000080830f4240808080a00000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000000088000000000000002ac0c0";
    private String POC9_GENESIS_HEX_HASH = "b5d6d8402156c5c1dfadaa4b87c676b5bcadb17ef9bc8e939606daaa0d35f55d";
    private String POC9_GENESIS_HEX_STATE_ROOT = "9178d0f23c965d81f0834a4c72c6253ce6830f4022b1359aaebfc1ecba442d4e";



    static String TEST_GENESIS =
            "{" +
            "'0000000000000000000000000000000000000001': { 'wei': '1' }" +
            "'0000000000000000000000000000000000000002': { 'wei': '1' }" +
            "'0000000000000000000000000000000000000003': { 'wei': '1' }" +
            "'0000000000000000000000000000000000000004': { 'wei': '1' }" +
            "'dbdbdb2cbd23b783741e8d7fcf51e459b497e4a6': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'e6716f9544a56c530d868e4bfbacb172315bdead': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'b9c015918bdaba24b4ff057a92a3873d6eb201be': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'1a26338f0d905e295fccb71fa9ea849ffa12aaf4': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'2ef47100e0787b915105fd5e3f4ff6752079d5cb': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'cd2a3d9f938e13cd947ec05abc7fe734df8dd826': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'6c386a4b26f73c802f34673f7248bb118f97424a': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "'e4157b34ea9615cfbde6b4fda419828124b70c78': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
            "}";

    static {
        TEST_GENESIS = TEST_GENESIS.replace("'", "\"");
    }


    @After
    public void doReset() {
        worldManager.reset();
    }


    @Ignore
    @Test
    public void testGenesisFromRLP() {
        // from RLP encoding
        byte[] genesisBytes = Hex.decode(POC9_GENESIS_HEX_RLP_ENCODED);
        Block genesisFromRLP = new Block(genesisBytes);
        Block genesis = Genesis.getInstance();
        assertEquals(Hex.toHexString(genesis.getHash()),   Hex.toHexString(genesisFromRLP.getHash()));
        assertEquals(Hex.toHexString(genesis.getParentHash()), Hex.toHexString(genesisFromRLP.getParentHash()));
        assertEquals(Hex.toHexString(genesis.getStateRoot()), Hex.toHexString(genesisFromRLP.getStateRoot()));
    }

    @Ignore
    @Test
    public void testGenesisFromNew() {
        Block genesis = Genesis.getInstance();
        logger.info(genesis.toString());

        logger.info("genesis hash: [{}]", Hex.toHexString(genesis.getHash()));
        logger.info("genesis rlp: [{}]", Hex.toHexString(genesis.getEncoded()));
        assertEquals(POC9_GENESIS_HEX_HASH, Hex.toHexString(genesis.getHash()));
        assertEquals(POC9_GENESIS_HEX_RLP_ENCODED, Hex.toHexString(genesis.getEncoded()));
    }

    @Test
    public void testGenesisPremineData() {
        Genesis genesis = (Genesis) Genesis.getInstance();
        Collection<AccountState> accounts = genesis.getPremine().values();
        assertTrue(accounts.size() == 12);
    }


    @Test
    public void testPremineFromJSON() throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject genesisMap = (JSONObject) parser.parse(TEST_GENESIS);

        Set<String> keys = genesisMap.keySet();

        Trie state = new SecureTrie(null);

        for (String key : keys) {

            JSONObject val = (JSONObject) genesisMap.get(key);
            String denom = (String) val.keySet().toArray()[0];
            String value = (String) val.values().toArray()[0];

            BigInteger wei = Denomination.valueOf(denom.toUpperCase()).value().multiply(new BigInteger(value));

            AccountState acctState = new AccountState(BigInteger.ZERO, wei);
            state.update(Hex.decode(key), acctState.getEncoded());
        }

        logger.info("root: " + Hex.toHexString(state.getRootHash()));
        assertEquals(POC9_GENESIS_HEX_STATE_ROOT, Hex.toHexString(state.getRootHash()));
    }


    @Test /* block without transactions - block#32  */
    public void testEmptyBlock() {
        // todo: add real block wire format testing
    }

    @Test /* block with some transactions  */
    public void testWithTransactionsBlock() {
        // todo: add real block wire format testing
    }


    @Test
    public void testCalcDifficulty() {

        Blockchain blockchain = worldManager.getBlockchain();
        Block genesis = Genesis.getInstance();
        BigInteger difficulty = new BigInteger(1, genesis.calcDifficulty());
        logger.info("Genesis difficulty: [{}]", difficulty.toString());
        assertEquals(new BigInteger(1, Genesis.DIFFICULTY), difficulty);

        // Storing genesis because the parent needs to be in the DB for calculation.
        blockchain.add(genesis);

        Block block1 = new Block(Hex.decode(POC9_GENESIS_HEX_RLP_ENCODED));
        BigInteger calcDifficulty = new BigInteger(1, block1.calcDifficulty());
        BigInteger actualDifficulty = new BigInteger(1, block1.getDifficulty());
        logger.info("Block#1 actual difficulty: [{}] ", actualDifficulty.toString());
        logger.info("Block#1 calculated difficulty: [{}] ", calcDifficulty.toString());
        assertEquals(actualDifficulty, calcDifficulty);
    }


    @Ignore
    @Test
    public void testScenario1() throws URISyntaxException, IOException {

        BlockchainImpl blockchain = (BlockchainImpl) worldManager.getBlockchain();

        URL scenario1 = ClassLoader
                .getSystemResource("blockload/scenario1.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        byte[] root = Genesis.getInstance().getStateRoot();
        for (String blockRLP : strData) {
            Block block = new Block(
                    Hex.decode(blockRLP));
            logger.info("sending block.hash: {}", Hex.toHexString(block.getHash()));
            blockchain.tryToConnect(block);
            root = block.getStateRoot();
        }

        logger.info("asserting root state is: {}", Hex.toHexString(root));

        //expected root: fb8be59e6420892916e3967c60adfdf48836af040db0072ca411d7aaf5663804
        assertEquals(Hex.toHexString(root),
                Hex.toHexString(worldManager.getRepository().getRoot()));
    }

    @Ignore
    @Test
    public void testScenario2() throws URISyntaxException, IOException {

        BlockchainImpl blockchain = (BlockchainImpl) worldManager.getBlockchain();

        URL scenario1 = ClassLoader
                .getSystemResource("blockload/scenario2.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        byte[] root = Genesis.getInstance().getStateRoot();
        for (String blockRLP : strData) {
            Block block = new Block(
                    Hex.decode(blockRLP));
            logger.info("sending block.hash: {}", Hex.toHexString(block.getHash()));
            blockchain.tryToConnect(block);
            root = block.getStateRoot();
        }

        logger.info("asserting root state is: {}", Hex.toHexString(root));

        //expected root: a5e2a18bdbc4ab97775f44852382ff5585b948ccb15b1d69f0abb71e2d8f727d
        assertEquals(Hex.toHexString(root),
                Hex.toHexString(worldManager.getRepository().getRoot()));
    }


    @Test
    @Ignore
    public void testUncleValidGenerationGap() {
        // TODO
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testUncleInvalidGenerationGap() {
        // TODO
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testUncleInvalidParentGenerationGap() {
        // TODO
        fail("Not yet implemented");
    }
}