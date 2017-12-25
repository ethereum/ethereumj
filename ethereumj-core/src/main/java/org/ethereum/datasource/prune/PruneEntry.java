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
 * Represents a trie node in pruning window. <br/><br/>
 *
 * Keeps tracking of node references. <br/>
 * There are two types of references: storage references and references
 * inside pruning window itself. <br/><br/>
 *
 * Lifecycle: <br/>
 * ~> attached (blk N): [inserted/deleted] <br/>
 * ~> ... <br/>
 * ~> attached (blk K): [inserted/deleted] <br/>
 * ~> ... <br/>
 * ~> detached (blk N) <br/>
 * ~> ... <br/>
 * ~> detached (blk K): engage pruning <br/><br/>
 *
 * Detached nodes with storage ref counter less than 1 are treated as orphans
 * and tend to be removed from the storage.
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 */
public class PruneEntry {

    // tracks references kept by pruning window
    int pruning = 0;
    // tracks storage references
    int storage = 0;

    private PruneEntry() {
    }

    private PruneEntry(byte[] encoded) {
        parse(encoded);
    }

    static PruneEntry newInserted() {
        PruneEntry entry = new PruneEntry();
        return entry.attached().inserted();
    }

    static PruneEntry newDeleted() {
        PruneEntry entry = new PruneEntry();
        return entry.attached();
    }

    /**
     * Supposed to be called each time when node enters pruning
     */
    public PruneEntry attached() {
        ++pruning;
        return this;
    }

    /**
     * Supposed to be called each time when node leaves pruning
     */
    public PruneEntry detached() {
        --pruning;
        return this;
    }

    /**
     * Increases storage refs by one
     */
    public PruneEntry inserted() {
        ++storage;
        return this;
    }

    /**
     * Decreases storage refs by one
     */
    public PruneEntry deleted() {
        --storage;
        return this;
    }

    private void parse(byte[] encoded) {
        byte[] data;
        RLPList elements = (RLPList) RLP.decode2(encoded).get(0);
        this.pruning = (data = elements.get(0).getRLPData()) != null ? RLP.decodeInt(data, 0) : 0;
        this.storage = (data = elements.get(1).getRLPData()) != null ? RLP.decodeInt(data, 0) : 0;
    }

    private byte[] getEncoded() {
        return RLP.encodeList(
                RLP.encodeElement(RLP.encodeInt(pruning)),
                RLP.encodeElement(RLP.encodeInt(storage))
        );
    }

    public static final MemSizeEstimator<PruneEntry> memSizeEstimator = entry -> {
        if (entry == null) return 0;
        return 4 + 2 * 4; // compressed ref size + size of couple 4-bytes counters
    };

    public static final Serializer<PruneEntry, byte[]> serializer = new Serializer<PruneEntry, byte[]>() {

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
