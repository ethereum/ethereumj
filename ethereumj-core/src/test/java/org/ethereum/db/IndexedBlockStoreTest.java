/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.util.FileUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.*;
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
import static org.ethereum.TestUtils.*;
import static org.ethereum.util.ByteUtil.wrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class IndexedBlockStoreTest {

    private static final Logger logger = LoggerFactory.getLogger("test");
    private List<Block> blocks = new ArrayList<>();
    private BigInteger cumDifficulty = ZERO;

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

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
        indexedBlockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

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
        cache.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

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
        cache.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        // TODO cache removed, remove this test?

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

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
        SystemProperties.getDefault().setDataBaseDir(testDir);

        LevelDbDataSource indexDB = new LevelDbDataSource("index");
        indexDB.init();

        DbSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(indexDB, blocksDB);


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
        indexDB.close();


        // testing after: REOPEN

        indexDB = new LevelDbDataSource("index");
        indexDB.init();

        blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(indexDB, blocksDB);

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
        indexDB.close();
        FileUtil.recursiveDelete(testDir);
    }

    @Test // cache + leveldb + mapdb, save part to disk part to cache, and check it exist
    public void test5() throws IOException {

        BigInteger bi = new BigInteger(32, new Random());
        String testDir = "test_db_" + bi;
        SystemProperties.getDefault().setDataBaseDir(testDir);

        LevelDbDataSource indexDB = new LevelDbDataSource("index");
        indexDB.init();

        DbSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        try {

            IndexedBlockStore cache = new IndexedBlockStore();
            cache.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

            IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB);


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
            indexDB.close();

            // testing after: REOPEN

            indexDB = new LevelDbDataSource("index");
            indexDB.init();

            blocksDB = new LevelDbDataSource("blocks");
            blocksDB.init();

            indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB);

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
            indexDB.close();
            FileUtil.recursiveDelete(testDir);
        }

    }

    @Test // cache + leveldb + mapdb, multi branch, total difficulty test
    public void test6() throws IOException {

        BigInteger bi = new BigInteger(32, new Random());
        String testDir = "test_db_" + bi;
        SystemProperties.getDefault().setDataBaseDir(testDir);

        DbSource indexDB = new LevelDbDataSource("index");
        indexDB.init();

        DbSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        try {

            IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB);

            List<Block> bestLine = getRandomChain(Genesis.getInstance().getHash(), 1, 100);

            indexedBlockStore.saveBlock(Genesis.getInstance(), Genesis.getInstance().getCumulativeDifficulty(), true);

            for (int i = 0; i < bestLine.size(); ++i){

                BigInteger td = indexedBlockStore.getTotalDifficulty();
                Block newBlock = bestLine.get(i);
                td = td.add(newBlock.getCumulativeDifficulty());

                indexedBlockStore.saveBlock(newBlock, td, true);
            }

            byte[] forkParentHash = bestLine.get(60).getHash();
            long forkParentNumber = bestLine.get(60).getNumber();
            List<Block> forkLine = getRandomChain(forkParentHash, forkParentNumber + 1, 50);


            for (int i = 0; i < forkLine.size(); ++i){

                Block newBlock = forkLine.get(i);
                Block parentBlock = indexedBlockStore.getBlockByHash(newBlock.getParentHash());
                BigInteger td = indexedBlockStore.getTotalDifficultyForHash(parentBlock.getHash());

                td = td.add(newBlock.getCumulativeDifficulty());
                indexedBlockStore.saveBlock(newBlock, td, false);
            }


            // calc all TDs
            Map<ByteArrayWrapper, BigInteger> tDiffs = new HashMap<>();
            BigInteger td = Genesis.getInstance().getCumulativeDifficulty();
            for (Block block : bestLine){
                td = td.add(block.getCumulativeDifficulty());
                tDiffs.put(wrap(block.getHash()), td);
            }

            Map<ByteArrayWrapper, BigInteger> tForkDiffs = new HashMap<>();
            Block block = forkLine.get(0);
            td = tDiffs.get(wrap(block.getParentHash()));
            for (Block currBlock : forkLine){
                td = td.add(currBlock.getCumulativeDifficulty());
                tForkDiffs.put(wrap(currBlock.getHash()), td);
            }


            // Assert tds on bestLine
            for ( ByteArrayWrapper hash :  tDiffs.keySet()){
                BigInteger currTD = tDiffs.get(hash);
                BigInteger checkTd =  indexedBlockStore.getTotalDifficultyForHash(hash.getData());
                assertEquals(checkTd, currTD);
            }

            // Assert tds on forkLine
            for ( ByteArrayWrapper hash :  tForkDiffs.keySet()){
                BigInteger currTD = tForkDiffs.get(hash);
                BigInteger checkTd =  indexedBlockStore.getTotalDifficultyForHash(hash.getData());
                assertEquals(checkTd, currTD);
            }

            indexedBlockStore.flush();

            // Assert tds on bestLine
            for ( ByteArrayWrapper hash :  tDiffs.keySet()){
                BigInteger currTD = tDiffs.get(hash);
                BigInteger checkTd =  indexedBlockStore.getTotalDifficultyForHash(hash.getData());
                assertEquals(checkTd, currTD);
            }

            // check total difficulty
            BigInteger totalDifficulty  = indexedBlockStore.getTotalDifficulty();
            Block bestBlock = bestLine.get(bestLine.size() - 1);
            BigInteger totalDifficulty_ = tDiffs.get(wrap(bestBlock.getHash()));

            assertEquals(totalDifficulty_, totalDifficulty);

            // Assert tds on forkLine
            for ( ByteArrayWrapper hash :  tForkDiffs.keySet()){
                BigInteger currTD = tForkDiffs.get(hash);
                BigInteger checkTd =  indexedBlockStore.getTotalDifficultyForHash(hash.getData());
                assertEquals(checkTd, currTD);
            }


        } finally {
            blocksDB.close();
            indexDB.close();
            FileUtil.recursiveDelete(testDir);
        }
    }

    @Test // cache + leveldb + mapdb, multi branch, total re-branch test
    public void test7() throws IOException {

        BigInteger bi = new BigInteger(32, new Random());
        String testDir = "test_db_" + bi;
        SystemProperties.getDefault().setDataBaseDir(testDir);

        DbSource indexDB = new LevelDbDataSource("index");
        indexDB.init();

        DbSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        try {

            IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB);

            List<Block> bestLine = getRandomChain(Genesis.getInstance().getHash(), 1, 100);

            indexedBlockStore.saveBlock(Genesis.getInstance(), Genesis.getInstance().getCumulativeDifficulty(), true);

            for (int i = 0; i < bestLine.size(); ++i){

                BigInteger td = indexedBlockStore.getTotalDifficulty();
                Block newBlock = bestLine.get(i);
                td = td.add(newBlock.getCumulativeDifficulty());

                indexedBlockStore.saveBlock(newBlock, td, true);
            }

            byte[] forkParentHash = bestLine.get(60).getHash();
            long forkParentNumber = bestLine.get(60).getNumber();
            List<Block> forkLine = getRandomChain(forkParentHash, forkParentNumber + 1, 50);


            for (int i = 0; i < forkLine.size(); ++i){

                Block newBlock = forkLine.get(i);
                Block parentBlock = indexedBlockStore.getBlockByHash(newBlock.getParentHash());
                BigInteger td = indexedBlockStore.getTotalDifficultyForHash(parentBlock.getHash());

                td = td.add(newBlock.getCumulativeDifficulty());
                indexedBlockStore.saveBlock(newBlock, td, false);
            }


            Block bestBlock = bestLine.get(bestLine.size() - 1);
            Block forkBlock = forkLine.get(forkLine.size() - 1);

            // check total difficulty
            BigInteger totalDifficulty  = indexedBlockStore.getTotalDifficulty();
            BigInteger totalDifficulty_ = indexedBlockStore.getTotalDifficultyForHash( bestBlock.getHash() );
            assertEquals(totalDifficulty_, totalDifficulty);


            indexedBlockStore.reBranch(forkBlock);

            // check total difficulty
            totalDifficulty  = indexedBlockStore.getTotalDifficulty();
            totalDifficulty_ = indexedBlockStore.getTotalDifficultyForHash( forkBlock.getHash() );
            assertEquals(totalDifficulty_, totalDifficulty);


        } finally {
            blocksDB.close();
            indexDB.close();
            FileUtil.recursiveDelete(testDir);
        }
    }

    @Test // cache + leveldb + mapdb, multi branch, total re-branch test
    public void test8() throws IOException {

        BigInteger bi = new BigInteger(32, new Random());
        String testDir = "test_db_" + bi;
        SystemProperties.getDefault().setDataBaseDir(testDir);

        DbSource indexDB = new LevelDbDataSource("index");
        indexDB.init();

        DbSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();

        try {
            IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
            indexedBlockStore.init(indexDB, blocksDB);

            List<Block> bestLine = getRandomChain(Genesis.getInstance().getHash(), 1, 100);

            indexedBlockStore.saveBlock(Genesis.getInstance(), Genesis.getInstance().getCumulativeDifficulty(), true);

            for (int i = 0; i < bestLine.size(); ++i){

                BigInteger td = indexedBlockStore.getTotalDifficulty();
                Block newBlock = bestLine.get(i);
                td = td.add(newBlock.getCumulativeDifficulty());

                indexedBlockStore.saveBlock(newBlock, td, true);
            }

            byte[] forkParentHash = bestLine.get(60).getHash();
            long forkParentNumber = bestLine.get(60).getNumber();
            List<Block> forkLine = getRandomChain(forkParentHash, forkParentNumber + 1, 10);


            for (int i = 0; i < forkLine.size(); ++i){

                Block newBlock = forkLine.get(i);
                Block parentBlock = indexedBlockStore.getBlockByHash(newBlock.getParentHash());
                BigInteger td = indexedBlockStore.getTotalDifficultyForHash(parentBlock.getHash());

                td = td.add(newBlock.getCumulativeDifficulty());
                indexedBlockStore.saveBlock(newBlock, td, false);
            }


            Block bestBlock = bestLine.get(bestLine.size() - 1);
            Block forkBlock = forkLine.get(forkLine.size() - 1);

            assertTrue( indexedBlockStore.getBestBlock().getNumber() == 100);

            // check total difficulty
            BigInteger totalDifficulty  = indexedBlockStore.getTotalDifficulty();
            BigInteger totalDifficulty_ = indexedBlockStore.getTotalDifficultyForHash(bestBlock.getHash());
            assertEquals(totalDifficulty_, totalDifficulty);


            indexedBlockStore.reBranch(forkBlock);

            assertTrue( indexedBlockStore.getBestBlock().getNumber() == 71);

            // check total difficulty
            totalDifficulty  = indexedBlockStore.getTotalDifficulty();
            totalDifficulty_ = indexedBlockStore.getTotalDifficultyForHash( forkBlock.getHash() );
            assertEquals(totalDifficulty_, totalDifficulty);


            // Assert that all fork moved to the main line
            for (Block currBlock : forkLine){

                Long number = currBlock.getNumber();
                Block chainBlock = indexedBlockStore.getChainBlockByNumber(number);
                assertEquals(currBlock.getShortHash(), chainBlock.getShortHash());
            }


            // Assert that all fork moved to the main line
            // re-branch back to previous line and assert that
            // all the block really moved
            bestBlock = bestLine.get(bestLine.size() - 1);
            indexedBlockStore.reBranch(bestBlock);
            for (Block currBlock : bestLine){

                Long number = currBlock.getNumber();
                Block chainBlock = indexedBlockStore.getChainBlockByNumber(number);
                assertEquals(currBlock.getShortHash(), chainBlock.getShortHash());
            }


        } finally {
            blocksDB.close();
            indexDB.close();
            FileUtil.recursiveDelete(testDir);
        }
    }

    @Test // test index merging during the flush
    public void test9() {

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

        // blocks with the same block number
        Block block1 = new Block(Hex.decode("f90202f901fda0ad0d51e8d64c364a7b77ef2fe252f3f4df0940c7cfa69cedc1fbd6ea66894936a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479414a3bc0f103706650a19c5d24e5c4cf1ea5af78ea0e0580f4fdd1e3ae8346efaa6b1018605361f6e2fb058580e31414c8cbf5b0d49a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008605065cf2c43a8303e52e832fefd8808455fcbe1b80a017247341fd5d2f1d384682fea9302065a95dbd3e4f8260dde88a386f3cb95be3880f3fc8d5e0c87378c0c0"));
        Block block2 = new Block(Hex.decode("f90218f90213a0c63fc3626abc6f6ba695064e973126cccc6fd513d4f53485e11794a8855e8b2ba01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347941dcb8d1f0fcc8cbc8c2d76528e877f915e299fbea0ccb2ed2a8c585409fe5530d36320bc8c1406454b32a9e419e890ea49489e534aa056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008605079eb238d88303e52e832fefd8808455fcbe2596d583010103844765746885676f312e35856c696e7578a0a673a429161eb32e6d0887b2bce2b12b1edd6e4b4cf55371853cba13d57118bd88d44d3609c7e203c7c0c0"));

        indexedBlockStore.saveBlock(block1, block1.getCumulativeDifficulty(), true);
        indexedBlockStore.flush();

        indexedBlockStore.saveBlock(block2, block2.getCumulativeDifficulty(), true);
        indexedBlockStore.flush();

        assertEquals(block1.getCumulativeDifficulty(), indexedBlockStore.getTotalDifficultyForHash(block1.getHash()));
        assertEquals(block2.getCumulativeDifficulty(), indexedBlockStore.getTotalDifficultyForHash(block2.getHash()));
    }

    @Test
    public void myTest() throws Exception {
        // check that IndexedStore rebranch changes are persisted
        StandaloneBlockchain bc = new StandaloneBlockchain().withGasPrice(1);
        IndexedBlockStore ibs = (IndexedBlockStore) bc.getBlockchain().getBlockStore();

        Block b1 = bc.createBlock();
        Block b2 = bc.createBlock();
        Block b2_ = bc.createForkBlock(b1);
        Assert.assertTrue(bc.getBlockchain().getBestBlock().isEqual(b2));
        Block b3_ = bc.createForkBlock(b2_);
        Assert.assertTrue(bc.getBlockchain().getBestBlock().isEqual(b3_));
        Block sb2 = bc.getBlockchain().getBlockStore().getChainBlockByNumber(2);
        Block sb3 = bc.getBlockchain().getBlockStore().getChainBlockByNumber(3);
        Assert.assertTrue(sb2.isEqual(b2_));
        Assert.assertTrue(sb3.isEqual(b3_));
        Block b4_ = bc.createBlock();
        bc.getBlockchain().flush();

        IndexedBlockStore ibs1 = new IndexedBlockStore();
        ibs1.init(ibs.indexDS, ibs.blocksDS);

        sb2 = ibs1.getChainBlockByNumber(2);
        sb3 = ibs1.getChainBlockByNumber(3);
        Block sb4 = ibs1.getChainBlockByNumber(4);
        Assert.assertTrue(sb2.isEqual(b2_));
        Assert.assertTrue(sb3.isEqual(b3_));
        Assert.assertTrue(sb4.isEqual(b4_));
    }


// todo: test this

//    public byte[] getBlockHashByNumber(long blockNumber)

}
