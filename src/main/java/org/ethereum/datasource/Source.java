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
 * Base interface for all data source classes
 *
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public interface Source<K, V> {

    /**
     * Puts key-value pair into source
     */
    void put(K key, V val);

    /**
     * Gets a value by its key
     * @return value or <null/> if no such key in the source
     */
    V get(K key);

    /**
     * Deletes the key-value pair from the source
     */
    void delete(K key);

    /**
     * If this source has underlying level source then all
     * changes collected in this source are flushed into the
     * underlying source.
     * The implementation may do 'cascading' flush, i.e. call
     * flush() on the underlying Source
     * @return true if any changes we flushed, false if the underlying
     * Source didn't change
     */
    boolean flush();

}
