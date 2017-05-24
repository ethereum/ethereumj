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

import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class CollectionUtils {

    public static <K, V> List<V> collectList(Collection<K> items, Functional.Function<K, V> collector) {
        List<V> collected = new ArrayList<>(items.size());
        for(K item : items) {
            collected.add(collector.apply(item));
        }
        return collected;
    }

    public static <K, V> Set<V> collectSet(Collection<K> items, Functional.Function<K, V> collector) {
        Set<V> collected = new HashSet<>();
        for(K item : items) {
            collected.add(collector.apply(item));
        }
        return collected;
    }

    public static <T> List<T> truncate(List<T> items, int limit) {
        if(limit > items.size()) {
            return new ArrayList<>(items);
        }
        List<T> truncated = new ArrayList<>(limit);
        for(T item : items) {
            truncated.add(item);
            if(truncated.size() == limit) {
                break;
            }
        }
        return truncated;
    }

    public static <T> List<T> selectList(Collection<T> items, Functional.Predicate<T> predicate) {
        List<T> selected = new ArrayList<>();
        for(T item : items) {
            if(predicate.test(item)) {
                selected.add(item);
            }
        }
        return selected;
    }

    public static <T> Set<T> selectSet(Collection<T> items, Functional.Predicate<T> predicate) {
        Set<T> selected = new HashSet<>();
        for(T item : items) {
            if(predicate.test(item)) {
                selected.add(item);
            }
        }
        return selected;
    }
}
