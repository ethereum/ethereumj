package org.ethereum.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU map used for reusing lookup values.
 */
public class LRUMap<K,V> extends LinkedHashMap<K,V> {

	private static final long serialVersionUID = 1L;

	protected final int maxEntries;
    
    public LRUMap(int initialEntries, int maxEntries) {
        super(initialEntries, 0.8f, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxEntries;
    }
}