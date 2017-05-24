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

import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.util.AbstractList;

/**
 * Stores List structure in Source structure
 *
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public class DataSourceArray<V> extends AbstractList<V> {
    private ObjectDataSource<V> src;
    private static final byte[] SIZE_KEY = Hex.decode("FFFFFFFFFFFFFFFF");
    private int size = -1;

    public DataSourceArray(ObjectDataSource<V> src) {
        this.src = src;
    }

    public synchronized boolean flush() {
        return src.flush();
    }

    @Override
    public synchronized V set(int idx, V value) {
        if (idx >= size()) {
            setSize(idx + 1);
        }
        src.put(ByteUtil.intToBytes(idx), value);
        return value;
    }

    @Override
    public synchronized void add(int index, V element) {
        set(index, element);
    }

    @Override
    public synchronized V remove(int index) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public synchronized V get(int idx) {
        if (idx < 0 || idx >= size()) throw new IndexOutOfBoundsException(idx + " > " + size);
        return src.get(ByteUtil.intToBytes(idx));
    }

    @Override
    public synchronized int size() {
        if (size < 0) {
            byte[] sizeBB = src.getSource().get(SIZE_KEY);
            size = sizeBB == null ? 0 : ByteUtil.byteArrayToInt(sizeBB);
        }
        return size;
    }

    private synchronized void setSize(int newSize) {
        size = newSize;
        src.getSource().put(SIZE_KEY, ByteUtil.intToBytes(newSize));
    }
}
