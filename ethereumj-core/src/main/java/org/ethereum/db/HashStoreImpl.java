package org.ethereum.db;

import org.ethereum.datasource.mapdb.MapDBFactory;

import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public class HashStoreImpl implements HashStore {

    private MapDBFactory mapDBFactory;

    private Map<Long, byte[]> hashes;
    private List<Long> index;

    @Override
    public void open() {
        hashes = mapDBFactory.createHashStoreMap();
        index = new ArrayList<>(hashes.keySet());
        sortIndex();
    }

    @Override
    public void close() {
        mapDBFactory.destroy(hashes);
    }

    @Override
    public synchronized void add(byte[] hash) {
        addInner(false, hash);
    }

    @Override
    public synchronized void addFirst(byte[] hash) {
        addInner(true, hash);
    }

    private void addInner(boolean first, byte[] hash) {
        Long idx = createIndex(first);
        hashes.put(idx, hash);
    }

    @Override
    public synchronized byte[] peek() {
        if(!index.isEmpty()) {
            Long idx = index.get(0);
            return hashes.get(idx);
        } else {
            return null;
        }
    }

    @Override
    public synchronized byte[] poll() {
        if(!index.isEmpty()) {
            Long idx = index.get(0);
            byte[] hash = hashes.get(idx);
            hashes.remove(idx);
            index.remove(0);
            return hash;
        } else {
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public Set<Long> getKeys() {
        return hashes.keySet();
    }

    private Long createIndex(boolean first) {
        Long idx;
        if(index.isEmpty()) {
            idx = 0L;
            index.add(idx);
        } else if(first) {
            idx = index.get(0) - 1;
            index.add(0, idx);
        } else {
            idx = index.get(index.size() - 1) + 1;
            index.add(idx);
        }
        return idx;
    }

    private void sortIndex() {
        Collections.sort(index);
    }

    public void setMapDBFactory(MapDBFactory mapDBFactory) {
        this.mapDBFactory = mapDBFactory;
    }
}
