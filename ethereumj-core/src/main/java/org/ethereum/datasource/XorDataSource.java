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

/**
 * When propagating changes to the backing Source XORs keys
 * with the specified value
 *
 * May be useful for merging several Sources into a single
 *
 * Created by Anton Nashatyrev on 18.02.2016.
 */
public class XorDataSource<V> extends AbstractChainedSource<byte[], V, byte[], V> {
    private byte[] subKey;

    /**
     * Creates instance with a value all keys are XORed with
     */
    public XorDataSource(Source<byte[], V> source, byte[] subKey) {
        super(source);
        this.subKey = subKey;
    }

    private byte[] convertKey(byte[] key) {
        return ByteUtil.xorAlignRight(key, subKey);
    }

    @Override
    public V get(byte[] key) {
        return getSource().get(convertKey(key));
    }

    @Override
    public void put(byte[] key, V value) {
        getSource().put(convertKey(key), value);
    }

    @Override
    public void delete(byte[] key) {
        getSource().delete(convertKey(key));
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }
}
