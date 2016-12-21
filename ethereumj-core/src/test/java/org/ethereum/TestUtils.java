package org.ethereum;

import org.ethereum.core.Block;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.vm.DataWord;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.spongycastle.util.BigIntegers;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.crypto.HashUtil.randomHash;
import static org.ethereum.db.IndexedBlockStore.BLOCK_INFO_SERIALIZER;

public final class TestUtils {

    private TestUtils() {
    }

    public static byte[] randomBytes(int length) {
        byte[] result = new byte[length];
        new Random().nextBytes(result);
        return result;
    }

    public static DataWord randomDataWord() {
        return new DataWord(randomBytes(32));
    }

    public static byte[] randomAddress() {
        return randomBytes(20);
    }

    public static Map<Long, List<IndexedBlockStore.BlockInfo>> createIndexMap(DB db){

//        Map<Long, List<IndexedBlockStore.BlockInfo>> index = db.hashMapCreate("index")
//                .keySerializer(Serializer.LONG)
//                .valueSerializer(BLOCK_INFO_SERIALIZER)
//                .makeOrGet();
//
//        return index;
        return null;
    }

//    public static DB createMapDB(String testDBDir){
//
//        String blocksIndexFile = testDBDir + "/blocks/index";
//        File dbFile = new File(blocksIndexFile);
//        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
//
//        DB db = DBMaker.fileDB(dbFile)
//                .transactionDisable()
//                .closeOnJvmShutdown()
//                .make();
//
//
//        return db;
//    }

    public static List<Block> getRandomChain(byte[] startParentHash, long startNumber, long length){

        List<Block> result = new ArrayList<>();

        byte[] lastHash = startParentHash;
        long lastIndex = startNumber;


        for (int i = 0; i < length; ++i){

            byte[] difficulty = BigIntegers.asUnsignedByteArray(new BigInteger(8, new Random()));
            byte[] newHash = randomHash();

            Block block = new Block(lastHash, newHash,  null, null, difficulty, lastIndex, new byte[] {0}, 0, 0, null, null,
                    null, null, EMPTY_TRIE_HASH, randomHash(), null, null);

            ++lastIndex;
            lastHash = block.getHash();
            result.add(block);
        }

        return result;
    }

    // Generates chain with alternative sub-chains, maxHeight blocks on each level
    public static List<Block> getRandomAltChain(byte[] startParentHash, long startNumber, long length, int maxHeight){

        List<Block> result = new ArrayList<>();

        List<byte[]> lastHashes = new ArrayList<>();
        lastHashes.add(startParentHash);
        long lastIndex = startNumber;
        Random rnd = new Random();

        for (int i = 0; i < length; ++i){
            List<byte[]> currentHashes = new ArrayList<>();
            int curMaxHeight = maxHeight;
            if (i == 0) curMaxHeight = 1;

            for (int j = 0; j < curMaxHeight; ++j){
                byte[] parentHash = lastHashes.get(rnd.nextInt(lastHashes.size()));
                byte[] difficulty = BigIntegers.asUnsignedByteArray(new BigInteger(8, new Random()));
                byte[] newHash = randomHash();

                Block block = new Block(parentHash, newHash, null, null, difficulty, lastIndex, new byte[]{0}, 0, 0, null, null,
                        null, null, EMPTY_TRIE_HASH, randomHash(), null, null);
                currentHashes.add(block.getHash());
                result.add(block);
            }

            ++lastIndex;
            lastHashes.clear();
            lastHashes.addAll(currentHashes);
        }

        return result;
    }
}
