package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.vm.DataWord;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.longToBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Testing different sources and chain of sources in multi-thread environment
 */
public class MultiThreadSourcesTest {

    private byte[] intToKey(int i) {
        return sha3(longToBytes(i));
    }

    private byte[] intToValue(int i) {
        return (new DataWord(i)).getData();
    }

    private String str(Object obj) {
        if (obj == null) return null;
        return Hex.toHexString((byte[]) obj);
    }

    private class TestExecutor {

        private Source<byte[], byte[]> cache;
        boolean isCounting = false;

        boolean running = true;
        final CountDownLatch failSema = new CountDownLatch(1);
        final AtomicInteger putCnt = new AtomicInteger(1);
        final AtomicInteger delCnt = new AtomicInteger(1);
        final AtomicInteger checkCnt = new AtomicInteger(1);

        public TestExecutor(Source cache) {
            this.cache = cache;
        }

        public TestExecutor(Source cache, boolean isCounting) {
            this.cache = cache;
            this.isCounting = isCounting;
        }

        final Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(running) {
                        int curMax = putCnt.get() - 1;
                        assertEquals(str(intToValue(curMax)), str(cache.get(intToKey(curMax))));
                        checkCnt.set(curMax);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    failSema.countDown();
                }
            }
        });

        final Thread delThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(running) {
                        int toDelete = delCnt.get();
                        int curMax = putCnt.get() - 1;

                        if (toDelete > checkCnt.get() || toDelete >= curMax) {
                            sleep(10);
                            continue;
                        }
                        assertEquals(str(intToValue(toDelete)), str(cache.get(intToKey(toDelete))));

                        if (isCounting) {
                            for (int i = 0; i < (toDelete % 5); ++i) {
                                cache.delete(intToKey(toDelete));
                                assertEquals(str(intToValue(toDelete)), str(cache.get(intToKey(toDelete))));
                            }
                        }

                        cache.delete(intToKey(toDelete));
                        if (isCounting) cache.flush();
                        assertNull(cache.get(intToKey(toDelete)));
                        delCnt.getAndIncrement();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    failSema.countDown();
                }
            }
        });

        public void run(long timeout) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(running) {
                            int curCnt = putCnt.get();
                            cache.put(intToKey(curCnt), intToValue(curCnt));
                            if (isCounting) {
                                for (int i = 0; i < (curCnt % 5); ++i) {
                                    cache.put(intToKey(curCnt), intToValue(curCnt));
                                }
                            }
                            putCnt.getAndIncrement();
                            if (curCnt == 1) {
                                readThread.start();
                                delThread.start();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failSema.countDown();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(running) {
                            sleep(10);
                            cache.flush();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failSema.countDown();
                    }
                }
            }).start();

            try {
                failSema.await(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                running = false;
                throw new RuntimeException("Thrown interrupted exception", ex);
            }

            // Shutdown carefully
            running = false;

            if (failSema.getCount() == 0) {
                throw new RuntimeException("Test failed.");
            } else {
                System.out.println("Test passed, put counter: " + putCnt.get() + ", delete counter: " + delCnt.get());
            }
        }
    }

    @Test
    public void testWriteCache() throws InterruptedException {
        Source<byte[], byte[]> src = new HashMapDB<>();
        final WriteCache writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);

        TestExecutor testExecutor = new TestExecutor(writeCache);
        testExecutor.run(5);
    }

    @Test
    public void testReadCache() throws InterruptedException {
        Source<byte[], byte[]> src = new HashMapDB<>();
        final ReadCache readCache = new ReadCache.BytesKey<>(src);

        TestExecutor testExecutor = new TestExecutor(readCache);
        testExecutor.run(5);
    }

    @Test
    public void testReadWriteCache() throws InterruptedException {
        Source<byte[], byte[]> src = new HashMapDB<>();
        final ReadWriteCache readWriteCache = new ReadWriteCache.BytesKey<>(src, WriteCache.CacheType.SIMPLE);

        TestExecutor testExecutor = new TestExecutor(readWriteCache);
        testExecutor.run(5);
    }

    @Test
    public void testCountingWriteCache() throws InterruptedException {
        Source<byte[], byte[]> parentSrc = new HashMapDB<>();
        Source<byte[], byte[]> src = new CountingBytesSource(parentSrc);
        final WriteCache writeCache = new WriteCache.BytesKey<>(src, WriteCache.CacheType.COUNTING);

        TestExecutor testExecutor = new TestExecutor(writeCache, true);
        testExecutor.run(10);
    }
}
