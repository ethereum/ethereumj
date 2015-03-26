package org.ethereum.datasource.redis;

import org.ethereum.datasource.KeyValueDataSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.Map;
import java.util.Set;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.util.Functional.*;

public class RedisKeyValueDataSource extends RedisStorage<byte[]> implements KeyValueDataSource {

    RedisKeyValueDataSource(String namespace, JedisPool pool, RedisSerializer<byte[]> serializer) {
        super(namespace, pool, serializer);
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
    public void setName(String name) {
        setNamespace(name);
    }

    @Override
    public byte[] get(final byte[] key) {
        return pooledWithResult(new Function<Jedis, byte[]>() {
            @Override
            public byte[] apply(Jedis jedis) {
                return jedis.get(getNamespace());
            }
        });
    }

    @Override
    public void put(final byte[] key, final byte[] value) {
        pooled(new Consumer<Jedis>() {
            @Override
            public void accept(Jedis jedis) {
                jedis.set(formatKey(key), value);
            }
        });
    }

    @Override
    public void delete(final byte[] key) {
        pooled(new Consumer<Jedis>() {
            @Override
            public void accept(Jedis jedis) {
                jedis.del(formatKey(key));
            }
        });
    }

    @Override
    public Set<byte[]> keys() {
        return pooledWithResult(new Function<Jedis, Set<byte[]>>() {
            @Override
            public Set<byte[]> apply(Jedis jedis) {
                return jedis.keys(formatKey("*".getBytes()));
            }
        });
    }

    @Override
    public void updateBatch(final Map<byte[], byte[]> rows) {
        doInPipeline(rows.entrySet(), new BiConsumer<Map.Entry<byte[], byte[]>, Pipeline>() {
            @Override
            public void accept(Map.Entry<byte[], byte[]> entry, Pipeline pipeline) {
                pipeline.set(formatKey(entry.getKey()), entry.getValue());
            }
        });
    }

    @Override
    public void close() {

    }
}
