package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class JournalSource<V> extends AbstractChainedSource<byte[], V, byte[], V>
        implements HashedKeySource<byte[], V> {

    private class Update {
        byte[] updateHash;
        List<byte[]> insertedKeys = new ArrayList<>();
        List<byte[]> deletedKeys = new ArrayList<>();
    }

    private Update currentUpdate = new Update();

    Source<byte[], Update> journal = new HashMapDB<>();

    public JournalSource(Source<byte[], V> src) {
        super(src);
    }

    @Override
    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
            return;
        }

        currentUpdate.insertedKeys.add(key);
        getSource().put(key, val);
    }

    @Override
    public void delete(byte[] key) {
        currentUpdate.deletedKeys.add(key);
    }

    @Override
    public V get(byte[] key) {
        return getSource().get(key);
    }

    public void commitUpdates(byte[] updateHash) {
        currentUpdate.updateHash = updateHash;
        journal.put(updateHash, currentUpdate);
        currentUpdate = new Update();
    }

    public boolean hasUpdate(byte[] updateHash) {
        return journal.get(updateHash) != null;
    }

    public void persistUpdate(byte[] updateHash) {
        Update update = journal.get(updateHash);
        if (update == null) throw new RuntimeException("No update found: " + Hex.toHexString(updateHash));
        for (byte[] key : update.deletedKeys) {
            getSource().delete(key);
        }
        journal.delete(updateHash);
    }

    public void revertUpdate(byte[] updateHash) {
        Update update = journal.get(updateHash);
        if (update == null) throw new RuntimeException("No update found: " + Hex.toHexString(updateHash));
        for (byte[] key : update.insertedKeys) {
            getSource().delete(key);
        }
        journal.delete(updateHash);
    }

    @Override
    public boolean flushImpl() {
        journal.flush();
        return false;
    }
}
