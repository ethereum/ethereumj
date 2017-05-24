/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
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
