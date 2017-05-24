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
package org.ethereum.db.index;

import java.util.Collection;

/**
 * @author Mikhail Kalinin
 * @since 28.01.2016
 */
public interface Index extends Iterable<Long> {

    void addAll(Collection<Long> nums);

    void add(Long num);

    Long peek();

    Long poll();

    boolean contains(Long num);

    boolean isEmpty();

    int size();

    void clear();

    void removeAll(Collection<Long> indexes);

    Long peekLast();

    void remove(Long num);
}
