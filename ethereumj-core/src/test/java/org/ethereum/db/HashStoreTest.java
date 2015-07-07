package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.util.FileUtil;
import org.junit.Before;
import org.junit.Test;

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

    private List<byte[]> hashes = new ArrayList<>();

    @Before
    public void setup() {
        Random rnd = new Random(System.currentTimeMillis());
        for(int i = 0; i < 100; i++) {
            byte[] hash = new byte[32];
            rnd.nextBytes(hash);
            hashes.add(hash);
        }
    }

    @Test
    public void test() throws InstantiationException, IllegalAccessException {
        BigInteger bi = new BigInteger(32, new Random());
        String testDb = "test_db_" + bi;
        SystemProperties.CONFIG.setDataBaseDir(testDb);

        HashStore hashStore = new HashStore.Builder().build();

        try {
            for(byte[] hash : hashes) {
                hashStore.add(hash);
            }
            assertArrayEquals(hashes.get(0), hashStore.peek());
            for(byte[] hash : hashes) {
                assertArrayEquals(hash, hashStore.poll());
            }
            assertEquals(true, hashStore.isEmpty());
            assertNull(hashStore.peek());
            assertNull(hashStore.poll());
        } finally {
            hashStore.close();
            FileUtil.recursiveDelete(testDb);
        }
    }
}
