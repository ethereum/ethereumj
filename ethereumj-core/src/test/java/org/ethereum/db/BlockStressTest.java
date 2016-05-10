package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockWrapper;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.datasource.mapdb.Serializers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Mikhail Kalinin
 * @since 10.07.2015
 */
public class BlockStressTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private static final String TEST_DB_DIR = "test_db/block_stress/";
    private static final String BLOCK_SOURCE = "block_src";
    private Map<byte[], Block> blockSource;
    private DB blockSourceDB;
    private MapDBFactory mapDBFactory;
    private byte[] nodeId = new byte[64];

    private SystemProperties config;

    @Before
    public void setup() {
        config = SystemProperties.getDefault();
        config.setDataBaseDir(TEST_DB_DIR);

        mapDBFactory = new MapDBFactoryImpl(config);
        blockSourceDB = mapDBFactory.createDB(BLOCK_SOURCE);
        blockSource = blockSourceDB.hashMapCreate(BLOCK_SOURCE)
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializers.BLOCK)
                .makeOrGet();

        Random rnd = new Random(System.currentTimeMillis());
        rnd.nextBytes(nodeId);
    }

    @After
    public void cleanup() {
        blockSourceDB.close();
    }

    @Ignore("long stress test")
    @Test // loads blocks from file and store them into disk DB
    public void prepareData() throws URISyntaxException, IOException {
        URL dataURL = ClassLoader.getSystemResource("blockstore/big_data.dmp");

        File file = new File(dataURL.toURI());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String blockRLP;
        while(null != (blockRLP = reader.readLine())) {
            Block block = new Block(
                    Hex.decode(blockRLP)
            );
            blockSource.put(block.getHash(), block);

            if (block.getNumber() % 10000 == 0)
                logger.info(
                        "adding block.hash: [{}] block.number: [{}]",
                        block.getShortHash(),
                        block.getNumber()
                );
        }
        logger.info("total blocks loaded: {}", blockSource.size());
    }

    @Ignore("long stress test")
    @Test // interesting how much time will take reading block by its hash
    public void testBlockSource() {
        long start, end;

        start = System.currentTimeMillis();
        Set<byte[]> hashes = blockSource.keySet();
        end = System.currentTimeMillis();

        logger.info("getKeys: {} ms", end - start);

        start = System.currentTimeMillis();
        int counter = 0;
        for(byte[] hash : hashes) {
            blockSource.get(hash);
            if(++counter % 10000 == 0) {
                logger.info("reading: {} done from {}", counter, hashes.size());
            }
        }
        end = System.currentTimeMillis();

        logger.info("reading: total time {} ms", end - start);
        logger.info("reading: time per block {} ms", (end - start) / (float)hashes.size());
    }

    @Ignore("long stress test")
    @Test // benchmarking block queue, writing and reading
    public void testBlockQueue() {
        long start, end;

        BlockQueue blockQueue = new BlockQueueImpl(config);
        ((BlockQueueImpl)blockQueue).setMapDBFactory(mapDBFactory);
        blockQueue.open();

        Set<byte[]> hashes = blockSource.keySet();

        start = System.currentTimeMillis();
        int counter = 0;
        for(byte[] hash : hashes) {
            Block block = blockSource.get(hash);
            blockQueue.add(new BlockWrapper(block, nodeId));
            if(++counter % 10000 == 0) {
                logger.info("writing: {} done from {}", counter, hashes.size());
            }
        }
        end = System.currentTimeMillis();

        logger.info("writing: total time {} ms", end - start);
        logger.info("writing: time per block {} ms", (end - start) / (float)hashes.size());

        start = System.currentTimeMillis();
        counter = 0;
        while(null != blockQueue.poll()) {
            if(++counter % 10000 == 0) {
                logger.info("reading: {} done from {}", counter, hashes.size());
            }
        }
        end = System.currentTimeMillis();

        logger.info("reading: total time {} ms", end - start);
        logger.info("reading: time per block {} ms", (end - start) / (float)hashes.size());
    }

    @Ignore("long stress test")
    @Test // benchmarking block queue writing with multiple threads
    public void testBlockQueueMultithreaded() throws InterruptedException {
        long start, end;
        int threadsCount = 5;

        BlockQueue blockQueue = new BlockQueueImpl(config);
        ((BlockQueueImpl)blockQueue).setMapDBFactory(mapDBFactory);
        blockQueue.open();

        try {
            List<byte[]> hashes = new ArrayList<>(blockSource.keySet());
            List<List<byte[]>> hashBunches = new ArrayList<>(threadsCount);
            int bunchSize = hashes.size() / threadsCount;
            for(int i = 0; i < threadsCount; i++) {
                List<byte[]> bunch = new ArrayList<>(bunchSize);
                for(int k = i * bunchSize; k < (i + 1) * bunchSize; k++) {
                    bunch.add(hashes.get(k));
                }
                if(i == threadsCount - 1) {
                    for(int k = (i + 1) * bunchSize; k < hashes.size(); k++) {
                        bunch.add(hashes.get(k));
                    }
                }
                hashBunches.add(bunch);
            }

            List<Thread> threads = new ArrayList<>();
            for(int i = 0; i < hashBunches.size(); i++) {
                Thread t = new Thread(new BlockQueueWriter(
                        i + 1,
                        hashBunches.get(i),
                        blockQueue
                ));
                t.start();
                threads.add(t);
            }

            start = System.currentTimeMillis();
            for(Thread t : threads) {
                t.join();
            }
            end = System.currentTimeMillis();

            logger.info("writing: total time {} ms", end - start);
            logger.info("writing: time per block {} ms", (end - start) / (float) hashes.size());

            // testing: order and import completeness
            Set<byte[]> importedHashes = new HashSet<>();
            long prevNumber = -1;
            BlockWrapper block;
            while (null != (block = blockQueue.poll())) {
                assertTrue(block.getNumber() > prevNumber);
                prevNumber = block.getNumber();
                importedHashes.add(block.getHash());
            }

            for(byte[] hash : hashes) {
                assertTrue(contains(importedHashes, hash));
            }
            for(byte[] hash : importedHashes) {
                assertTrue(contains(hashes, hash));
            }
        } finally {
            blockQueue.close();
        }
    }

    private boolean contains(Collection<byte[]> hashes, byte[] hash) {
        for(byte[] h : hashes) {
            if(Arrays.equals(h, hash)) {
                return true;
            }
        }
        return false;
    }

    private class BlockQueueWriter implements Runnable {

        private int threadNumber;
        private List<byte[]> hashes;
        private BlockQueue blockQueue;

        public BlockQueueWriter(int threadNumber, List<byte[]> hashes, BlockQueue blockQueue) {
            this.threadNumber = threadNumber;
            this.hashes = hashes;
            this.blockQueue = blockQueue;
        }

        @Override
        public void run() {
            logger.info("writer {}: starting", threadNumber);
            int counter = 0;
            for(byte[] hash : hashes) {
                Block block = blockSource.get(hash);
                blockQueue.add(new BlockWrapper(block, nodeId));
                if(++counter % 10000 == 0) {
                    logger.info("writer {}: {} done from {}", threadNumber, counter, hashes.size());
                }
            }
        }
    }
}
