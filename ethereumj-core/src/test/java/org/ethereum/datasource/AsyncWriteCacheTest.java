package org.ethereum.datasource;

import org.ethereum.db.SlowHashMapDb;
import org.junit.Test;

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
                    public boolean flushImpl() {
                        flushing = true;
                        System.out.println("Flushing started");
                        boolean ret = super.flushImpl();
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
}
