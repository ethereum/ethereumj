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
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.MemSizeEstimator;
import org.ethereum.datasource.WriteCache;
import org.ethereum.datasource.inmem.HashMapDB;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.ethereum.datasource.MemSizeEstimator.ByteArrayEstimator;
import static org.ethereum.util.ByteUtil.intToBytes;

/**
 * Created by Anton Nashatyrev on 23.12.2016.
 */
public class FlushDbManagerTest {

    @Test
    public void testConcurrentCommit() throws Throwable {
        // check that FlushManager commit(Runnable) is performed atomically

        final HashMapDB<byte[]> db1 = new HashMapDB<>();
        final HashMapDB<byte[]> db2 = new HashMapDB<>();
        final WriteCache<byte[], byte[]> cache1 = new WriteCache.BytesKey<>(db1, WriteCache.CacheType.SIMPLE);
        cache1.withSizeEstimators(ByteArrayEstimator, ByteArrayEstimator);
        final WriteCache<byte[], byte[]> cache2 = new WriteCache.BytesKey<>(db2, WriteCache.CacheType.SIMPLE);
        cache2.withSizeEstimators(ByteArrayEstimator, ByteArrayEstimator);

        final DbFlushManager dbFlushManager = new DbFlushManager(SystemProperties.getDefault(), Collections.<DbSource>emptySet(), null);
        dbFlushManager.addCache(cache1);
        dbFlushManager.addCache(cache2);
        dbFlushManager.setSizeThreshold(1);

        final CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] exception = new Throwable[1];

        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 300; i++) {
                        final int i_ = i;
                        dbFlushManager.commit(new Runnable() {
                            @Override
                            public void run() {
                                cache1.put(intToBytes(i_), intToBytes(i_));
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {}
                                cache2.put(intToBytes(i_), intToBytes(i_));
                            }
                        });
                    }
                } catch (Throwable e) {
                    exception[0] = e;
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        dbFlushManager.commit();
                        Thread.sleep(5);
                    }
                } catch (Exception e) {
                    exception[0] = e;
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    int cnt = 0;
                    while (true) {
                        synchronized (dbFlushManager) {
                            for (; cnt < 300; cnt++) {
                                byte[] val1 = db1.get(intToBytes(cnt));
                                byte[] val2 = db2.get(intToBytes(cnt));
                                if (val1 == null) {
                                    Assert.assertNull(val2);
                                    break;
                                } else {
                                    Assert.assertNotNull(val2);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    exception[0] = e;
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        }.start();



        latch.await();

        if (exception[0] != null) throw exception[0];
    }
}
