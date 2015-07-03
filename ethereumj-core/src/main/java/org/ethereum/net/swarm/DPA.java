package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public abstract class DPA {

    Chunker chunker;

    ChunkStore chunkStore;

    public abstract SectionReader retrieve(Key key);

    public abstract void store(Key key, SectionReader reader);
}
