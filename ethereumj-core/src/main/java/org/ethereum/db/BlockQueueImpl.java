package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.Serializers;
import org.ethereum.util.CollectionUtils;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public class BlockQueueImpl implements BlockQueue {

    private final static String STORE_NAME = "blockqueue";
    private final static String HASH_SET_NAME = "hashset";
    private MapDBFactory mapDBFactory;

    private DB db;
    private Map<Long, Block> blocks;
    private Set<byte[]> hashes;
    private List<Long> index;

    @Override
    public void open() {
        db = mapDBFactory.createTransactionalDB(dbName());
        blocks = db.hashMapCreate(STORE_NAME)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializers.BLOCK)
                .makeOrGet();
        hashes = db.hashSetCreate(HASH_SET_NAME)
                .serializer(Serializer.BYTE_ARRAY)
                .makeOrGet();
        index = new ArrayList<>(blocks.keySet());
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
    public synchronized void addAll(Collection<Block> blockList) {
        synchronized (this) {
            List<Long> numbers = new ArrayList<>(blockList.size());
            Set<byte[]> newHashes = new HashSet<>();
            for (Block b : blockList) {
                if(!index.contains(b.getNumber())) {
                    blocks.put(b.getNumber(), b);
                    numbers.add(b.getNumber());
                    newHashes.add(b.getHash());
                }
            }
            hashes.addAll(newHashes);
            index.addAll(numbers);
            sortIndex();
        }
        db.commit();
    }

    @Override
    public void add(Block block) {
        synchronized (this) {
            if(index.contains(block.getNumber())) {
                return;
            }
            blocks.put(block.getNumber(), block);
            index.add(block.getNumber());
            hashes.add(block.getHash());
            sortIndex();
        }
        db.commit();
    }

    @Override
    public synchronized Block poll() {
        Block block;
        synchronized (this) {
            if(index.isEmpty()) {
                return null;
            }

            Long idx = index.get(0);
            block = blocks.get(idx);
            blocks.remove(idx);
            hashes.remove(block.getHash());
            index.remove(0);
        }
        db.commit();
        return block;
    }

    @Override
    public synchronized Block peek() {
        synchronized (this) {
            if(index.isEmpty()) {
                return null;
            }
        
            Long idx = index.get(0);
            return blocks.get(idx);
        }
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public void clear() {
        synchronized(this) {
            blocks.clear();
            hashes.clear();
            index.clear();
        }
        db.commit();
    }

    @Override
    public List<byte[]> filterExisting(final Collection<byte[]> hashList) {
        return CollectionUtils.selectList(hashList, new CollectionUtils.Predicate<byte[]>() {
            @Override
            public boolean evaluate(byte[] hash) {
                return !hashes.contains(hash);
            }
        });
    }

    @Override
    public Set<byte[]> getHashes() {
        return hashes;
    }

    private void sortIndex() {
        Collections.sort(index);
    }

    public void setMapDBFactory(MapDBFactory mapDBFactory) {
        this.mapDBFactory = mapDBFactory;
    }
}
