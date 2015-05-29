package test.ethereum.core;

import org.ethereum.core.*;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;


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

    private final static byte[] GOOD_BLOCK_RLP = Hex.decode("f90667f905fba09d2d8af58fe8efeb7930ccef1806e1147a35508d5b7f100f7b241ec93b35908aa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347948888f1f195afa192cfee860698584c030f4c9db1a0f93c8db1e931daa2e22e39b5d2da6fb4074e3d544094857608536155e3521bc1a0b05ab377881f195a1b1e1acb8785502fe40f3d3cd98ec563eaef1d49fac582c7a0c7778a7376099ee2e5c455791c1885b5c361b95713fddcbe32d97fd01334d296b90100000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000400000000000000000000000000000000000000000000000000000008302000001832fefd882560b84554ca188b9040001020304050607080910111213141516171819202122232410000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000100000000000000000002000000000000000000030000000000000000000400000000000000000005000000000000000000060000000000000000000700000000000000000008000000000000000000090000000000000000000100000000000000000001000000000000000000020000000000000000000300000000000000000004000000000000000000050000000000000000000600000000000000000007000000000000000000080000000000000000000900000000000000000001000000000000000000010000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000100000000000000000002000000000000000000030000000000000000000400000000000000000005000000000000000000060000000000000000000700000000000000000008000000000000000000090000000000000000000100000000000000000001000000000000000000020000000000000000000300000000000000000004000000000000000000050000000000000000000600000000000000000007000000000000000000080000000000000000000900000000000000000001000000000000000000010000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000100000000000000000002000000000000000000030000000000000000000400000000000000000005000000000000000000060000000000000000000700000000000000000008000000000000000000090000000000000000000100000000000000000001000000000000000000020000000000000000000300000000000000000004000000000000000000050000000000000000000600000000000000000007000000000000000000080000000000000000000900000000000000000001000000000000000000010000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000a0c9576ae76cad89b4ad76a95e4edc3180596e24b5d0d2dc7af101d5b40f6aad1388584f563c7858ce3af866f864800a82c35094095e7baea6a6c7c4c2dfeb977efac326af552d8785012a05f200801ba0a1687c0eb61e4de37bf2ce94ae4e71692cef8dfbd0ece8ecdc843af60def6595a057ade92561b633bca0cefd596114f58f780396c4c40b9f39def3db65ad2b022cc0");

    // increment GOOD_BLOCK_RLP's nonce by one, 7858ce3a => 7858ce3b
    private final static byte[] BAD_BLOCK_RLP = Hex.decode("f90667f905fba09d2d8af58fe8efeb7930ccef1806e1147a35508d5b7f100f7b241ec93b35908aa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347948888f1f195afa192cfee860698584c030f4c9db1a0f93c8db1e931daa2e22e39b5d2da6fb4074e3d544094857608536155e3521bc1a0b05ab377881f195a1b1e1acb8785502fe40f3d3cd98ec563eaef1d49fac582c7a0c7778a7376099ee2e5c455791c1885b5c361b95713fddcbe32d97fd01334d296b90100000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000000000400000000000000000000000000000000000000000000000000000008302000001832fefd882560b84554ca188b9040001020304050607080910111213141516171819202122232410000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000100000000000000000002000000000000000000030000000000000000000400000000000000000005000000000000000000060000000000000000000700000000000000000008000000000000000000090000000000000000000100000000000000000001000000000000000000020000000000000000000300000000000000000004000000000000000000050000000000000000000600000000000000000007000000000000000000080000000000000000000900000000000000000001000000000000000000010000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000100000000000000000002000000000000000000030000000000000000000400000000000000000005000000000000000000060000000000000000000700000000000000000008000000000000000000090000000000000000000100000000000000000001000000000000000000020000000000000000000300000000000000000004000000000000000000050000000000000000000600000000000000000007000000000000000000080000000000000000000900000000000000000001000000000000000000010000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000100000000000000000002000000000000000000030000000000000000000400000000000000000005000000000000000000060000000000000000000700000000000000000008000000000000000000090000000000000000000100000000000000000001000000000000000000020000000000000000000300000000000000000004000000000000000000050000000000000000000600000000000000000007000000000000000000080000000000000000000900000000000000000001000000000000000000010000000000000000000200000000000000000003000000000000000000040000000000000000000500000000000000000006000000000000000000070000000000000000000800000000000000000009000000000000000000010000000000000000000a0c9576ae76cad89b4ad76a95e4edc3180596e24b5d0d2dc7af101d5b40f6aad1388584f563c7858ce3bf866f864800a82c35094095e7baea6a6c7c4c2dfeb977efac326af552d8785012a05f200801ba0a1687c0eb61e4de37bf2ce94ae4e71692cef8dfbd0ece8ecdc843af60def6595a057ade92561b633bca0cefd596114f58f780396c4c40b9f39def3db65ad2b022cc0");

    static {
        TEST_GENESIS = TEST_GENESIS.replace("'", "\"");
    }

    @Test
    public void testGenesisFromRLP() {
        // from RLP encoding
        byte[] genesisBytes = Hex.decode(GENESIS_RLP);
        Block genesisFromRLP = new Block(genesisBytes);
        Block genesis = Genesis.getInstance();
        assertEquals(Hex.toHexString(genesis.getHash()),   Hex.toHexString(genesisFromRLP.getHash()));
        assertEquals(Hex.toHexString(genesis.getParentHash()), Hex.toHexString(genesisFromRLP.getParentHash()));
        assertEquals(Hex.toHexString(genesis.getStateRoot()), Hex.toHexString(genesisFromRLP.getStateRoot()));
    }


    @Test
    public void testGenesisFromNew() {
        Block genesis = Genesis.getInstance();
        logger.info(genesis.toString());

        logger.info("genesis hash: [{}]", Hex.toHexString(genesis.getHash()));
        logger.info("genesis rlp: [{}]", Hex.toHexString(genesis.getEncoded()));

        assertEquals(GENESIS_HASH, Hex.toHexString(genesis.getHash()));
        assertEquals(GENESIS_RLP, Hex.toHexString(genesis.getEncoded()));
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
        assertEquals(GENESIS_STATE_ROOT, Hex.toHexString(state.getRootHash()));
    }

    @Test
    public void testGoodBlockVerifies() {
	Block goodBlock = new Block( GOOD_BLOCK_RLP );
	assertTrue( goodBlock.validateMixHashAndNonce() );
    }

    @Test
    public void testBadBlockDoesNotVerify() {
	Block badBlock = new Block( BAD_BLOCK_RLP );
	assertFalse( badBlock.validateMixHashAndNonce() );
    }
}
