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

import org.ethereum.datasource.Source;
import org.ethereum.datasource.inmem.HashMapDB;

import static org.ethereum.datasource.prune.PruneWindow.DetachStatus.DETACHED;
import static org.ethereum.datasource.prune.PruneWindow.DetachStatus.KEPT;
import static org.ethereum.datasource.prune.PruneWindow.DetachStatus.PRUNED;

/**
 * Manages {@link PruneEntry} lifecycle
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 */
public class PruneWindow {

    Source<byte[], PruneEntry> entries = new HashMapDB<>();

    public void inserted(byte[] key) {
        PruneEntry entry = entries.get(key);
        if (entry == null) {
            entries.put(key, PruneEntry.newInserted());
        } else {
            entry.attached().inserted();
        }
    }

    public void deleted(byte[] key) {
        PruneEntry entry = entries.get(key);
        if (entry == null) {
            entries.put(key, PruneEntry.newDeleted());
        } else {
            entry.attached().deleted();
        }
    }

    public void revertInsert(byte[] key) {
        PruneEntry entry = entries.get(key);
        if (entry != null) {
            entry.deleted();
        }
    }

    public void revertDelete(byte[] key) {
        PruneEntry entry = entries.get(key);
        if (entry != null) {
            entry.inserted();
        }
    }

    /**
     * Decreases window reference counter by one
     * If last reference was removed it checks storage ref counter value
     */
    public DetachStatus detach(byte[] key) {

        PruneEntry entry = entries.get(key);

        // in some unexpected case entry might be not found
        // no node entry - no pruning
        if (entry == null) return DETACHED;

        // detach entry
        entry.detached();
        if (entry.pruning > 0) return KEPT;

        // delete entry
        entries.delete(key);
        if (entry.storage < 1) return PRUNED;
        return DETACHED;
    }

    public void setSource(Source<byte[], PruneEntry> src) {
        this.entries = src;
    }

    public enum DetachStatus {
        KEPT,       // yet referenced by the window
        PRUNED,     // neither window nor storage refs, tend to be deleted
        DETACHED    // left the window but has storage refs, must keep stored
    }
}
