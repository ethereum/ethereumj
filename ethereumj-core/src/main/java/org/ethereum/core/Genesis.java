package org.ethereum.core;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.util.ByteUtil.wrap;
/**
 * The genesis block is the first block in the chain and has fixed values according to
 * the protocol specification. The genesis block is 13 items, and is specified thus:
 * <p>
 * ( zerohash_256 , SHA3 RLP () , zerohash_160 , stateRoot, 0, 2^22 , 0, 0, 1000000, 0, 0, 0, SHA3 (42) , (), () )
 * <p>
 * - Where zerohash_256 refers to the parent hash, a 256-bit hash which is all zeroes;
 * - zerohash_160 refers to the coinbase address, a 160-bit hash which is all zeroes;
 * - 2^22 refers to the difficulty;
 * - 0 refers to the timestamp (the Unix epoch);
 * - the transaction trie root and extradata are both 0, being equivalent to the empty byte array.
 * - The sequences of both uncles and transactions are empty and represented by ().
 * - SHA3 (42) refers to the SHA3 hash of a byte array of length one whose first and only byte is of value 42.
 * - SHA3 RLP () value refers to the hash of the uncle lists in RLP, both empty lists.
 * <p>
 * See Yellow Paper: http://www.gavwood.com/Paper.pdf (Appendix I. Genesis Block)
 */
public class Genesis extends Block {

    public final static BigInteger PREMINE_AMOUNT = BigInteger.valueOf(2).pow(200);

    // Genesis reference:  https://github.com/ethereum/cpp-ethereum/blob/[#branch#]/libethereum/GenesisInfo.cpp
    static String GENESIS_JSON =
            "{" +
                    "'0000000000000000000000000000000000000001': { 'wei': '1' }" +
                    "'0000000000000000000000000000000000000002': { 'wei': '1' }" +
                    "'0000000000000000000000000000000000000003': { 'wei': '1' }" +
                    "'0000000000000000000000000000000000000004': { 'wei': '1' }" +
                    "'dbdbdb2cbd23b783741e8d7fcf51e459b497e4a6': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(J) */            "'e6716f9544a56c530d868e4bfbacb172315bdead': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(V) */            "'b9c015918bdaba24b4ff057a92a3873d6eb201be': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(A) */            "'1a26338f0d905e295fccb71fa9ea849ffa12aaf4': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(M) */            "'2ef47100e0787b915105fd5e3f4ff6752079d5cb': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(R) */            "'cd2a3d9f938e13cd947ec05abc7fe734df8dd826': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(HH)*/            "'6c386a4b26f73c802f34673f7248bb118f97424a': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
/*(CH)*/            "'e4157b34ea9615cfbde6b4fda419828124b70c78': { 'wei': '1606938044258990275541962092341162602522202993782792835301376' }" +
                    "}";

    static {
        GENESIS_JSON = GENESIS_JSON.replace("'", "\"");
    }


    private Map<ByteArrayWrapper, AccountState> premine = new HashMap<>();

    private static byte[] zeroHash256 = new byte[32];
    private static byte[] zeroHash160 = new byte[20];
    private static byte[] zeroHash2048 = new byte[256];

    public static byte[] PARENT_HASH = zeroHash256;
    public static byte[] UNCLES_HASH = EMPTY_LIST_HASH;
    public static byte[] COINBASE = zeroHash160;
    public static byte[] LOG_BLOOM = zeroHash2048;
    public static byte[] DIFFICULTY = BigInteger.valueOf(2).pow(17).toByteArray();
    public static long NUMBER = 0;
    public static long GAS_LIMIT = 0x2FEFD8;
    public static long GAS_USED = 0;
    public static long TIMESTAMP = 0;
    public static byte[] EXTRA_DATA = new byte[0];
    public static byte[] MIX_HASH = zeroHash256;
    public static byte[] NONCE = Hex.decode("000000000000002A");

    private static Block instance;

    private Genesis() {
        super(PARENT_HASH, UNCLES_HASH, COINBASE, LOG_BLOOM, DIFFICULTY,
                NUMBER, GAS_LIMIT, GAS_USED, TIMESTAMP,
                EXTRA_DATA, MIX_HASH, NONCE, null, null);

        // The proof-of-concept series include a development pre-mine, making the state root hash
        // some value stateRoot. The latest documentation should be consulted for the value of the state root.
        Trie state = parseGenesis();
        setStateRoot(state.getRootHash());
    }


    private Trie parseGenesis() {
        JSONParser parser = new JSONParser();
        JSONObject genesisMap = null;
        try {
            genesisMap = (JSONObject) parser.parse(GENESIS_JSON);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Set<String> keys = genesisMap.keySet();

        Trie state = new SecureTrie(null);

        for (String key : keys) {

            JSONObject val = (JSONObject) genesisMap.get(key);
            String denom = (String) val.keySet().toArray()[0];
            String value = (String) val.values().toArray()[0];

            BigInteger wei = Denomination.valueOf(denom.toUpperCase()).value().
                    multiply(new BigInteger(value));

            AccountState acctState = new AccountState(BigInteger.ZERO, wei);
            byte[] keyB = Hex.decode(key);
            state.update(keyB, acctState.getEncoded());
            premine.put(wrap(keyB), acctState);
        }

        return state;
    }

    public static Block getInstance() {
        if (instance == null) {
            instance = new Genesis();
        }
        return instance;
    }


    public Map<ByteArrayWrapper, AccountState> getPremine() {
        return premine;
    }


}
