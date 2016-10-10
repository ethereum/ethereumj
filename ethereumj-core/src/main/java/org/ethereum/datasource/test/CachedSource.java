package org.ethereum.datasource.test;

import org.ethereum.datasource.Flushable;
import org.ethereum.datasource.Serializer;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class CachedSource<K, V, KS, VS> implements Source<K, V>, Flushable {
    Source<KS, VS> src;

    Serializer<K, KS> keySerializer;
    Serializer<V, VS> valSerializer;

    Map<K, V> cache;

    boolean cacheReads;
    boolean cacheWrites;
    boolean writeThrough;
    boolean noDelete;
    boolean delayedDelete;
    boolean flushSource;

    public CachedSource(Source<KS, VS> src, Serializer<K, KS> keySerializer, Serializer<V, VS> valSerializer) {
        this.src = src;
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
    }

    @Override
    public void put(K key, V val) {

    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void delete(K key) {

    }

    @Override
    public void flush() {

    }

    public Collection<K> getModified() {
        throw new RuntimeException("TODO");
    }

    public static class BytesKey<V, VS> extends CachedSource<byte[], V, byte[], VS> {

        public BytesKey(Source<byte[], VS> src, Serializer<V, VS> valSerializer) {
            super(src, new Serializer.IdentitySerializer<byte[]>(), valSerializer);
        }
    }

    public static class Simple<K, V> extends CachedSource<K, V, K, V> {

        public Simple(Source<K, V> src) {
            super(src, new Serializer.IdentitySerializer<K>(), new Serializer.IdentitySerializer<V>());
        }
    }
}
