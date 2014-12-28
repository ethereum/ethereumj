package org.ethereum.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple LRU map used for reusing lookup values.
 */
public class LRUMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    protected final int maxEntries;

    public LRUMap(int initialEntries, int maxEntries) {
        super(initialEntries, 0.8f, 3);
        this.maxEntries = maxEntries;
    }

/* todo: temporary removed during concurrent impl
    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxEntries;
    }
*/
}