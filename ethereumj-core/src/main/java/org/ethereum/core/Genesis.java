package org.ethereum.core;

import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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

    private Map<ByteArrayWrapper, AccountState> premine = new HashMap<>();

    public  static byte[] ZERO_HASH_2048 = new byte[256];
    public static byte[] DIFFICULTY = BigInteger.valueOf(2).pow(17).toByteArray();
    public static long NUMBER = 0;

    private static Block instance;

    public Genesis(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
                   byte[] difficulty, long number, long gasLimit,
                   long gasUsed, long timestamp,
                   byte[] extraData, byte[] mixHash, byte[] nonce){
        super(parentHash, unclesHash, coinbase, logsBloom, difficulty,
                number, gasLimit, gasUsed, timestamp, extraData,
                mixHash, nonce, null, null);
    }

    public static Block getInstance() {
        if (instance == null) {
            instance = GenesisLoader.loadGenesis();
        }
        return instance;
    }


    public Map<ByteArrayWrapper, AccountState> getPremine() {
        return premine;
    }

    public void setPremine(Map<ByteArrayWrapper, AccountState> premine) {
        this.premine = premine;
    }
}
