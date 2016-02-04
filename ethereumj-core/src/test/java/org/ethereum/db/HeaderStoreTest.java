package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.util.FileUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * Created by Anton Nashatyrev on 25.12.2015.
 */
public class HeaderStoreTest {

    static AtomicInteger cnt = new AtomicInteger();

    static BlockHeaderWrapper[] createHeaders(int headCnt) {
        BlockHeaderWrapper[] ret = new BlockHeaderWrapper[headCnt];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new BlockHeaderWrapper(new BlockHeader(new byte[0], new byte[0], new byte[0], new byte[0], new byte[0],
                    cnt.getAndIncrement(), 0, 0, 0, new byte[0], new byte[0], new byte[0]), null);
        }
        return ret;
    }

    String testDb;
    HeaderStoreImpl hs;

    @Before
    public void setup() throws InstantiationException, IllegalAccessException, URISyntaxException, IOException {
        BigInteger bi = new BigInteger(32, new Random());
        testDb = "test_db_" + bi;
        CONFIG.setDataBaseDir(testDb);
        CONFIG.setDatabaseReset(false);

        hs = new HeaderStoreImpl();
        hs.setMapDBFactory(new MapDBFactoryImpl());
        hs.open();
    }

    @After
    public void cleanup() {
        hs.close();
        FileUtil.recursiveDelete(testDb);
    }

    @Test
    public void test1() {
        hs.addBatch(Arrays.asList(createHeaders(10)));
        List<BlockHeaderWrapper> bhs = hs.pollBatch(1000);
        Assert.assertEquals(10, bhs.size());

        BlockHeaderWrapper[] headers = createHeaders(2);
        hs.add(headers[1]);
        hs.add(headers[0]);
        BlockHeaderWrapper bh = hs.poll();
        Assert.assertEquals(headers[0].getNumber(), bh.getNumber());
        bh = hs.poll();
        Assert.assertEquals(headers[1].getNumber(), bh.getNumber());
    }

    @Test
    public void concurrentTest1() throws InterruptedException {
        final CountDownLatch waiter = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    hs.addBatch(Arrays.asList(createHeaders(10)));
                    System.out.println("Added 10");
                }
            }
        }.start();

        new Thread() {
            int cnt = 0;
            @Override
            public void run() {
                while (cnt < 10 * 10) {
                    List<BlockHeaderWrapper> ret = hs.pollBatch(100000);
                    if (ret.size() > 0) {
                        System.out.println("Polled " + ret.size());
                    }
                    cnt += ret.size();
                }
                waiter.countDown();
            }
        }.start();
        waiter.await(20, TimeUnit.SECONDS);
        Assert.assertTrue(waiter.getCount() == 0);
    }
}
