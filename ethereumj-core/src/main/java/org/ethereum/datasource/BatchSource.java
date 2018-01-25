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

import java.util.Map;

/**
 * The Source which is capable of batch updates.
 * The semantics of a batch update is up to implementation:
 * it can be just performance optimization or batch update
 * can be atomic or other.
 * <p>
 * Created by Anton Nashatyrev on 01.11.2016.
 */
public interface BatchSource<K, V> extends Source<K, V> {

    /**
     * Do batch update
     *
     * @param rows Normally this Map is treated just as a collection
     *             of key-value pairs and shouldn't conform to a normal
     *             Map contract. Though it is up to implementation to
     *             require passing specific Maps
     */
    void updateBatch(Map<K, V> rows);
}
