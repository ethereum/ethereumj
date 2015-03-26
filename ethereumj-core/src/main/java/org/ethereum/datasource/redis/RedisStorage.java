package org.ethereum.datasource.redis;

import org.apache.commons.collections4.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.Collection;

import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.ethereum.util.Functional.*;

public abstract class RedisStorage<T> {

    private static final Logger logger = LoggerFactory.getLogger("db");

    private String namespace;
    private final JedisPool pool;
    private final RedisSerializer<T> serializer;

    RedisStorage(String namespace, JedisPool pool, RedisSerializer<T> serializer) {
        this.pool = pool;
        this.serializer = serializer;

        setNamespace(namespace);
    }

    protected byte[] getNamespace() {
        return namespace.getBytes();
    }

    protected void setNamespace(String namespace) {
        this.namespace = namespace + ":";
    }

    protected byte[] formatKey(byte[] key) {
        byte[] prefix = getNamespace();

        int length = prefix.length + key.length;
        byte[] result = new byte[length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(key, 0, result, prefix.length, key.length);
        return result;
    }

    protected byte[] serialize(Object o) {
        if (serializer.canSerialize(o)) {
            return serializer.serialize((T) o);
        }

        logger.warn("Cannot serialize '%s'.", o.getClass());
        return new byte[0];
    }

    protected byte[][] serialize(Collection<?> collection) {
        return collect(collection, new Transformer<Object, byte[]>() {
            @Override
            public byte[] transform(Object input) {
                return serialize(input);
            }
        }).toArray(new byte[][]{});
    }

    protected T deserialize(byte[] bytes) {
        return serializer.deserialize(bytes);
    }


    protected Collection<T> deserialize(Collection<byte[]> bytesCollection) {
        return collect(bytesCollection, new Transformer<byte[], T>() {
            @Override
            public T transform(byte[] input) {
                return deserialize(input);
            }
        });
    }

    protected <P> void doInPipeline(final Collection<P> collection, final BiConsumer<P, Pipeline> consumer) {
        pooled(new Consumer<Jedis>() {
            @Override
            public void accept(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                try {
                    for (P el : collection) {
                        consumer.accept(el, pipeline);
                    }
                } finally {
                    pipeline.sync();
                }
            }
        });
    }

    protected void pooled(final Consumer<Jedis> consumer) {
        pooledWithResult(new Function<Jedis, Object>() {
            @Override
            public Object apply(Jedis jedis) {
                consumer.accept(jedis);
                return null;
            }
        });
    }

    protected <R> R pooledWithResult(Function<Jedis, R> function) {
        Jedis jedis = pool.getResource();
        try {
            return function.apply(jedis);
        } finally {
            pool.returnResource(jedis);
        }
    }
}
