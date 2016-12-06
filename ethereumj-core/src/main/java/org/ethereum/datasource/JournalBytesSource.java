package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class JournalBytesSource extends SourceDelegateAdapter<byte[], byte[]>
        implements HashedKeySource<byte[], byte[]> {

    private class Update {
        byte[] updateHash;
        List<byte[]> insertedKeys = new ArrayList<>();
        List<byte[]> deletedKeys = new ArrayList<>();
    }

    private Update currentUpdate = new Update();

    Source<byte[], Update> journal = new HashMapDB<>();

    public JournalBytesSource(Source<byte[], byte[]> src) {
        super(src);
    }

    @Override
    public void put(byte[] key, byte[] val) {
        if (val == null) {
            delete(key);
            return;
        }

        currentUpdate.insertedKeys.add(key);
        super.put(key, val);
    }

    @Override
    public void delete(byte[] key) {
        currentUpdate.deletedKeys.add(key);
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
            super.delete(key);
        }
        journal.delete(updateHash);
    }

    public void revertUpdate(byte[] updateHash) {
        Update update = journal.get(updateHash);
        if (update == null) throw new RuntimeException("No update found: " + Hex.toHexString(updateHash));
        for (byte[] key : update.insertedKeys) {
            super.delete(key);
        }
        journal.delete(updateHash);
    }

    @Override
    public boolean flush() {
        journal.flush();
        return super.flush();
    }
}
