package org.ethereum.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clue class between Source and BatchSource
 *
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class BatchSourceWriter<Key, Value> extends AbstractChainedSource<Key, Value, Key, Value> {

    Map<Key, Value> buf = new HashMap<>();

    public BatchSourceWriter(BatchSource<Key, Value> src) {
        super(src);
    }

    private BatchSource<Key, Value> getBatchSource() {
        return (BatchSource<Key, Value>) getSource();
    }

    @Override
    public synchronized void delete(Key key) {
        buf.put(key, null);
    }

    @Override
    public synchronized void put(Key key, Value val) {
        buf.put(key, val);
    }

    @Override
    public Value get(Key key) {
        return getSource().get(key);
    }

    @Override
    public synchronized boolean flushImpl() {
        if (!buf.isEmpty()) {
            getBatchSource().updateBatch(buf);
            buf.clear();
            return true;
        } else {
            return false;
        }
    }
}
