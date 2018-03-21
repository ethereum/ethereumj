/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.datasource;

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.prune.Pruner;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

/**
 * The JournalSource records all the changes which were made before each commitUpdate
 * Unlike 'put' deletes are not propagated to the backing Source immediately but are
 * delayed until {@link Pruner} accepts and persists changes for the corresponding hash.
 *
 * Normally this class is used together with State pruning: we need all the state nodes for last N
 * blocks to be able to get back to previous state for applying fork block
 * however we would like to delete 'zombie' nodes which are not referenced anymore by
 * persisting update for the block CurrentBlockNumber - N and we would
 * also like to remove the updates made by the blocks which weren't too lucky
 * to remain on the main chain by reverting update for such blocks
 *
 * @see Pruner
 *
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class JournalSource<V> extends AbstractChainedSource<byte[], V, byte[], V>
        implements HashedKeySource<byte[], V> {

    public static class Update {
        byte[] updateHash;
        List<byte[]> insertedKeys = new ArrayList<>();
        List<byte[]> deletedKeys = new ArrayList<>();

        public Update() {
        }

        public Update(byte[] bytes) {
            parse(bytes);
        }

        public byte[] serialize() {
            byte[][] insertedBytes = new byte[insertedKeys.size()][];
            for (int i = 0; i < insertedBytes.length; i++) {
                insertedBytes[i] = RLP.encodeElement(insertedKeys.get(i));
            }
            byte[][] deletedBytes = new byte[deletedKeys.size()][];
            for (int i = 0; i < deletedBytes.length; i++) {
                deletedBytes[i] = RLP.encodeElement(deletedKeys.get(i));
            }
            return RLP.encodeList(RLP.encodeElement(updateHash),
                    RLP.encodeList(insertedBytes), RLP.encodeList(deletedBytes));
        }

        private void parse(byte[] encoded) {
            RLPList l = (RLPList) RLP.decode2(encoded).get(0);
            updateHash = l.get(0).getRLPData();

            for (RLPElement aRInserted : (RLPList) l.get(1)) {
                insertedKeys.add(aRInserted.getRLPData());
            }
            for (RLPElement aRDeleted : (RLPList) l.get(2)) {
                deletedKeys.add(aRDeleted.getRLPData());
            }
        }

        public List<byte[]> getInsertedKeys() {
            return insertedKeys;
        }

        public List<byte[]> getDeletedKeys() {
            return deletedKeys;
        }
    }

    private Update currentUpdate = new Update();

    Source<byte[], Update> journal = new HashMapDB<>();

    /**
     * Constructs instance with the underlying backing Source
     */
    public JournalSource(Source<byte[], V> src) {
        super(src);
    }

    public void setJournalStore(Source<byte[], byte[]> journalSource) {
        journal = new SourceCodec.BytesKey<>(journalSource,
                new Serializer<Update, byte[]>() {
                    public byte[] serialize(Update object) { return object.serialize(); }
                    public Update deserialize(byte[] stream) { return stream == null ? null : new Update(stream); }
                });
    }

    /**
     * Inserts are immediately propagated to the backing Source
     * though are still recorded to the current update
     * The insert might later be reverted by {@link Pruner}
     */
    @Override
    public synchronized void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
            return;
        }

        getSource().put(key, val);
        currentUpdate.insertedKeys.add(key);
    }

    /**
     * Deletes are not propagated to the backing Source immediately
     * but instead they are recorded to the current Update and
     * might be later persisted
     */
    @Override
    public synchronized void delete(byte[] key) {
        currentUpdate.deletedKeys.add(key);
    }

    @Override
    public synchronized V get(byte[] key) {
        return getSource().get(key);
    }

    /**
     * Records all the changes made prior to this call to a single chunk
     * with supplied hash.
     * Later those updates could be either persisted to backing Source (deletes only)
     * or reverted from the backing Source (inserts only)
     */
    public synchronized Update commitUpdates(byte[] updateHash) {
        currentUpdate.updateHash = updateHash;
        journal.put(updateHash, currentUpdate);
        Update committed = currentUpdate;
        currentUpdate = new Update();
        return committed;
    }

    public Source<byte[], Update> getJournal() {
        return journal;
    }

    @Override
    public synchronized boolean flushImpl() {
        journal.flush();
        return false;
    }
}
