package org.ethereum.datasource;

import java.util.Collection;

/**
 * Source which internally caches underlying Source key-value pairs
 *
 * Created by Anton Nashatyrev on 21.10.2016.
 */
public interface CachedSource<Key, Value> extends Source<Key, Value> {

    /**
     * @return The underlying Source
     */
    Source<Key, Value> getSource();

    /**
     * @return Modified entry keys if this is a write cache
     */
    Collection<Key> getModified();

    /**
     * Estimates the size of cached entries in bytes.
     * This value shouldn't be precise size of Java objects
     * @return cahe size in bytes
     */
    long estimateCacheSize();

    /**
     * Just a convenient shortcut to the most popular Sources with byte[] key
     */
    interface BytesKey<Value> extends CachedSource<byte[], Value> {}
}
