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

import static org.ethereum.datasource.prune.PruneWindow.Decision.DELETE;
import static org.ethereum.datasource.prune.PruneWindow.Decision.KEEP;

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

    public Decision insertPersisted(byte[] key) {
        return decide(key, PruneEntry::confirmInsert);
    }

    public Decision deletePersisted(byte[] key) {
        return decide(key, PruneEntry::confirmDelete);
    }

    public Decision insertReverted(byte[] key) {
        return decide(key, PruneEntry::undoInsert);
    }

    public Decision deleteReverted(byte[] key) {
        return decide(key, PruneEntry::undoDelete);
    }

    synchronized Decision decide(byte[] key, Consumer<PruneEntry> vote) {

        PruneEntry entry = entries.get(key);
        if (entry == null) {
            return KEEP;
        }

        vote.accept(entry);

        if (entry.decisionMade()) {
            entries.delete(key);
            // keep node only if its final state is insert
            return entry.isInsertConfirmed() ? KEEP : DELETE;
        } else {
            return KEEP;
        }
    }

    public void setSource(Source<byte[], PruneEntry> src) {
        this.entries = src;
    }

    public enum Decision {
        KEEP,   // do nothing
        DELETE  // remove from storage
    }
}
