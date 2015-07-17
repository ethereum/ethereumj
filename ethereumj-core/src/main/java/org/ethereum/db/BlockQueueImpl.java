package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.Serializers;
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
    private MapDBFactory mapDBFactory;

    private DB db;
    private Map<Long, Block> blocks;
    private List<Long> index;

    @Override
    public void open() {
        db = mapDBFactory.createTransactionalDB(dbName());
        blocks = db.hashMapCreate(STORE_NAME)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializers.BLOCK)
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
        List<Long> numbers = new ArrayList<>(blockList.size());
        for(Block b : blockList) {
            blocks.put(b.getNumber(), b);
            numbers.add(b.getNumber());
        }
        db.commit();
        index.addAll(numbers);
        sortIndex();
    }

    @Override
    public synchronized void add(Block block) {
        blocks.put(block.getNumber(), block);
        db.commit();
        index.add(block.getNumber());
        sortIndex();
    }

    @Override
    public synchronized Block poll() {
        if(!index.isEmpty()) {
            Long idx = index.get(0);
            Block block = blocks.get(idx);
            blocks.remove(idx);
            db.commit();
            index.remove(0);
            return block;
        } else {
            return null;
        }
    }

    @Override
    public synchronized Block peek() {
        if(!index.isEmpty()) {
            Long idx = index.get(0);
            return blocks.get(idx);
        } else {
            return null;
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

    private void sortIndex() {
        Collections.sort(index);
    }

    public void setMapDBFactory(MapDBFactory mapDBFactory) {
        this.mapDBFactory = mapDBFactory;
    }
}
