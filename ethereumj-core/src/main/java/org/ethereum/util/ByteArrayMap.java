package org.ethereum.util;

import com.sun.istack.internal.NotNull;
import org.ethereum.db.ByteArrayWrapper;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 06.10.2016.
 */
public class ByteArrayMap<V> implements Map<byte[], V> {
    Map<ByteArrayWrapper, V> delegate = new HashMap<>();

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(new ByteArrayWrapper((byte[]) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate.get(new ByteArrayWrapper((byte[]) key));
    }

    @Override
    public V put(byte[] key, V value) {
        return delegate.put(new ByteArrayWrapper(key), value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(new ByteArrayWrapper((byte[]) key));
    }

    @Override
    public void putAll(Map<? extends byte[], ? extends V> m) {
        for (Entry<? extends byte[], ? extends V> entry : m.entrySet()) {
            delegate.put(new ByteArrayWrapper(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<byte[]> keySet() {
        return new SetAdapter<>(this);
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<byte[], V>> entrySet() {
        Set<Entry<byte[], V>> ret = new HashSet<>();
        for (Entry<ByteArrayWrapper, V> entry : delegate.entrySet()) {
            ret.add(new AbstractMap.SimpleImmutableEntry<byte[], V>(entry.getKey().getData(), entry.getValue()));
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
