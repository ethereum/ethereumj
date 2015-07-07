package org.ethereum.net.swarm;

import org.apache.commons.collections4.map.AbstractLinkedMap;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Limited capacity memory storage. Last recently used chunks are purged when
 * max memory threshold is reached.
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class MemStore implements ChunkStore {
    public final Statter statCurSize = Statter.create("net.swarm.memstore.curSize");
    public final Statter statCurChunks = Statter.create("net.swarm.memstore.curChunkCnt");

    long maxSizeBytes = 10 * 1000000;
    long curSizeBytes = 0;

    public MemStore() {
    }

    public MemStore(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    // TODO: SoftReference for Chunks?
    public Map<Key, Chunk> store = Collections.synchronizedMap(new LRUMap<Key, Chunk>(10000) {
        @Override
        protected boolean removeLRU(LinkEntry<Key, Chunk> entry) {
            curSizeBytes -= entry.getValue().getData().length;
            boolean ret = super.removeLRU(entry);
            statCurSize.add(curSizeBytes);
            statCurChunks.add(size());
            return ret;
        }

        @Override
        public Chunk put(Key key, Chunk value) {
            curSizeBytes += value.getData().length;
            Chunk ret = super.put(key, value);
            statCurSize.add(curSizeBytes);
            statCurChunks.add(size());
            return ret;
        }

        @Override
        public boolean isFull() {
            return curSizeBytes >= maxSizeBytes;
        }
    });

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
}
