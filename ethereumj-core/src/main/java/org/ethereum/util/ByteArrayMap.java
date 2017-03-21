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
package org.ethereum.util;

import org.ethereum.db.ByteArrayWrapper;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 06.10.2016.
 */
public class ByteArrayMap<V> implements Map<byte[], V> {
    private final Map<ByteArrayWrapper, V> delegate;

    public ByteArrayMap() {
        this(new HashMap<ByteArrayWrapper, V>());
    }

    public ByteArrayMap(Map<ByteArrayWrapper, V> delegate) {
        this.delegate = delegate;
    }

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
        return new ByteArraySet(new SetAdapter<>(delegate));
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

    @Override
    public String toString() {
        return delegate.toString();
    }
}
