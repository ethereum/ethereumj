package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public interface ChunkStore {

    void put(Chunk chunk);

    Chunk get(Key key);
}
