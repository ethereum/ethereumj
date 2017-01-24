package org.ethereum.datasource;

/**
 * Cache of Caches (child caches)
 * When a child cache is not found in the local cache it is looked up in the backing Source
 * Based on this child backing cache (or null if not found) the new local cache is created
 * via create() method
 *
 * When flushing children, each child is just flushed if it has backing Source or the whole
 * child cache is put to the MultiCache backing source
 *
 * The primary goal if for caching contract storages in the child repositories (tracks)
 *
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public abstract class MultiCache<V extends CachedSource> extends ReadWriteCache.BytesKey<V> {

    public MultiCache(Source<byte[], V> src) {
        super(src, WriteCache.CacheType.SIMPLE);
    }

    /**
     * When a child cache is not found in the local cache it is looked up in the backing Source
     * Based on this child backing cache (or null if not found) the new local cache is created
     * via create() method
     */
    @Override
    public synchronized V get(byte[] key) {
        V ownCache = getCached(key);
        if (ownCache == null) {
            V v = getSource() != null ? super.get(key) : null;
            ownCache = create(key, v);
            put(key, ownCache);
        }
        return ownCache;
    }

    /**
     * each child is just flushed if it has backing Source or the whole
     * child cache is put to the MultiCache backing source
     */
    @Override
    public synchronized boolean flushImpl() {
        boolean ret = false;
        for (byte[] key: writeCache.getModified()) {
            V value = super.get(key);
            if (value.getSource() != null) {
                ret |= flushChild(value);
            } else {
                getSource().put(key, value);
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Is invoked to flush child cache if it has backing Source
     * Some additional tasks may be performed by subclasses here
     */
    protected boolean flushChild(V childCache) {
        return childCache.flush();
    }

    /**
     * Creates a local child cache instance based on the child cache instance
     * (or null) from the MultiCache backing Source
     */
    protected abstract V create(byte[] key, V srcCache);
}
