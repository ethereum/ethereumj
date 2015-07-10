package org.ethereum.db;

import org.ethereum.datasource.mapdb.MapDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
@Component
@Scope("prototype")
public class HashStoreImpl implements HashStore {

    @Autowired
    private MapDBFactory mapDBFactory;

    private Map<Long, byte[]> hashes;
    private List<Long> index;

    @PostConstruct
    public void init() {
        hashes = mapDBFactory.createHashStoreMap();
        index = new ArrayList<>(hashes.keySet());
        sortIndex();
    }

    public synchronized void add(byte[] hash) {
        addInner(false, hash);
    }

    public synchronized void addFirst(byte[] hash) {
        addInner(true, hash);
    }

    private void addInner(boolean first, byte[] hash) {
        Long idx = createIndex(first);
        hashes.put(idx, hash);
    }

    public synchronized byte[] peek() {
        if(!index.isEmpty()) {
            Long idx = index.get(0);
            return hashes.get(idx);
        } else {
            return null;
        }
    }

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

    public boolean isEmpty() {
        return index.isEmpty();
    }

    public void close() {
        mapDBFactory.destroy(hashes);
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
}
