package org.ethereum.net.swarm;

/**
 * Manages the local ChunkStore
 *
 * Uses {@link DBStore} for slow access long living data
 * and {@link MemStore} for fast access short living
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class LocalStore implements ChunkStore {

    public ChunkStore dbStore;
    ChunkStore memStore;

    public LocalStore(ChunkStore dbStore, ChunkStore memStore) {
        this.dbStore = dbStore;
        this.memStore = memStore;
    }

    @Override
    public void put(Chunk chunk) {
        memStore.put(chunk);
        // TODO make sure this is non-blocking call
        dbStore.put(chunk);
    }

    @Override
    public Chunk get(Key key) {
        Chunk chunk = memStore.get(key);
        if (chunk == null) {
            chunk = dbStore.get(key);
        }
        return chunk;
    }

    // for testing
    public void clean() {
        for (ChunkStore chunkStore : new ChunkStore[]{dbStore, memStore}) {
            if (chunkStore instanceof MemStore) {
                ((MemStore)chunkStore).clear();
            }
        }
    }
}
