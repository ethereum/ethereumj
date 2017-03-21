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
package org.ethereum.datasource;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.SlowHashMapDb;
import org.ethereum.db.StateSource;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static java.lang.Math.max;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.intToBytes;
import static org.ethereum.util.ByteUtil.intsToBytes;
import static org.spongycastle.util.encoders.Hex.decode;

/**
 * Created by Anton Nashatyrev on 19.01.2017.
 */
public class AsyncWriteCacheTest {

    volatile boolean flushing;

    @Test
    public void simpleTest1() {
        final SlowHashMapDb<String> db = new SlowHashMapDb<String>().withDelay(100);
        AsyncWriteCache<byte[], String> cache = new AsyncWriteCache<byte[], String>(db) {
            @Override
            protected WriteCache<byte[], String> createCache(Source<byte[], String> source) {
                return new WriteCache.BytesKey<String>(source, WriteCache.CacheType.SIMPLE) {
                    @Override
                    public boolean flush() {
                        flushing = true;
                        System.out.println("Flushing started");
                        boolean ret = super.flush();
                        System.out.println("Flushing complete");
                        flushing = false;
                        return ret;
                    }
                };
            }
        };

        cache.put(decode("1111"), "1111");
        cache.flush();
        assert cache.get(decode("1111")) == "1111";

        while (!flushing);

        System.out.println("get");
        assert cache.get(decode("1111")) == "1111";
        System.out.println("put");
        cache.put(decode("2222"), "2222");
        System.out.println("get");
        assert flushing;

        while (flushing) {
            assert cache.get(decode("2222")) == "2222";
            assert cache.get(decode("1111")) == "1111";
        }
        assert cache.get(decode("2222")) == "2222";
        assert cache.get(decode("1111")) == "1111";

        cache.put(decode("1111"), "1112");

        cache.flush();
        assert cache.get(decode("1111")) == "1112";
        assert cache.get(decode("2222")) == "2222";

        while (!flushing);

        System.out.println("Second flush");
        cache.flush();
        System.out.println("Second flush complete");

        assert cache.get(decode("1111")) == "1112";
        assert cache.get(decode("2222")) == "2222";

        System.out.println("put");
        cache.put(decode("3333"), "3333");

        assert cache.get(decode("1111")) == "1112";
        assert cache.get(decode("2222")) == "2222";
        assert cache.get(decode("3333")) == "3333";

        System.out.println("Second flush");
        cache.flush();
        System.out.println("Second flush complete");

        assert cache.get(decode("1111")) == "1112";
        assert cache.get(decode("2222")) == "2222";
        assert cache.get(decode("3333")) == "3333";
        assert db.get(decode("1111")) == "1112";
        assert db.get(decode("2222")) == "2222";
        assert db.get(decode("3333")) == "3333";
    }

    @Ignore
    @Test
    public void highLoadTest1() throws InterruptedException {
        final SlowHashMapDb<byte[]> db = new SlowHashMapDb<byte[]>() {
            @Override
            public void updateBatch(Map<byte[], byte[]> rows) {
                Utils.sleep(10000);
                super.updateBatch(rows);
            }
        };
        StateSource stateSource = new StateSource(db, false);
        stateSource.getReadCache().withMaxCapacity(1);

        stateSource.put(sha3(intToBytes(1)), intToBytes(1));
        stateSource.put(sha3(intToBytes(2)), intToBytes(2));

        System.out.println("Flush...");
        stateSource.flush();
        System.out.println("Flush!");

        Thread.sleep(100);
        System.out.println("Get...");
        byte[] bytes1 = stateSource.get(sha3(intToBytes(1)));
        System.out.println("Get!: " + bytes1);
        byte[] bytes2 = stateSource.get(sha3(intToBytes(2)));
        System.out.println("Get!: " + bytes2);


//        int cnt = 0;
//        while(true) {
//            for (int i = 0; i < 1000; i++) {
//                stateSource.put(sha3(intToBytes(cnt)), intToBytes(cnt));
//                cnt++;
//            }
//
//            stateSource.getWriteCache().flush();
//
////            for (int i = 0; i < 200; i++) {
////                stateSource.put(sha3(intToBytes(cnt)), intToBytes(cnt));
////                cnt++;
////            }
//
//            Thread.sleep(800);
//
//                for (int i = max(0, cnt - 1000); i < cnt; i++) {
//
//                    byte[] bytes = stateSource.get(sha3(intToBytes(i)));
//                    assert Arrays.equals(bytes, intToBytes(i));
//                }
//            System.err.println("Iteration done");
//        }
//
//
    }
}
