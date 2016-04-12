package org.ethereum.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlockTest {

    private static final Logger logger = LoggerFactory.getLogger("test");


    // https://github.com/ethereum/tests/blob/71d80bd63aaf7cee523b6ca9d12a131698d41e98/BasicTests/genesishashestest.json
    private String GENESIS_RLP = "f901f8f901f3a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a09178d0f23c965d81f0834a4c72c6253ce6830f4022b1359aaebfc1ecba442d4ea056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008302000080832fefd8808080a0000000000000000000000000000000000000000000000000000000000000000088000000000000002ac0c0";
    private String GENESIS_HASH = "fd4af92a79c7fc2fd8bf0d342f2e832e1d4f485c85b9152d2039e03bc604fdca";
    private String GENESIS_STATE_ROOT = "9178d0f23c965d81f0834a4c72c6253ce6830f4022b1359aaebfc1ecba442d4e";


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



    @Test
    public void testGenesisFromRLP() {
        // from RLP encoding
        byte[] genesisBytes = Hex.decode(GENESIS_RLP);
        Block genesisFromRLP = new Block(genesisBytes);
        Block genesis = GenesisLoader.loadGenesis(getClass().getResourceAsStream("/genesis/olympic.json"));
        assertEquals(Hex.toHexString(genesis.getHash()),   Hex.toHexString(genesisFromRLP.getHash()));
        assertEquals(Hex.toHexString(genesis.getParentHash()), Hex.toHexString(genesisFromRLP.getParentHash()));
        assertEquals(Hex.toHexString(genesis.getStateRoot()), Hex.toHexString(genesisFromRLP.getStateRoot()));
    }

    @Test
    public void testGenesisFromNew() {

        Block genesis = GenesisLoader.loadGenesis(getClass().getResourceAsStream("/genesis/olympic.json"));
        logger.info(genesis.toString());

        logger.info("genesis hash: [{}]", Hex.toHexString(genesis.getHash()));
        logger.info("genesis rlp: [{}]", Hex.toHexString(genesis.getEncoded()));

        assertEquals(GENESIS_HASH, Hex.toHexString(genesis.getHash()));
        assertEquals(GENESIS_RLP, Hex.toHexString(genesis.getEncoded()));
    }

    @Test
    public void testGenesisPremineData() {
        Genesis genesis = GenesisLoader.loadGenesis(getClass().getResourceAsStream("/genesis/olympic.json"));
        Collection<AccountState> accounts = genesis.getPremine().values();
        assertTrue(accounts.size() == 12);
    }


    @Test
    public void testPremineFromJSON() throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject genesisMap = (JSONObject) parser.parse(TEST_GENESIS);

        Set keys = genesisMap.keySet();

        Trie state = new SecureTrie(null);

        for (Object key : keys) {

            JSONObject val = (JSONObject) genesisMap.get(key);
            String denom = (String) val.keySet().toArray()[0];
            String value = (String) val.values().toArray()[0];

            BigInteger wei = Denomination.valueOf(denom.toUpperCase()).value().multiply(new BigInteger(value));

            AccountState acctState = new AccountState(BigInteger.ZERO, wei);
            state.update(Hex.decode(key.toString()), acctState.getEncoded());
        }

        logger.info("root: " + Hex.toHexString(state.getRootHash()));
        assertEquals(GENESIS_STATE_ROOT, Hex.toHexString(state.getRootHash()));
    }


    @Test
    public void testFrontierGenesis(){

        SystemProperties config = SystemProperties.getDefault();
        config.setGenesisInfo("frontier.json");

        Block genesis = GenesisLoader.loadGenesis(config);

        String hash = Hex.toHexString(genesis.getHash());
        String root = Hex.toHexString(genesis.getStateRoot());

        assertEquals("d7f8974fb5ac78d9ac099b9ad5018bedc2ce0a72dad1827a1709da30580f0544", root);
        assertEquals("d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3", hash);
    }


}
