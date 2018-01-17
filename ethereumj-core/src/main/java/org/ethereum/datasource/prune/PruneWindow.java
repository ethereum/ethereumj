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

import java.util.function.Consumer;

import static org.ethereum.datasource.prune.PruneWindow.StorageCall.DELETE;
import static org.ethereum.datasource.prune.PruneWindow.StorageCall.KEEP;

/**
 * Manages {@link PruneEntry} lifecycle
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 */
public class PruneWindow {

    Source<byte[], PruneEntry> entries = new HashMapDB<>();

    public synchronized void inserted(byte[] key) {
        PruneEntry entry = entries.get(key);
        if (entry == null) {
            entries.put(key, PruneEntry.newlyInserted());
        } else {
            entry.inserted();
        }
    }

    public synchronized void deleted(byte[] key) {
        PruneEntry entry = entries.get(key);
        if (entry == null) {
            entries.put(key, PruneEntry.newlyDeleted());
        } else {
            entry.deleted();
        }
    }

    public StorageCall insertionConfirmed(byte[] key) {
        return deleteIfUnconfirmed(key, PruneEntry::confirmInsertion);
    }

    public StorageCall deletionConfirmed(byte[] key) {
        return deleteIfUnconfirmed(key, PruneEntry::confirmDeletion);
    }

    public StorageCall insertionReverted(byte[] key) {
        return deleteIfUnconfirmed(key, PruneEntry::undoInsertion);
    }

    public StorageCall deletionReverted(byte[] key) {
        return deleteIfUnconfirmed(key, PruneEntry::undoDeletion);
    }

    synchronized StorageCall deleteIfUnconfirmed(byte[] key, Consumer<PruneEntry> action) {

        PruneEntry entry = entries.get(key);
        if (entry == null) {
            return KEEP;
        }

        action.accept(entry);

        if (entry.decisionMade()) {
            if (!entry.isDeletionConfirmed()) { // evict UNCONFIRMED and INSERTED entries
                entries.delete(key);
            }
            if (entry.isUnconfirmed()) { // UNCONFIRMED entries can be deleted at that moment
                return DELETE;
            }
        }

        return KEEP;
    }

    public synchronized StorageCall persisted(byte[] key) {

        PruneEntry entry = entries.get(key);
        if (entry == null) {
            return KEEP;
        }

        // treat entry as DELETED, the others have been previously evicted

        if (entry.decisionMade()) {
            entries.delete(key); // evict entry from pruning
            return DELETE;
        } else {
            // otherwise it has been inserted again
            // keep entry UNCONFIRMED until decision can be made
            entry.setUnconfirmed();
        }

        return KEEP;
    }

    public void setSource(Source<byte[], PruneEntry> src) {
        this.entries = src;
    }

    public enum StorageCall {
        KEEP,   // keep (do nothing)
        DELETE  // remove from storage
    }
}
