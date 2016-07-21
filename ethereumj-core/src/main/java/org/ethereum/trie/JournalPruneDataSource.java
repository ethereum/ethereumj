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

    private KeyValueDataSource src;
    // block hash => updates
    private LinkedHashMap<ByteArrayWrapper, Updates> blockUpdates = new LinkedHashMap<>();
    private Updates currentUpdates = new Updates();

    public JournalPruneDataSource(KeyValueDataSource src) {
        this.src = src;
    }

    /*******  updates  *******/

    public synchronized byte[] put(byte[] key, byte[] value) {
        currentUpdates.insertedKeys.add(new ByteArrayWrapper(key));
        return src.put(key, value);
    }

    public synchronized void delete(byte[] key) {
        currentUpdates.deletedKeys.add(new ByteArrayWrapper(key));
        // delete is delayed
    }

    public synchronized void updateBatch(Map<byte[], byte[]> rows) {
        Map<byte[], byte[]> insertsOnly = new HashMap<>();
        for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
            if (entry.getValue() != null) {
                currentUpdates.insertedKeys.add(new ByteArrayWrapper(entry.getKey()));
                insertsOnly.put(entry.getKey(), entry.getValue());
            } else {
                currentUpdates.deletedKeys.add(new ByteArrayWrapper(entry.getKey()));
            }
        }
        src.updateBatch(insertsOnly);
    }

    public synchronized void storeBlockChanges(BlockHeader header) {
        currentUpdates.blockHeader = header;
        blockUpdates.put(new ByteArrayWrapper(header.getHash()), currentUpdates);
        currentUpdates = new Updates();
    }

    public synchronized void prune(BlockHeader header) {
        ByteArrayWrapper blockHashW = new ByteArrayWrapper(header.getHash());
        Updates updates = blockUpdates.get(blockHashW);
        if (updates != null) {
            Iterator<Map.Entry<ByteArrayWrapper, Updates>> it = blockUpdates.entrySet().iterator();
            Map.Entry<ByteArrayWrapper, Updates> cur;
            while(!(cur = it.next()).getKey().equals(blockHashW)) it.remove();
            it.remove();
            while(true) {
                updates.deletedKeys.removeAll(cur.getValue().insertedKeys);
                if (!it.hasNext()) break;
                cur = it.next();
            }
            Map<byte[], byte[]> batchRemove = new HashMap<>();
            for (ByteArrayWrapper key : updates.deletedKeys) {
                batchRemove.put(key.getData(), null);
            }
            src.updateBatch(batchRemove);
        }
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
