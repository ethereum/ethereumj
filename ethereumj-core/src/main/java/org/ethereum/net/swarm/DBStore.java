package org.ethereum.net.swarm;

import org.ethereum.db.Database;

/**
 * Created by Admin on 18.06.2015.
 */
public class DBStore implements ChunkStore {
    private Database db;

    public DBStore(Database db) {
        this.db = db;
    }

    @Override
    public void put(Chunk chunk) {
        db.put(chunk.getKey().getBytes(), chunk.getData());
    }

    @Override
    public Chunk get(Key key) {
        byte[] bytes = db.get(key.getBytes());
        return bytes == null ? null : new Chunk(key, bytes);
    }
}
