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
package org.ethereum.datasource.prune;

import org.ethereum.datasource.MemSizeEstimator;
import org.ethereum.datasource.Serializer;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

/**
 * <p>
 *     Represents a trie node in pruning window.
 *
 * <p>
 *     Implements a kind of decision making system which
 *     registers each probabilistic insert and delete. <br>
 *     Registered actions are meant to be confirmed or undone in the future calls,
 *     thus entry reflects fork handling behaviour.
 *
 * <p>
 *     Once there becomes no ambiguity in entry's state
 *     its state can be propagated to the storage.
 *     See {@link #decisionMade()} method for details
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 */
public class PruneEntry {

    int inserts = 0;
    int deletes = 0;
    State state = State.UNCONFIRMED;

    enum State {
        UNCONFIRMED,
        INSERTED,
        DELETED
    }

    private PruneEntry() {
    }

    private PruneEntry(byte[] encoded) {
        parse(encoded);
    }

    static PruneEntry newlyInserted() {
        PruneEntry entry = new PruneEntry();
        entry.inserted();
        return entry;
    }

    static PruneEntry newlyDeleted() {
        PruneEntry entry = new PruneEntry();
        entry.deleted();
        return entry;
    }

    public void inserted() {
        ++inserts;
    }

    public void deleted() {
        ++deletes;
    }

    public void confirmDelete() {
        processDelete(true);
    }

    public void undoDelete() {
        processDelete(false);
    }

    public void confirmInsert() {
        processInsert(true);
    }

    public void undoInsert() {
        processInsert(false);
    }

    private void processInsert(boolean confirmed) {
        --inserts;
        if (confirmed) {
            state = State.INSERTED;
        }
    }

    private void processDelete(boolean confirmed) {
        --deletes;
        if (confirmed) {
            state = State.DELETED;
        }
    }

    public boolean isInsertConfirmed() {
        return state == State.INSERTED;
    }

    public boolean decisionMade() {

        // vote finished
        if (deletes < 1 && inserts < 1) {
            return true;
        }

        // deleted with no chance to be inserted
        if (state == State.DELETED && inserts < 1) {
            return true;
        }

        // inserted with no chance to be deleted
        if (state == State.INSERTED && deletes < 1) {
            return true;
        }

        // not yet
        return false;
    }

    private void parse(byte[] encoded) {
        byte[] data;
        RLPList elements = (RLPList) RLP.decode2(encoded).get(0);
        this.inserts = (data = elements.get(0).getRLPData()) != null ? RLP.decodeInt(data, 0) : 0;
        this.deletes = (data = elements.get(1).getRLPData()) != null ? RLP.decodeInt(data, 0) : 0;
        this.state = State.values()[(data = elements.get(1).getRLPData()) != null ? RLP.decodeInt(data, 0) : 0];
    }

    private byte[] getEncoded() {
        return RLP.encodeList(
                RLP.encodeElement(RLP.encodeInt(inserts)),
                RLP.encodeElement(RLP.encodeInt(deletes)),
                RLP.encodeElement(RLP.encodeInt(state.ordinal()))
        );
    }

    public static final MemSizeEstimator<PruneEntry> MemSizeEstimator = entry -> {
        if (entry == null) return 0;
        return 12 + 2 * 4 + 2 * 4; // object header + size of two refs (instance itself and enum) + size of couple 4-bytes counters
    };

    public static final Serializer<PruneEntry, byte[]> Serializer = new Serializer<PruneEntry, byte[]>() {

        @Override
        public byte[] serialize(PruneEntry entry) {
            return entry.getEncoded();
        }

        @Override
        public PruneEntry deserialize(byte[] stream) {
            return stream == null ? null : new PruneEntry(stream);
        }
    };
}
