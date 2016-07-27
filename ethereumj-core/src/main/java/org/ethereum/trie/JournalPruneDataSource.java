package org.ethereum.trie;

import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;

import java.util.*;

/**
 * The DataSource which doesn't immediately forward delete updates (unlike inserts)
 * but collects them tied to the block where these changes were made (the changes
 * are mapped to a block upon [storeBlockChanges] call).
 * When the [prune] is called for a block the deletes for this block are
 * submitted to the underlying DataSource with respect to following inserts.
 * E.g. if the key was deleted at block N and then inserted at block N + 10 this
 * delete is not passed.
 *
 * Created by Anton Nashatyrev on 01.07.2016.
 */
public class JournalPruneDataSource implements KeyValueDataSource {
    private class Updates {
        BlockHeader blockHeader;
        Set<ByteArrayWrapper> insertedKeys = new HashSet<>();
        Set<ByteArrayWrapper> deletedKeys = new HashSet<>();
    }

    private static class Ref {
        boolean dbRef;
        int journalRefs;

        public Ref(boolean dbRef) {
            this.dbRef = dbRef;
        }

        public int getTotRefs() {
            return journalRefs + (dbRef ? 1 : 0);
        }
    }

    Map<ByteArrayWrapper, Ref> refCount = new HashMap<>();

    private KeyValueDataSource src;
    // block hash => updates
    private LinkedHashMap<ByteArrayWrapper, Updates> blockUpdates = new LinkedHashMap<>();
    private Updates currentUpdates = new Updates();

    public JournalPruneDataSource(KeyValueDataSource src) {
        this.src = src;
    }

    /*******  updates  *******/

    public synchronized byte[] put(byte[] key, byte[] value) {
        ByteArrayWrapper keyW = new ByteArrayWrapper(key);
        if (value != null) {
            currentUpdates.insertedKeys.add(keyW);
            incRef(keyW);
            return src.put(key, value);
        } else {
            currentUpdates.deletedKeys.add(keyW);
            return value;
        }
    }

    public synchronized void delete(byte[] key) {
        currentUpdates.deletedKeys.add(new ByteArrayWrapper(key));
        // delete is delayed
    }

    public synchronized void updateBatch(Map<byte[], byte[]> rows) {
        Map<byte[], byte[]> insertsOnly = new HashMap<>();
        for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
            ByteArrayWrapper keyW = new ByteArrayWrapper(entry.getKey());
            if (entry.getValue() != null) {
                currentUpdates.insertedKeys.add(keyW);
                incRef(keyW);
                insertsOnly.put(entry.getKey(), entry.getValue());
            } else {
                currentUpdates.deletedKeys.add(keyW);
            }
        }

        src.updateBatch(insertsOnly);
    }

    private void incRef(ByteArrayWrapper keyW) {
        Ref cnt = refCount.get(keyW);
        if (cnt == null) {
            cnt = new Ref(src.get(keyW.getData()) != null);
            refCount.put(keyW, cnt);
        }
        cnt.journalRefs++;
    }

    private Ref decRef(ByteArrayWrapper keyW) {
        Ref cnt = refCount.get(keyW);
        cnt.journalRefs -= 1;
        if (cnt.journalRefs == 0) {
            refCount.remove(keyW);
        }
        return cnt;
    }

    public synchronized void storeBlockChanges(BlockHeader header) {
        currentUpdates.blockHeader = header;
        blockUpdates.put(new ByteArrayWrapper(header.getHash()), currentUpdates);
        currentUpdates = new Updates();
    }

    public synchronized void prune(BlockHeader header) {
        ByteArrayWrapper blockHashW = new ByteArrayWrapper(header.getHash());
        Updates updates = blockUpdates.remove(blockHashW);
        if (updates != null) {
            for (ByteArrayWrapper insertedKey : updates.insertedKeys) {
                decRef(insertedKey).dbRef = true;
            }

            Map<byte[], byte[]> batchRemove = new HashMap<>();
            for (ByteArrayWrapper key : updates.deletedKeys) {
                Ref ref = refCount.get(key);
                if (ref == null || ref.journalRefs == 0) {
                    batchRemove.put(key.getData(), null);
                } else if (ref != null) {
                    ref.dbRef = false;
                }
            }
            src.updateBatch(batchRemove);

            rollbackForkBlocks(header.getNumber());
        }
    }

    private void rollbackForkBlocks(long blockNum) {
        for (Updates updates : new ArrayList<>(blockUpdates.values())) {
            if (updates.blockHeader.getNumber() == blockNum) {
                rollback(updates.blockHeader);
            }
        }
    }

    private synchronized void rollback(BlockHeader header) {
        ByteArrayWrapper blockHashW = new ByteArrayWrapper(header.getHash());
        Updates updates = blockUpdates.remove(blockHashW);
        Map<byte[], byte[]> batchRemove = new HashMap<>();
        for (ByteArrayWrapper insertedKey : updates.insertedKeys) {
            Ref ref = decRef(insertedKey);
            if (ref.getTotRefs() == 0) {
                batchRemove.put(insertedKey.getData(), null);
            }
        }
        src.updateBatch(batchRemove);
    }

    public Map<ByteArrayWrapper, Ref> getRefCount() {
        return refCount;
    }

    public LinkedHashMap<ByteArrayWrapper, Updates> getBlockUpdates() {
        return blockUpdates;
    }

    /***** other *****/

    public byte[] get(byte[] key) {
        return src.get(key);
    }

    public boolean isAlive() {
        return src.isAlive();
    }

    public void setName(String name) {
        src.setName(name);
    }

    public void init() {
        src.init();
    }

    public Set<byte[]> keys() {
        return src.keys();
    }

    public void close() {
        src.close();
    }

    public String getName() {
        return src.getName();
    }
}
