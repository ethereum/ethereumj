package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * The JournalSource records all the changes which were made before each commitUpdate
 * Unlike 'put' deletes are not propagated to the backing Source immediately but are
 * delayed until 'persistUpdate' is called for the corresponding hash.
 * Also 'revertUpdate' might be called for a hash, in this case all inserts are removed
 * from the database.
 *
 * Normally this class is used for State pruning: we need all the state nodes for last N
 * blocks to be able to get back to previous state for applying fork block
 * however we would like to delete 'zombie' nodes which are not referenced anymore by
 * calling 'persistUpdate' for the block CurrentBlockNumber - N and we would
 * also like to remove the updates made by the blocks which weren't too lucky
 * to remain on the main chain by calling revertUpdate for such blocks
 *
 * NOTE: the backing Source should be <b>counting</b> for this class to work correctly
 * if e.g. some key is deleted in block 100 then added in block 200
 * then pruning of the block 100 would delete this key from the backing store
 * if it was non-counting
 *
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

    /**
     * Constructs instance with the underlying backing Source
     * @param src the Source must implement counting semantics
     *            see e.g. {@link CountingBytesSource} or {@link WriteCache.CacheType#COUNTING}
     */
    public JournalSource(Source<byte[], V> src) {
        super(src);
    }

    /**
     * Inserts are immediately propagated to the backing Source
     * though are still recorded to the current update
     * The insert might later be reverted due to revertUpdate call
     */
    @Override
    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
            return;
        }

        currentUpdate.insertedKeys.add(key);
        getSource().put(key, val);
    }

    /**
     * Deletes are not propagated to the backing Source immediately
     * but instead they are recorded to the current Update and
     * might be later persisted with persistUpdate call
     */
    @Override
    public void delete(byte[] key) {
        currentUpdate.deletedKeys.add(key);
    }

    @Override
    public V get(byte[] key) {
        return getSource().get(key);
    }

    /**
     * Records all the changes made prior to this call to a single chunk
     * with supplied hash.
     * Later those updates could be either persisted to backing Source (deletes only)
     * via persistUpdate call
     * or reverted from the backing Source (inserts only)
     * via revertUpdate call
     */
    public void commitUpdates(byte[] updateHash) {
        currentUpdate.updateHash = updateHash;
        journal.put(updateHash, currentUpdate);
        currentUpdate = new Update();
    }

    /**
     *  Checks if the update with this hash key exists
     */
    public boolean hasUpdate(byte[] updateHash) {
        return journal.get(updateHash) != null;
    }

    /**
     * Persists all deletes to the backing store made under this hash key
     */
    public void persistUpdate(byte[] updateHash) {
        Update update = journal.get(updateHash);
        if (update == null) throw new RuntimeException("No update found: " + Hex.toHexString(updateHash));
        for (byte[] key : update.deletedKeys) {
            getSource().delete(key);
        }
        journal.delete(updateHash);
    }

    /**
     * Deletes all inserts to the backing store made under this hash key
     */
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
