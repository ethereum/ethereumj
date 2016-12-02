package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public abstract class MultiCache<V extends CachedSource> extends ReadWriteCache.BytesKey<V> {

    public MultiCache(Source<byte[], V> src) {
        super(src, WriteCache.CacheType.SIMPLE);
    }

    @Override
    public synchronized V get(byte[] key) {
        V ownCache = getCached(key);
        if (ownCache == null) {
            V v = delegate != null ? super.get(key) : null;
            ownCache = create(key, v);
            put(key, ownCache);
        }
        return ownCache;
    }

    @Override
    public synchronized boolean flush() {
        boolean ret = false;
        for (byte[] key: writeCache.getModified()) {
            V value = super.get(key);
            if (value.getSrc() != null) {
                ret |= flushChild(value);
            } else {
                delegate.put(key, value);
                ret = true;
            }
        }
        return ret;
    }

    protected boolean flushChild(V childCache) {
        return childCache.flush();
    }

    protected abstract V create(byte[] key, V srcCache);
}
