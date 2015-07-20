package org.ethereum.net.swarm;

/**
 * Self-explanatory interface
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public interface ChunkStore {

    void put(Chunk chunk);

    Chunk get(Key key);
}
