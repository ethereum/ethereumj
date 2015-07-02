package org.ethereum.datasource;

import org.ethereum.db.ByteArrayWrapper;

import java.util.*;

import static java.util.Arrays.asList;
import static org.ethereum.util.ByteUtil.wrap;

public class StorageTrieDataSource implements KeyValueDataSource {

    private static final int KEYS_LIMIT = 100;
    
    private KeyValueDataSource dataSource = new HashMapDB();
    private String name;
    private Set<ByteArrayWrapper> keys = new HashSet<>();

    public StorageTrieDataSource(String name) {
        this.name = name;
    }

    @Override
    public void init() {
        
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public byte[] get(byte[] key) {
        return dataSource.get(key);
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        switchIfNeed(asList(key));
        return dataSource.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        updateKeys(asList(key), true);
        dataSource.delete(key);
    }

    @Override
    public Set<byte[]> keys() {
        return dataSource.keys();
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        switchIfNeed(rows.keySet());
        dataSource.updateBatch(rows);
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private boolean isSwitched() {
        return this.keys == null;
    }

    private void switchIfNeed(Collection<byte[]> keys) {
        updateKeys(keys, false);

        if (!isSwitched() && this.keys.size() > KEYS_LIMIT) {
            KeyValueDataSource dataSource = new LevelDbDataSource();
            dataSource.setName(name);
            dataSource.init();

            copy(this.dataSource, dataSource);
            
            this.dataSource = dataSource;
            this.keys = null;
        }
    }

    private void updateKeys(Collection<byte[]> keys, boolean deleted) {
        if (isSwitched()) return;
        
        for (byte[] key : keys) {
            if (deleted) {
                this.keys.remove(wrap(key));
            } else {
                this.keys.add(wrap(key));
            }
        }
    }

    private static void copy(KeyValueDataSource source, KeyValueDataSource target) {
        Map<byte[], byte[]> rows = new HashMap<>();
        for (byte[] key : source.keys()) {
            rows.put(key, source.get(key));
        }
        target.updateBatch(rows);
    }
}
