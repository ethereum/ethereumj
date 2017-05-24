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

/**
 * Just ignores deletes from the backing Source
 * Normally used for testing for Trie backing Sources to
 * not delete older states
 *
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class NoDeleteSource<Key, Value> extends AbstractChainedSource<Key, Value, Key, Value> {

    public NoDeleteSource(Source<Key, Value> src) {
        super(src);
        setFlushSource(true);
    }

    @Override
    public void delete(Key key) {
    }

    @Override
    public void put(Key key, Value val) {
        if (val != null) getSource().put(key, val);
    }

    @Override
    public Value get(Key key) {
        return getSource().get(key);
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }
}
