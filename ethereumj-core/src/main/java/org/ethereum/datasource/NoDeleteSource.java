package org.ethereum.datasource;

/**
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class NoDeleteSource<Key, Value> extends SourceDelegateAdapter<Key, Value> {

    public NoDeleteSource(Source<Key, Value> src) {
        super(src);
    }

    @Override
    public void delete(Key key) {
    }

    @Override
    public void put(Key key, Value val) {
        if (val != null) super.put(key, val);
    }
}
