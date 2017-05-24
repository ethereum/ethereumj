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

import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 28.01.2016
 */
public class ArrayListIndex implements Index {
    private List<Long> index;

    public ArrayListIndex(Collection<Long> numbers) {
        index = new ArrayList<>(numbers);
        sort();
    }

    @Override
    public synchronized void addAll(Collection<Long> nums) {
        index.addAll(nums);
        sort();
    }

    @Override
    public synchronized void add(Long num) {
        index.add(num);
        sort();
    }

    @Override
    public synchronized Long peek() {
        return index.get(0);
    }

    @Override
    public synchronized Long poll() {
        Long num = index.get(0);
        index.remove(0);
        return num;
    }

    @Override
    public synchronized boolean contains(Long num) {
        return Collections.binarySearch(index, num) >= 0;
    }

    @Override
    public synchronized boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public synchronized int size() {
        return index.size();
    }

    @Override
    public synchronized void clear() {
        index.clear();
    }

    private void sort() {
        Collections.sort(index);
    }

    @Override
    public synchronized Iterator<Long> iterator() {
        return new ArrayList<>(index).iterator();
    }

    public synchronized void removeAll(Collection<Long> indexes) {
        index.removeAll(indexes);
    }

    @Override
    public synchronized Long peekLast() {

        if (index.isEmpty()) return null;
        return index.get(index.size() - 1);
    }

    @Override
    public synchronized void remove(Long num) {
        index.remove(num);
    }
}
