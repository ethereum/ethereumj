package org.ethereum.datasource;

import org.ethereum.db.ByteArrayWrapper;
import org.iq80.leveldb.DBException;

import java.util.*;

import static org.ethereum.util.ByteUtil.wrap;

public class HashMapDB implements KeyValueDataSource {

    Map<ByteArrayWrapper, byte[]> storage = new HashMap<>();
    private boolean clearOnClose = true;

    @Override
    public synchronized void delete(byte[] arg0) throws DBException {
        storage.remove(wrap(arg0));
    }


    @Override
    public synchronized byte[] get(byte[] arg0) throws DBException {
        return storage.get(wrap(arg0));
    }


    @Override
    public synchronized byte[] put(byte[] key, byte[] value) throws DBException {
        return storage.put(wrap(key), value);
    }

    /**
     * Returns the number of items added to this Mock DB
     *
     * @return int
     */
    public synchronized int getAddedItems() {
        return storage.size();
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return "in-memory";
    }

    @Override
    public synchronized Set<byte[]> keys() {
        Set<byte[]> keys = new HashSet<>();
        for (ByteArrayWrapper key : storage.keySet()){
            keys.add(key.getData());
        }
        return keys;
    }

    @Override
    public synchronized void updateBatch(Map<byte[], byte[]> rows) {
        for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
            if (entry.getValue() == null) {
                storage.remove(wrap(entry.getKey()));
            } else {
                storage.put(wrap(entry.getKey()), entry.getValue());
            }
        }
    }

    public HashMapDB setClearOnClose(boolean clearOnClose) {
        this.clearOnClose = clearOnClose;
        return this;
    }

    public int getSize() {
        return storage.size();
    }

    @Override
    public void close() {
        if (clearOnClose) {
            this.storage.clear();
        }
    }
}