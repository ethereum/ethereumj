package org.ethereum.datasource.redis;

import org.ethereum.datasource.KeyValueDataSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.Set;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.util.Functional.Consumer;

public class RedisDataSource extends RedisMap<byte[], byte[]> implements KeyValueDataSource {

    RedisDataSource(String namespace, JedisPool pool) {
        super(namespace, pool, null, null);
    }

    @Override
    protected byte[] serializeKey(byte[] key) {
        return key;
    }

    @Override
    protected byte[] deserializeKey(byte[] bytes) {
        return bytes;
    }

    @Override
    protected byte[] serialize(byte[] value) {
        return value;
    }

    @Override
    protected byte[] deserialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public void init() {
        if (CONFIG.databaseReset()) {
            pooled(new Consumer<Jedis>() {
                @Override
                public void accept(Jedis jedis) {
                    jedis.flushAll();
                }
            });
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public String getName() {
        return new String(getNameBytes());
    }

    @Override
    public byte[] get(byte[] key) {
        return super.get(key);
    }

    @Override
    public void delete(final byte[] key) {
        remove(key);
    }

    @Override
    public Set<byte[]> keys() {
        return super.keySet();
    }

    @Override
    public void updateBatch(final Map<byte[], byte[]> rows) {
        putAll(rows);
    }

    @Override
    public void close() {

    }
}
