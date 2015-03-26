package org.ethereum.datasource.redis;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.ethereum.util.Functional.Consumer;
import static org.ethereum.util.Functional.Function;

public class RedisMap<K, V> extends RedisStorage<V> implements Map<K, V> {

    private final RedisSerializer<K> keySerializer;

    RedisMap(String namespace, JedisPool pool, RedisSerializer<K> keySerializer, RedisSerializer<V> valueSerializer) {
        super(namespace, pool, valueSerializer);
        this.keySerializer = keySerializer;
    }

    @Override
    public int size() {
        return pooledWithResult(new Function<Jedis, Integer>() {
            @Override
            public Integer apply(Jedis jedis) {
                return jedis.hlen(getNamespace()).intValue();
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(final Object key) {
        return pooledWithResult(new Function<Jedis, Boolean>() {
            @Override
            public Boolean apply(Jedis jedis) {
                return jedis.hexists(getNamespace(), keySerializer.serialize((K) key));
            }
        });
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(final Object key) {
        return pooledWithResult(new Function<Jedis, V>() {
            @Override
            public V apply(Jedis jedis) {
                byte[] value = jedis.hget(getNamespace(), keySerializer.serialize((K) key));
                return deserialize(value);
            }
        });
    }

    @Override
    public V put(K key, V value) {
/*
        return pooledWithResult(new Function<Jedis, V>() {
            @Override
            public V apply(Jedis jedis) {
                return jedis.hset(getNamespace(), keySerializer.serialize(key), serialize(value));
            }
        });
*/
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        pooled(new Consumer<Jedis>() {
            @Override
            public void accept(Jedis jedis) {
                jedis.del(getNamespace());
            }
        });
    }

    @Override
    public Set<K> keySet() {
        return pooledWithResult(new Function<Jedis, Set<K>>() {
            @Override
            public Set<K> apply(Jedis jedis) {
                Set<K> result = new HashSet<K>();
                collect(jedis.hkeys(getNamespace()), new Transformer<byte[], K>() {
                    @Override
                    public K transform(byte[] input) {
                        return keySerializer.deserialize(input);
                    }
                }, result);
                return result;
            }
        });
    }

    @Override
    public Collection<V> values() {
        return pooledWithResult(new Function<Jedis, Collection<V>>() {
            @Override
            public Collection<V> apply(Jedis jedis) {
                return deserialize(jedis.hvals(getNamespace()));
            }
        });
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return pooledWithResult(new Function<Jedis, Set<Entry<K, V>>>() {
            @Override
            public Set<Entry<K, V>> apply(Jedis jedis) {
                Set<Entry<K, V>> result = new HashSet<Entry<K, V>>();
                collect(jedis.hgetAll(getNamespace()).entrySet(), new Transformer<Entry<byte[], byte[]>, Entry<K, V>>() {
                    @Override
                    public Entry<K, V> transform(Entry<byte[], byte[]> input) {
                        K key = keySerializer.deserialize(input.getKey());
                        V value = deserialize(input.getValue());
                        return new RedisMapEntry<K, V>(key, value);
                    }
                }, result);
                return result;
            }
        });
    }

    private class RedisMapEntry<K, V> extends AbstractMapEntry<K, V> {

        RedisMapEntry(K key, V value) {
            super(key, value);
        }
    }
}
