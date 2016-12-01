package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public abstract class AbstractCachedSource <Key, Value> implements CachedSource<Key, Value> {
    private MemSizeEstimator<Key> keySizeEstimator;
    private MemSizeEstimator<Value> valueSizeEstimator;
    private int size = 0;

    protected void cacheAdded(Key key, Value value) {
        if (keySizeEstimator != null) {
            size += keySizeEstimator.estimateSize(key);
        }
        if (valueSizeEstimator != null) {
            size += valueSizeEstimator.estimateSize(value);
        }
    }

    protected void cacheRemoved(Key key, Value value) {
        if (keySizeEstimator != null) {
            size -= keySizeEstimator.estimateSize(key);
        }
        if (valueSizeEstimator != null) {
            size -= valueSizeEstimator.estimateSize(value);
        }
    }

    protected void cacheCleared() {
        size = 0;
    }

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
