package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public class HashStoreTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private List<byte[]> hashes = new ArrayList<>();
    private HashStore hashStore;
    private String testDb;

    @Before
    public void setup() throws InstantiationException, IllegalAccessException {
        Random rnd = new Random(System.currentTimeMillis());
        for(int i = 0; i < 50; i++) {
            byte[] hash = new byte[32];
            rnd.nextBytes(hash);
            hashes.add(hash);
        }

        BigInteger bi = new BigInteger(32, new Random());
        testDb = "test_db_" + bi;
        SystemProperties.CONFIG.setDataBaseDir(testDb);
        hashStore = new HashStore.Builder().build();
    }

    @After
    public void cleanup() {
        hashStore.close();
        FileUtil.recursiveDelete(testDb);
    }

    @Test // FIFO, add first
    public void test1() throws InstantiationException, IllegalAccessException {
        for(byte[] hash : hashes) {
            hashStore.add(hash);
        }

        // testing peek and poll
        assertArrayEquals(hashes.get(0), hashStore.peek());
        for(byte[] hash : hashes) {
            assertArrayEquals(hash, hashStore.poll());
        }
        assertEquals(true, hashStore.isEmpty());
        assertNull(hashStore.peek());
        assertNull(hashStore.poll());

        // testing addFirst
        for(int i = 0; i < 10; i++) {
            hashStore.add(hashes.get(i));
        }
        for(int i = 10; i < 20; i++) {
            hashStore.addFirst(hashes.get(i));
        }
        for(int i = 19; i >= 10; i--) {
            assertArrayEquals(hashes.get(i), hashStore.poll());
        }
    }

    @Test // concurrency
    public void test2() throws InstantiationException, IllegalAccessException, InterruptedException {
        new Thread(new Writer(1)).start();
        new Thread(new Writer(2)).start();
        new Thread(new Writer(3)).start();
        new Thread(new Reader(1)).start();
        Thread r2 = new Thread(new Reader(2));
        r2.start();
        r2.join();
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
                while (nullsCount < 5) {
                    byte[] hash = hashStore.poll();
                    logger.info("reader {}: {}", index, hash == null ? null : Hex.toHexString(hash));
                    if(hash == null) {
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
                for(byte[] hash : hashes) {
                    hashStore.add(hash);
                    logger.info("writer {}: {}", index, Hex.toHexString(hash));
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
