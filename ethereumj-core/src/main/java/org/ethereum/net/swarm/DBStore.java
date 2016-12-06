package org.ethereum.net.swarm;

import org.ethereum.datasource.DbSource;

/**
 * ChunkStore backed up with KeyValueDataSource
 *
 * Created by Admin on 18.06.2015.
 */
public class DBStore implements ChunkStore {
    private DbSource db;

    public DBStore(DbSource db) {
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
