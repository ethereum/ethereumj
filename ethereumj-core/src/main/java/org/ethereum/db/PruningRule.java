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
import org.ethereum.datasource.prune.PruneEntryState;

import static org.ethereum.datasource.prune.JournalAction.DELETION;
import static org.ethereum.datasource.prune.JournalAction.INSERTION;

/**
 * An interface and set of basic pruning rules
 * which drives {@link PruneEntry} lifecycle
 *
 * <p>
 *     Basic pruning rules are gathered into pruning flows in {@link PruningFlow} class
 *
 * @author Mikhail Kalinin
 * @since 25.12.2017
 */
public interface PruningRule {

    /**
     * Applies rule to prune entry
     *
     * @param entry prune entry
     * @param action journal action which once was tracked for that prune entry
     * @return true if after applying the rule
     *         corresponding trie node can be deleted from storage,
     *         otherwise returns false
     */
    boolean apply(PruneEntry entry, JournalAction action);

    /**
     * Reverts given journal action,
     * usually when certain fork block is declined.
     *
     * Does not touch the storage.
     */
    PruningRule RevertAction = (entry, action) -> {
        entry.revert(action);
        return false;
    };

    /**
     * Reverts given journal action,
     * usually when certain fork block is accepted and becomes a main chain block
     *
     * Does not touch the storage.
     */
    PruningRule AcceptAction = (entry, action) -> {
        entry.accept(action);
        return false;
    };

    /**
     * Permits deletion from storage
     * if node's deletion has been accepted by previously called rules.
     *
     * Deleted entry is recycled.
     *
     * NOTE: this rule is dependent on fork management,
     *       thus it must be called only after fork management is done,
     *       otherwise it won't work correctly
     */
    PruningRule DeleteIfAccepted = (entry, action) -> {

        // applicable to DELETION only
        if (action != DELETION) {
            return false;
        }

        if (entry.isAccepted(DELETION)) {
            entry.recycle();
            return true;
        }

        return false;
    };

    /**
     * Permits deletion from storage
     * if node insertion hasn't been accepted by previously called rules,
     * usually it happens when action has been reverted.
     *
     * Deleted entry is recycled.
     */
    PruningRule DeleteIfUnaccepted = (entry, action) -> {

        if (entry.isUnaccepted()) {
            entry.recycle();
            // unaccepted deletes must not be propagated
            return action == JournalAction.INSERTION;
        }

        return false;
    };

    /**
     * Permits deletion from storage in all possible cases:
     * either it is an accepted deletion or reverted insertion.
     *
     * NOTE: unlike DeleteIfAccepted rule this one is independent
     *       and might be called before or after the fork management,
     *       thus it is useful for tests where order of fork management and main pruning may be mixed up
     */
    PruningRule DeleteIfPossible = (entry, action) -> {

        if (entry.isAccepted(DELETION)) {
            entry.recycle();
            return true;
        }

        if (entry.isUnaccepted()) {
            entry.recycle();
            // unaccepted deletes must not be propagated
            return action == JournalAction.INSERTION;
        }

        return false;
    };

    /**
     * Recycles entry if it's insertion has been accepted by previously called rules.
     *
     * Does not touch the storage cause insertions are immediately propagated.
     */
    PruningRule RecycleInserted = (entry, action) -> {

        if (entry.isAccepted(INSERTION)) {
            entry.recycle();
        }

        return false;
    };

    /**
     * Helps to make prune entry lifecycle shorter.
     *
     * Let's assume that node's deletion is accepted after the fork management,
     * but when it has almost been processed by main chain pruning it was inserted in the new block.
     * In that case it becomes a part of fork management again.
     * If that recent insert is eventually reverted then this node stays confirmed for deletion once again
     * but instead of being deleted at the end of fork management node must wait for main chain pruning
     * to be deleted from storage.
     *
     * This rule checks if node stayed for deletion is dirty (new inserts have been tracked)
     * and if yes it makes that node state UNACCEPTED. Thus it may be deleted during fork management
     * if recent inserts will be reverted finally.
     *
     * NOTE: like DeleteIfAccepted this rule is in dependency from fork management,
     *       it must be called only after fork management flow
     */
    PruningRule ResetDirty = (entry, action) -> {

        // applicable to DELETION only
        if (action != DELETION) {
            return false;
        }

        entry.resetIfDirty(DELETION);

        return false;
    };

    /**
     * Dummy rule which does nothing with storage and prune entry.
     * Its presence indicates that currently processing update should be released after it's done.
     * Check {@link JournalSource#processUpdate(byte[], PruningFlow)} for details
     */
    PruningRule ReleaseUpdate = ((entry, action) -> false);
}
