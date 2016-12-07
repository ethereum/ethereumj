package org.ethereum.datasource;

/**
 * Abstract cache implementation which tracks the cache size with
 * supplied key and value MemSizeEstimator's
 *
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public abstract class AbstractCachedSource <Key, Value>
        extends AbstractChainedSource<Key, Value, Key, Value>
        implements CachedSource<Key, Value> {

    private MemSizeEstimator<Key> keySizeEstimator;
    private MemSizeEstimator<Value> valueSizeEstimator;
    private int size = 0;

    public AbstractCachedSource(Source<Key, Value> source) {
        super(source);
    }

    /**
     * Needs to be called by the implementation when cache entry is added
     * Only new entries should be accounted for accurate size tracking
     * If the value for the key is changed the {@link #cacheRemoved}
     * needs to be called first
     */
    protected void cacheAdded(Key key, Value value) {
        if (keySizeEstimator != null) {
            size += keySizeEstimator.estimateSize(key);
        }
        if (valueSizeEstimator != null) {
            size += valueSizeEstimator.estimateSize(value);
        }
    }

    /**
     * Needs to be called by the implementation when cache entry is removed
     */
    protected void cacheRemoved(Key key, Value value) {
        if (keySizeEstimator != null) {
            size -= keySizeEstimator.estimateSize(key);
        }
        if (valueSizeEstimator != null) {
            size -= valueSizeEstimator.estimateSize(value);
        }
    }

    /**
     * Needs to be called by the implementation when cache is cleared
     */
    protected void cacheCleared() {
        size = 0;
    }

    /**
     * Sets the key/value size estimators
     */
    public AbstractCachedSource <Key, Value> withSizeEstimators(MemSizeEstimator<Key> keySizeEstimator, MemSizeEstimator<Value> valueSizeEstimator) {
        this.keySizeEstimator = keySizeEstimator;
        this.valueSizeEstimator = valueSizeEstimator;
        return this;
    }

    @Override
    public long estimateCashSize() {
        return size;
    }
}
