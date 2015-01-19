package test.ethereum.db;

import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;
import org.iq80.leveldb.DBException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockDB implements KeyValueDataSource {

    Map<ByteArrayWrapper, byte[]> storage = new HashMap<>();


    @Override
    public void delete(byte[] arg0) throws DBException {
        storage.remove(arg0);
    }


    @Override
    public byte[] get(byte[] arg0) throws DBException {

        return storage.get(new ByteArrayWrapper(arg0));
    }


    @Override
    public void put(byte[] key, byte[] value) throws DBException {

        storage.put(new ByteArrayWrapper(key), value);
    }
    
    /**
     * Returns the number of items added to this Mock DB
     *
     * @return int
     */
    public int getAddedItems() {
        return storage.size();
    }

    @Override
    public void init() {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Set<byte[]> keys() {
        return null;
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {

    }

    @Override
    public void close() {

    }
}