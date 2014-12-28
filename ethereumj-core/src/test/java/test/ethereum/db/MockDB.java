package test.ethereum.db;

import org.ethereum.db.ByteArrayWrapper;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Range;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class MockDB implements DB {

    Map<ByteArrayWrapper, byte[]> storage = new HashMap<>();

    @Override
    public void close() throws IOException {
        storage.clear();
    }

    @Override
    public void compactRange(byte[] arg0, byte[] arg1) throws DBException {
        // TODO Auto-generated method stub
    }

    @Override
    public WriteBatch createWriteBatch() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(byte[] arg0) throws DBException {
        storage.remove(arg0);
    }

    @Override
    public Snapshot delete(byte[] arg0, WriteOptions arg1)
            throws DBException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] get(byte[] arg0) throws DBException {

        return storage.get(new ByteArrayWrapper(arg0));
    }

    @Override
    public byte[] get(byte[] arg0, ReadOptions arg1) throws DBException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long[] getApproximateSizes(Range... arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProperty(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Snapshot getSnapshot() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DBIterator iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DBIterator iterator(ReadOptions arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put(byte[] key, byte[] value) throws DBException {

        storage.put(new ByteArrayWrapper(key), value);
    }

    @Override
    public Snapshot put(byte[] arg0, byte[] arg1, WriteOptions arg2)
            throws DBException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resumeCompactions() {
        // TODO Auto-generated method stub
    }

    @Override
    public void suspendCompactions() throws InterruptedException {
        // TODO Auto-generated method stub
    }

    @Override
    public void write(WriteBatch arg0) throws DBException {
        // TODO Auto-generated method stub
    }

    @Override
    public Snapshot write(WriteBatch arg0, WriteOptions arg1)
            throws DBException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the number of items added to this Mock DB
     *
     * @return int
     */
    public int getAddedItems() {
        return storage.size();
    }
}