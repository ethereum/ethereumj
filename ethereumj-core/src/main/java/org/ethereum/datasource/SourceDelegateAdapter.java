package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class SourceDelegateAdapter<Key, Value> implements Source<Key, Value> {

    protected Source<Key, Value> delegate;

    public SourceDelegateAdapter(Source<Key, Value> delegate) {
        this.delegate = delegate;
    }

    protected SourceDelegateAdapter() {
    }

    @Override
    public void put(Key key, Value val) {
        delegate.put(key, val);
    }

    @Override
    public Value get(Key key) {
        return delegate.get(key);
    }

    @Override
    public void delete(Key key) {
        delegate.delete(key);
    }

    @Override
    public boolean flush() {
        return delegate.flush();
    }
}
