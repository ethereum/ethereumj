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
package org.ethereum.db;

import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.prune.JournalAction;
import org.ethereum.datasource.prune.PruneEntry;

import static org.ethereum.datasource.prune.JournalAction.DELETION;
import static org.ethereum.datasource.prune.JournalAction.INSERTION;

/**
 * An interface and set of basic pruning rules
 * which drives {@link PruneEntry} lifecycle
 *
 * <p>
 *     Basic pruning rules are gathered into pruning flows in {@link PruneRuleSet} class
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 *
 * @see PruneEntry
 */
public interface PruneRule {

    /**
     * Applies rule to prune entry
     *
     * @param entry prune entry
     * @param action journal action which was once tracked for that prune entry
     * @return true if after applying the rule
     *         corresponding trie node tends to be deleted from storage,
     *         otherwise returns false
     */
    boolean apply(PruneEntry entry, JournalAction action);

    /**
     * Reverts given journal action and propagates reverting to the storage,
     * usually it happens when certain fork block is declined.
     */
    PruneRule Revert = (entry, action) -> {
        entry.revert(action);

        if (entry.isUnaccepted()) {
            entry.recycle();
            // unaccepted deletes must not be propagated
            return action == JournalAction.INSERTION;
        }

        return false;
    };

    /**
     * Accepts given journal action but does not propagate anything to storage,
     * it happens usually when certain fork block is accepted and becomes a main chain block
     */
    PruneRule Accept = (entry, action) -> {
        entry.accept(action);

        // recycle it just in case
        if (entry.isUnaccepted()) {
            entry.recycle();
        }

        return false;
    };

    /**
     * Permits deletion from storage if node tends to be deleted.
     * It, also, sends entry back to fork management if entry is still 'dirty', see {@link PruneEntry#isDirty()}.
     * Sends entry for recycling.
     *
     * NOTE: this rule is dependent on fork management,
     *       thus it must be called only after fork management is done,
     *       otherwise it won't work correctly
     */
    PruneRule PropagateDeletion = (entry, action) -> {

        // trigger fork management if dirty
        if (entry.isDirty()) {
            entry.reset();
            return false;
        }

        if (entry.acceptedFor(DELETION)) {
            entry.recycle();
            return true;
        }

        // recycle it anyway
        entry.recycle();
        return false;
    };

    /**
     * Sends all nodes which tend to be inserted for recycling.
     * Does not touch the storage cause insertions are immediately propagated.
     */
    PruneRule RecycleInsertion = (entry, action) -> {

        if (entry.acceptedFor(INSERTION)) {
            entry.recycle();
        }

        return false;
    };

    /**
     * Dummy rule which does nothing with storage and prune entry.
     * Its presence indicates that currently processing update should be released after it's done.
     * Check {@link JournalSource#processUpdate(byte[], PruneRuleSet...)} for details
     */
    PruneRule ReleaseUpdate = ((entry, action) -> false);
}
