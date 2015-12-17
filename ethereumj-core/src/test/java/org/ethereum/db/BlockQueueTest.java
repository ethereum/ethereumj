package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockWrapper;
import org.ethereum.core.Genesis;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.util.CollectionUtils;
import org.ethereum.util.FileUtil;
import org.ethereum.util.Functional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.junit.Assert.*;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public class BlockQueueTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private BlockQueue blockQueue;
    private List<Block> blocks = new ArrayList<>();
    private List<byte[]> hashes = new ArrayList<>();
    private String testDb;
    private byte[] nodeId = new byte[64];

    @Before
    public void setup() throws InstantiationException, IllegalAccessException, URISyntaxException, IOException {
        URL scenario1 = ClassLoader
                .getSystemResource("blockstore/light-load.dmp");

        File file = new File(scenario1.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        Block genesis = Genesis.getInstance();
        blocks.add(genesis);

        for (String blockRLP : strData) {
            Block block = new Block(
                    Hex.decode(blockRLP)
            );

            if (block.getNumber() % 10 == 0)
                logger.info("adding block.hash: [{}] block.number: [{}]",
                        block.getShortHash(),
                        block.getNumber());

            blocks.add(block);
            hashes.add(block.getHash());
        }

        logger.info("total blocks loaded: {}", blocks.size());

        BigInteger bi = new BigInteger(32, new Random());
        testDb = "test_db_" + bi;
        CONFIG.setDataBaseDir(testDb);
        CONFIG.setDatabaseReset(false);

        MapDBFactory mapDBFactory = new MapDBFactoryImpl();
        blockQueue = new BlockQueueImpl();
        ((BlockQueueImpl)blockQueue).setMapDBFactory(mapDBFactory);
        blockQueue.open();

        Random rnd = new Random(System.currentTimeMillis());
        rnd.nextBytes(nodeId);
    }

    @After
    public void cleanup() {
        blockQueue.close();
        FileUtil.recursiveDelete(testDb);
    }

    @Test // basic checks
    public void test1() {
        long receivedAt = System.currentTimeMillis();
        long importFailedAt = receivedAt + receivedAt / 2;
        BlockWrapper wrapper = new BlockWrapper(blocks.get(0), true, nodeId);
        wrapper.setReceivedAt(receivedAt);
        wrapper.setImportFailedAt(importFailedAt);
        blockQueue.add(new BlockWrapper(blocks.get(0), nodeId));

        // testing: peek()
        BlockWrapper block = blockQueue.peek();

        assertNotNull(block);
        assertTrue(wrapper.isNewBlock());
        assertEquals(receivedAt, wrapper.getReceivedAt());
        assertEquals(importFailedAt, wrapper.getImportFailedAt());
        assertArrayEquals(nodeId, wrapper.getNodeId());

        // testing: validity of loaded block
        assertArrayEquals(blocks.get(0).getEncoded(), block.getEncoded());

        blockQueue.take();

        // testing: addOrReplaceAll(), close(), open()
        blockQueue.addOrReplaceAll(CollectionUtils.collectList(blocks, new Functional.Function<Block, BlockWrapper>() {
            @Override
            public BlockWrapper apply(Block block) {
                BlockWrapper wrapper = new BlockWrapper(block, nodeId);
                wrapper.setReceivedAt(System.currentTimeMillis());
                return wrapper;
            }
        }));

        blockQueue.close();
        blockQueue.open();

        assertEquals(blocks.size(), blockQueue.size());

        // checking: hashset
        List<byte[]> filtered = blockQueue.filterExisting(hashes);
        assertTrue(filtered.isEmpty());

        // testing: poll()
        long prevNumber = -1;
        for(int i = 0; i < blocks.size(); i++) {
            block = blockQueue.poll();
            assertTrue(block.getNumber() > prevNumber);
            prevNumber = block.getNumber();
        }

        assertNull(blockQueue.peek());
        assertNull(blockQueue.poll());
        assertTrue(blockQueue.isEmpty());

        // testing: add()
        for(Block b : blocks) {
            blockQueue.add(new BlockWrapper(b, nodeId));
        }

        prevNumber = -1;
        for(int i = 0; i < blocks.size(); i++) {
            block = blockQueue.poll();
            assertTrue(block.getNumber() > prevNumber);
            prevNumber = block.getNumber();
        }

        // testing addOrReplace()
        Block b1 = blocks.get(0);
        Block b1_ = blocks.get(1);
        BlockHeader header = b1_.getHeader();
        header.setNumber(b1.getNumber());
        b1_ = new Block(header, b1_.getTransactionsList(), b1_.getUncleList());

        blockQueue.add(new BlockWrapper(b1, nodeId));
        assertTrue(b1.isEqual(blockQueue.peek().getBlock()));

        blockQueue.add(new BlockWrapper(b1_, nodeId));
        assertTrue(b1.isEqual(blockQueue.peek().getBlock()));
        assertTrue(blockQueue.filterExisting(Arrays.asList(b1.getHash())).isEmpty());

        blockQueue.addOrReplace(new BlockWrapper(b1_, nodeId));
        assertTrue(b1_.isEqual(blockQueue.peek().getBlock()));
        assertFalse(blockQueue.filterExisting(Arrays.asList(b1.getHash())).isEmpty());
        assertTrue(blockQueue.filterExisting(Arrays.asList(b1_.getHash())).isEmpty());
    }

    @Test // concurrency
    public void test2() throws InterruptedException {
        new Thread(new Writer(1)).start();
        new Thread(new Dropper(1)).start();
        new Thread(new Reader(1)).start();
        Thread r2 = new Thread(new Reader(2));
        r2.start();
        r2.join();
    }

    @Test // test dropping
    public void test3() {
        Random rnd = new Random(System.currentTimeMillis());
        byte[] nodeA = new byte[32];
        byte[] nodeB = new byte[32];
        rnd.nextBytes(nodeA);
        rnd.nextBytes(nodeB);

        // main flow
        blockQueue.add(new BlockWrapper(blocks.get(0), nodeB));
        for (int i = 1; i < 11; i++) {
            blockQueue.add(new BlockWrapper(blocks.get(i), nodeA));
        }
        blockQueue.add(new BlockWrapper(blocks.get(11), nodeB));

        blockQueue.drop(nodeA, 10);

        assertArrayEquals(nodeB, blockQueue.take().getNodeId());
        assertArrayEquals(nodeA, blockQueue.take().getNodeId());
        assertArrayEquals(nodeB, blockQueue.take().getNodeId());

        // close/open
        blockQueue.add(new BlockWrapper(blocks.get(0), nodeA));
        blockQueue.add(new BlockWrapper(blocks.get(1), nodeB));
        blockQueue.drop(nodeA, 10);

        blockQueue.close();
        blockQueue.open();

        assertArrayEquals(nodeB, blockQueue.take().getNodeId());
        assertNull(blockQueue.peek());
    }

    private class Dropper implements Runnable {

        private int index;

        public Dropper(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                blockQueue.drop(new byte[32], 1000);
                logger.info("dropper {}: finished", index);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private class Reader implements Runnable {

        private int index;

        public Reader(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                int nullsCount = 0;
                while (nullsCount < 10) {
                    BlockWrapper b = blockQueue.poll();
                    logger.info("reader {}: {}", index, b == null ? null : b.getShortHash());
                    if(b == null) {
                        nullsCount++;
                    } else {
                        nullsCount = 0;
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private class Writer implements Runnable {

        private int index;

        public Writer(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                for(int i = 0; i < 50; i++) {
                    Block b = blocks.get(i);
                    blockQueue.add(new BlockWrapper(b, nodeId));
                    logger.info("writer {}: {}", index, b.getShortHash());
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
