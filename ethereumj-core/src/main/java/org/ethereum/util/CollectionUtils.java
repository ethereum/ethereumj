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
