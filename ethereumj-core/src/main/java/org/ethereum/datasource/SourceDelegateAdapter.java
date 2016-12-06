package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class SourceDelegateAdapter<Key, Value> extends AbstractChainedSource<Key, Value, Key, Value> {

    public SourceDelegateAdapter(Source<Key, Value> delegate) {
        super(delegate);
    }

    protected SourceDelegateAdapter() {
        super();
    }

    @Override
    public void put(Key key, Value val) {
        getSource().put(key, val);
    }

    @Override
    public Value get(Key key) {
        return getSource().get(key);
    }

    @Override
    public void delete(Key key) {
        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        return getSource().flush();
    }
}
