package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.util.FileUtil;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static java.math.BigInteger.ZERO;
import static org.ethereum.TestUtils.createIndexMap;
import static org.ethereum.TestUtils.createMapDB;
import static org.junit.Assert.assertEquals;


public class IndexedBlockStoreTest {

    private static final Logger logger = LoggerFactory.getLogger("test");
    private List<Block> blocks = new ArrayList<>();
    private BigInteger cumDifficulty = ZERO;

    @Before
    public void setup() throws URISyntaxException, IOException {

        URL scenario1 = ClassLoader
                .getSystemResource("blockstore/load.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        Block genesis = Genesis.getInstance();
        blocks.add(genesis);
        cumDifficulty = cumDifficulty.add(genesis.getCumulativeDifficulty());

        for (String blockRLP : strData) {

            Block block = new Block(
                    Hex.decode(blockRLP));

            if (block.getNumber() % 1000 == 0)
                logger.info("adding block.hash: [{}] block.number: [{}]",
                        block.getShortHash(),
                        block.getNumber());

            blocks.add(block);
            cumDifficulty = cumDifficulty.add(block.getCumulativeDifficulty());
        }

        logger.info("total difficulty: {}", cumDifficulty);
        logger.info("total blocks loaded: {}", blocks.size());
    }


    @Test // no cache, save some load, and check it exist
    public void test1(){

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null);

        BigInteger cummDiff = BigInteger.ZERO;
        for (Block block : blocks){
            cummDiff = cummDiff.add( block.getCumulativeDifficulty() );
            indexedBlockStore.saveBlock(block, cummDiff, true);
        }

        //  testing:   getTotalDifficulty()
        //  testing:   getMaxNumber()

        long bestIndex = blocks.get(blocks.size() - 1).getNumber();
        assertEquals(bestIndex, indexedBlockStore.getMaxNumber());
        assertEquals(cumDifficulty, indexedBlockStore.getTotalDifficulty());

        //  testing:  getBlockByHash(byte[])

        Block block  = blocks.get(50);
        Block block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getBlockByHash(Hex.decode("00112233"));
        assertEquals(null, block_);

        //  testing:  getChainBlockByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getChainBlockByNumber(10000);
        assertEquals(null, block_);

        //  testing: getBlocksByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        int blocksNum  = indexedBlockStore.getBlocksByNumber(10000).size();
        assertEquals(0, blocksNum);

        //  testing: getListHashesEndWith(byte[], long)

        block  = blocks.get(8003);
        List<byte[]> hashList =  indexedBlockStore.getListHashesEndWith(block.getHash(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(8003 - i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

        //  testing: getListHashesStartWith(long, long)

        block  = blocks.get(7003);
        hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(7003 + i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

    }

    @Test // predefined cache, save some load, and check it exist
    public void test2(){

        IndexedBlockStore cache = new IndexedBlockStore();
        cache.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null);

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), cache);

        BigInteger cummDiff = BigInteger.ZERO;
        for (Block block : blocks){
            cummDiff = cummDiff.add( block.getCumulativeDifficulty() );
            indexedBlockStore.saveBlock(block, cummDiff, true);
        }

        //  testing:   getTotalDifficulty()
        //  testing:   getMaxNumber()

        long bestIndex = blocks.get(blocks.size() - 1).getNumber();
        assertEquals(bestIndex, indexedBlockStore.getMaxNumber());
        assertEquals(cumDifficulty, indexedBlockStore.getTotalDifficulty());

        //  testing:  getBlockByHash(byte[])

        Block block  = blocks.get(50);
        Block block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getBlockByHash(Hex.decode("00112233"));
        assertEquals(null, block_);

        //  testing:  getChainBlockByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getChainBlockByNumber(10000);
        assertEquals(null, block_);

        //  testing: getBlocksByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        int blocksNum  = indexedBlockStore.getBlocksByNumber(10000).size();
        assertEquals(0, blocksNum);

        //  testing: getListHashesEndWith(byte[], long)

        block  = blocks.get(8003);
        List<byte[]> hashList =  indexedBlockStore.getListHashesEndWith(block.getHash(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(8003 - i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

        //  testing: getListHashesStartWith(long, long)

        block  = blocks.get(7003);
        hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(7003 + i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

    }

    @Test // predefined cache loaded and flushed, check it exist
    public void test3(){

        IndexedBlockStore cache = new IndexedBlockStore();
        cache.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null);

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), cache);

        BigInteger cummDiff = BigInteger.ZERO;
        for (Block block : blocks){
            cummDiff = cummDiff.add( block.getCumulativeDifficulty() );
            indexedBlockStore.saveBlock(block, cummDiff, true);
        }

        indexedBlockStore.flush();

        //  testing:   getTotalDifficulty()
        //  testing:   getMaxNumber()

        long bestIndex = blocks.get(blocks.size() - 1).getNumber();
        assertEquals(bestIndex, indexedBlockStore.getMaxNumber());
        assertEquals(cumDifficulty, indexedBlockStore.getTotalDifficulty());

        //  testing:  getBlockByHash(byte[])

        Block block  = blocks.get(50);
        Block block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getBlockByHash(Hex.decode("00112233"));
        assertEquals(null, block_);

        //  testing:  getChainBlockByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getChainBlockByNumber(10000);
        assertEquals(null, block_);

        //  testing: getBlocksByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        int blocksNum  = indexedBlockStore.getBlocksByNumber(10000).size();
        assertEquals(0, blocksNum);

        //  testing: getListHashesEndWith(byte[], long)

        block  = blocks.get(8003);
        List<byte[]> hashList =  indexedBlockStore.getListHashesEndWith(block.getHash(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(8003 - i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

        //  testing: getListHashesStartWith(long, long)

        block  = blocks.get(7003);
        hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(7003 + i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

    }



    @Test // cache + leveldb + mapdb, save some load, flush to disk, and check it exist
    public void test4() throws IOException {

        BigInteger bi = new BigInteger(32, new Random());
        String testDir = "test_db_" + bi;
        SystemProperties.CONFIG.setDataBaseDir(testDir);

        DB db = createMapDB(testDir);
        Map<Long, List<IndexedBlockStore.BlockInfo>> indexDB = createIndexMap(db);

        KeyValueDataSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(indexDB, blocksDB, null);


        BigInteger cummDiff = BigInteger.ZERO;
        for (Block block : blocks){
            cummDiff = cummDiff.add( block.getCumulativeDifficulty() );
            indexedBlockStore.saveBlock(block, cummDiff, true);
        }

        //  testing:   getTotalDifficulty()
        //  testing:   getMaxNumber()

        long bestIndex = blocks.get(blocks.size() - 1).getNumber();
        assertEquals(bestIndex, indexedBlockStore.getMaxNumber());
        assertEquals(cumDifficulty, indexedBlockStore.getTotalDifficulty());

        //  testing:  getBlockByHash(byte[])

        Block block  = blocks.get(50);
        Block block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlockByHash(block.getHash());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getBlockByHash(Hex.decode("00112233"));
        assertEquals(null, block_);

        //  testing:  getChainBlockByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
        assertEquals(block.getNumber(), block_.getNumber());

        block_  = indexedBlockStore.getChainBlockByNumber(10000);
        assertEquals(null, block_);

        //  testing: getBlocksByNumber(long)

        block  = blocks.get(50);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(150);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(0);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        block  = blocks.get(8003);
        block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
        assertEquals(block.getNumber(), block_.getNumber());

        int blocksNum  = indexedBlockStore.getBlocksByNumber(10000).size();
        assertEquals(0, blocksNum);

        //  testing: getListHashesEndWith(byte[], long)

        block  = blocks.get(8003);
        List<byte[]> hashList =  indexedBlockStore.getListHashesEndWith(block.getHash(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(8003 - i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

        //  testing: getListHashesStartWith(long, long)

        block  = blocks.get(7003);
        hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(7003 + i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

        blocksDB.close();
        db.close();


        // testing after: REOPEN

        db = createMapDB(testDir);
        indexDB = createIndexMap(db);

        blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(indexDB, blocksDB, null);

        //  testing: getListHashesStartWith(long, long)

        block  = blocks.get(7003);
        hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
        for (int i = 0; i < 100; ++i){
            block  = blocks.get(7003 + i);
            String hash  = Hex.toHexString(hashList.get(i));
            String hash_ = Hex.toHexString( block.getHash() );
            assertEquals(hash_, hash);
        }

        blocksDB.close();
        db.close();
        FileUtil.recursiveDelete(testDir);
    }

    @Test // cache + leveldb + mapdb, save part to disk part to cache, and check it exist
    public void test5() throws IOException {

        BigInteger bi = new BigInteger(32, new Random());
        String testDir = "test_db_" + bi;
        SystemProperties.CONFIG.setDataBaseDir(testDir);

        DB db = createMapDB(testDir);
        Map<Long, List<IndexedBlockStore.BlockInfo>> indexDB = createIndexMap(db);

        KeyValueDataSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        try {

            IndexedBlockStore cache = new IndexedBlockStore();
            cache.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null);

            IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB, cache);


            BigInteger cummDiff = BigInteger.ZERO;
            int preloadSize = blocks.size() / 2;
            for (int i = 0; i < preloadSize; ++i){
                Block block = blocks.get(i);
                cummDiff = cummDiff.add( block.getCumulativeDifficulty() );
                indexedBlockStore.saveBlock(block, cummDiff, true);
            }

            indexedBlockStore.flush();

            for (int i = preloadSize; i < blocks.size(); ++i){
                Block block = blocks.get(i);
                cummDiff = cummDiff.add( block.getCumulativeDifficulty() );
                indexedBlockStore.saveBlock(block, cummDiff, true);
            }

            //  testing:   getTotalDifficulty()
            //  testing:   getMaxNumber()

            long bestIndex = blocks.get(blocks.size() - 1).getNumber();
            assertEquals(bestIndex, indexedBlockStore.getMaxNumber());
            assertEquals(cumDifficulty, indexedBlockStore.getTotalDifficulty());

            //  testing:  getBlockByHash(byte[])

            Block block  = blocks.get(50);
            Block block_ = indexedBlockStore.getBlockByHash(block.getHash());
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(150);
            block_ = indexedBlockStore.getBlockByHash(block.getHash());
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(0);
            block_ = indexedBlockStore.getBlockByHash(block.getHash());
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(8003);
            block_ = indexedBlockStore.getBlockByHash(block.getHash());
            assertEquals(block.getNumber(), block_.getNumber());

            block_  = indexedBlockStore.getBlockByHash(Hex.decode("00112233"));
            assertEquals(null, block_);

            //  testing:  getChainBlockByNumber(long)

            block  = blocks.get(50);
            block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(150);
            block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(0);
            block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(8003);
            block_ = indexedBlockStore.getChainBlockByNumber(block.getNumber());
            assertEquals(block.getNumber(), block_.getNumber());

            block_  = indexedBlockStore.getChainBlockByNumber(10000);
            assertEquals(null, block_);

            //  testing: getBlocksByNumber(long)

            block  = blocks.get(50);
            block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(150);
            block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(0);
            block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
            assertEquals(block.getNumber(), block_.getNumber());

            block  = blocks.get(8003);
            block_ = indexedBlockStore.getBlocksByNumber(block.getNumber()).get(0);
            assertEquals(block.getNumber(), block_.getNumber());

            int blocksNum  = indexedBlockStore.getBlocksByNumber(10000).size();
            assertEquals(0, blocksNum);

            //  testing: getListHashesEndWith(byte[], long)

            block  = blocks.get(8003);
            List<byte[]> hashList =  indexedBlockStore.getListHashesEndWith(block.getHash(), 100);
            for (int i = 0; i < 100; ++i){
                block  = blocks.get(8003 - i);
                String hash  = Hex.toHexString(hashList.get(i));
                String hash_ = Hex.toHexString( block.getHash() );
                assertEquals(hash_, hash);
            }

            //  testing: getListHashesStartWith(long, long)

            block  = blocks.get(7003);
            hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
            for (int i = 0; i < 100; ++i){
                block  = blocks.get(7003 + i);
                String hash  = Hex.toHexString(hashList.get(i));
                String hash_ = Hex.toHexString( block.getHash() );
                assertEquals(hash_, hash);
            }


            indexedBlockStore.flush();
            blocksDB.close();
            db.close();
            // testing after: REOPEN

            db = createMapDB(testDir);
            indexDB = createIndexMap(db);

            blocksDB = new LevelDbDataSource("blocks");
            blocksDB.init();

            indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB, null);


            //  testing: getListHashesStartWith(long, long)

            block  = blocks.get(7003);
            hashList =  indexedBlockStore.getListHashesStartWith(block.getNumber(), 100);
            for (int i = 0; i < 100; ++i){
                block  = blocks.get(7003 + i);
                String hash  = Hex.toHexString(hashList.get(i));
                String hash_ = Hex.toHexString( block.getHash() );
                assertEquals(hash_, hash);
            }
        } finally {
            blocksDB.close();
            db.close();
            FileUtil.recursiveDelete(testDir);
        }

    }

// todo: test this
//    public Block getBestBlock()
//    public byte[] getBlockHashByNumber(long blockNumber)

}
