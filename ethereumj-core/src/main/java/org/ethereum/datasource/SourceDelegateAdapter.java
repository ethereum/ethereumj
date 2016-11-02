package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class SourceDelegateAdapter<Key, Value> implements Source<Key, Value> {

    protected Source<Key, Value> src;

    public SourceDelegateAdapter(Source<Key, Value> src) {
        this.src = src;
    }

    @Override
    public void put(Key key, Value val) {
        src.put(key, val);
    }

    @Override
    public Value get(Key key) {
        return src.get(key);
    }

    @Override
    public void delete(Key key) {
        src.delete(key);
    }

    @Override
    public boolean flush() {
        return src.flush();
    }
}
