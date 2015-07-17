package org.ethereum.db;

import org.ethereum.datasource.mapdb.MapDBFactory;
import org.mapdb.DB;
import org.mapdb.Serializer;

import java.util.*;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public class HashStoreImpl implements HashStore {

    private final static String STORE_NAME = "hashstore";
    private MapDBFactory mapDBFactory;

    private DB db;
    private Map<Long, byte[]> hashes;
    private List<Long> index;

    @Override
    public void open() {
        db = mapDBFactory.createTransactionalDB(dbName());
        hashes = db.hashMapCreate(STORE_NAME)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .makeOrGet();
        index = new ArrayList<>(hashes.keySet());
        sortIndex();
    }

    private String dbName() {
        return String.format("%s/%s", STORE_NAME, STORE_NAME);
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public synchronized void add(byte[] hash) {
        addInner(false, hash);
        db.commit();
    }

    @Override
    public synchronized void addFirst(byte[] hash) {
        addInner(true, hash);
        db.commit();
    }

    @Override
    public void addFirstBatch(Collection<byte[]> hashes) {
        for (byte[] hash : hashes) {
            synchronized (this) {
                addInner(true, hash);
            }
        }
        db.commit();
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
        byte[] hash = pollInner();
        db.commit();
        return hash;
    }

    @Override
    public List<byte[]> pollBatch(int qty) {
        if(index.isEmpty()) {
            return Collections.emptyList();
        }
        List<byte[]> hashes = new ArrayList<>(qty > size() ? qty : size());
        while (hashes.size() < qty) {
            byte[] hash = pollInner();
            if(hash == null) {
                break;
            }
            hashes.add(hash);
        }
        db.commit();
        return hashes;
    }

    private synchronized byte[] pollInner() {
        if(!index.isEmpty()) {
            Long idx = index.get(0);
            byte[] hash = hashes.get(idx);
            hashes.remove(idx);
            index.remove(0);
            db.commit();
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

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public void clear() {
        synchronized(this) {
            index.clear();
            hashes.clear();
        }
        db.commit();
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
