package org.ethereum.datasource.redis;

import com.google.common.primitives.Bytes;
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

    protected static final Logger log = LoggerFactory.getLogger("redis");

    private byte[] name;
    private final JedisPool pool;
    private final RedisSerializer<T> serializer;

    RedisStorage(String name, JedisPool pool, RedisSerializer<T> serializer) {
        this.name = name.getBytes();
        this.pool = pool;
        this.serializer = serializer;
    }

    protected byte[] getNameBytes() {
        return name;
    }

    protected void setName(String name) {
        this.name = name.getBytes();
    }

    protected byte[] formatName(String suffix) {
        return Bytes.concat(getNameBytes(), suffix.getBytes());
    }

    protected byte[] temporaryName() {
        return formatName(":" + Thread.currentThread().getName() + ":" + System.currentTimeMillis());
    }

    protected byte[] serialize(T o) {
        return serializer.serialize(o);
    }

    protected byte[][] serialize(Collection<?> collection) {
        return collect(collection, new Transformer<Object, byte[]>() {
            @Override
            public byte[] transform(Object input) {
                return serialize((T) input);
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
        Exception operationException = null;
        try {
            return function.apply(jedis);
        } catch (Exception e) {
            operationException = e;
            throw e;
        } finally {
            if (operationException == null) {
                pool.returnResource(jedis);
            } else {
                pool.returnBrokenResource(jedis);
            }
        }
    }
}
