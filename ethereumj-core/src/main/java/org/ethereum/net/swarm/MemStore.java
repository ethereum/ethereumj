package org.ethereum.net.swarm;

import org.apache.commons.collections4.map.AbstractLinkedMap;
import org.apache.commons.collections4.map.LRUMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 18.06.2015.
 */
public class MemStore implements ChunkStore {

    long maxSizeBytes = 10 * 1000000;
    long curSizeBytes = 0;

    public MemStore() {
    }

    public MemStore(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    // TODO: SoftReference for Chunks
    LRUMap<Key, Chunk> store = new LRUMap<Key, Chunk>(100000) {
        @Override
        protected boolean removeLRU(LinkEntry<Key, Chunk> entry) {
            curSizeBytes -= entry.getValue().getSize();
            return super.removeLRU(entry);
        }

        @Override
        public Chunk put(Key key, Chunk value) {
            curSizeBytes += value.getSize();
            return super.put(key, value);
        }

        @Override
        public boolean isFull() {
            return curSizeBytes >= maxSizeBytes;
        }
    };

    @Override
    public void put(Chunk chunk) {
        store.put(chunk.getKey(), chunk);
    }

    @Override
    public Chunk get(Key key) {
        return store.get(key);
    }

    public void clear() {
        store.clear();
    }

    public static void main(String[] args) {
        MemStore ms = new MemStore(100);
        int key= 0;
        while(true) {
            ms.put(new Chunk(new Key(new byte[]{0, 1, (byte) key++}), new byte[10]) {
                @Override
                public long getSize() {
                    return getData().length;
                }
            });
        }
    }
}
