package org.ethereum.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chain of Sources as a single Source
 * All calls to this Source are delegated to the last Source in the chain
 * On flush all Sources in chain are flushed in reverse order
 *
 * Created by Anton Nashatyrev on 07.12.2016.
 */
public class SourceChainBox<Key, Value, SourceKey, SourceValue>
        extends AbstractChainedSource<Key, Value, SourceKey, SourceValue> {

    List<Source> chain = new ArrayList<>();
    Source<Key, Value> lastSource;

    public SourceChainBox(Source<SourceKey, SourceValue> source) {
        super(source);
    }

    /**
     * Adds next Source in the chain to the collection
     * Sources should be added from most bottom (connected to the backing Source)
     * All calls to the SourceChainBox will be delegated to the last added
     * Source
     */
    public void add(Source src) {
        chain.add(src);
        lastSource = src;
    }

    @Override
    public void put(Key key, Value val) {
        lastSource.put(key, val);
    }

    @Override
    public Value get(Key key) {
        return lastSource.get(key);
    }

    @Override
    public void delete(Key key) {
        lastSource.delete(key);
    }

    @Override
    public boolean flushImpl() {
        boolean ret = false;
        for (int i = chain.size() - 1; i >= 0 ; i--) {
            ret |= chain.get(i).flush();
        }
        return ret;
    }
}
