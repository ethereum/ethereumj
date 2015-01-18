package org.ethereum.datasource;

import org.ethereum.db.Database;
import org.ethereum.db.DatabaseImpl;
import org.iq80.leveldb.DB;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 18/01/2015 21:48
 */

public class LevelDbDataSource implements KeyValueDataSource{

    String name;
    Database db;
    
    @Override
    public void init() {
        
        if (name == null) throw new NullPointerException("no name set to the db");
        db = new DatabaseImpl(name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public byte[] get(byte[] key) {
        return db.get(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        db.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        db.delete(key);
    }

    @Override
    public Set<byte[]> keys() {
        
        // todo: re-modelling DataBase for that
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBatch(Map<byte[], byte[]> rows) {
        // todo: re-modelling DataBase for that
        throw new UnsupportedOperationException();
    }
}
