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

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Describes main pruning flows which are used by {@link PruneManager}
 * when it calls {@link JournalSource#processUpdate(byte[], PruningFlow)}
 *
 * @author Mikhail Kalinin
 * @since 18.01.2018
 */
public class PruningFlow implements PruningRule {

    private LinkedHashSet<PruningRule> rules;

    private PruningFlow(LinkedHashSet<PruningRule> rules) {
        this.rules = rules;
    }

    static PruningFlow of(PruningRule ... rules) {
        return new PruningFlow(new LinkedHashSet<>(Arrays.asList(rules)));
    }

    @Override
    public boolean apply(PruneEntry entry, JournalAction action) {
        boolean ret = false;
        for (PruningRule rule : rules) {
            ret |= rule.apply(entry, action);
        }
        return ret;
    }

    public boolean has(PruningRule rule) {
        return rules.contains(rule);
    }

    /**
     * A part of fork management.
     * Treats changes as accepted for the main chain.
     *
     * When applied to a certain update:
     * - Accepts both deletions and insertions
     * - Just in case checks if change has been finally reverted and if yes propagates corresponding update to storage and recycles pruning
     * - Recycles pruning by removing entries for those nodes which are finally inserted
     * - Propagates deleted nodes to {@link #PruneMainChain} flow
     * - Keeps updates until they are processed by {@link #PruneMainChain}
     */
    public static final PruningFlow AcceptFork = of(AcceptAction, DeleteIfUnaccepted, RecycleInserted);

    /**
     * A part of fork management.
     * Treats changes as they were made in a side chain.
     *
     * When applied to a certain update:
     * - Reverts both deletions and insertions
     * - Checks if change has been reverted finally and if yes propagates corresponding update to storage and recycles pruning
     * - Recycles pruning by removing entries for those nodes which are finally inserted
     * - Propagates deleted nodes to {@link #PruneMainChain} flow
     * - Releases updates
     */
    public static final PruningFlow RevertFork = of(RevertAction, DeleteIfUnaccepted, RecycleInserted, ReleaseUpdate);

    /**
     * Main chain pruning flow.
     * Works with deleted nodes propagated by either {@link #AcceptFork} or {@link #RevertFork} flows.
     *
     * Thus, regular pruning scenarios are:
     * ~> AcceptFork: accepts changes, recycles inserts, postpones deletes
     * ~> PruneMainChain: propagates to storage and recycles deletes
     *
     * ~> RevertFork: recycles reverted changes, recycles accepted inserts, postpones deletes
     * ~> PruneMainChain: propagates to storage and recycles deletes
     *
     * When applied to a certain update:
     * - Propagates deletion to the storage if node is meant to be deleted
     * - If decision can't be made at the moment (node has been inserted after it has passed fork management flow)
     *   then resets prune entry to send it to fork management again
     * - Recycles pruning by removing deleted nodes
     * - Releases updates
     *
     * NOTE: this flow depends on either {@link #AcceptFork} or {@link #RevertFork} flow
     *       thus it must be called after one of them was applied.
     */
    public static final PruningFlow PruneMainChain = of(DeleteIfAccepted, ResetDirty, ReleaseUpdate);

    // Following rules are used mostly in tests

    /**
     * Following couple of rules are used mostly in tests.
     * Each of them applies both fork management and main chain pruning parts.
     * They are order independent, so, it may no sense be given to the order in which they are applied.
     */
    public static final PruningFlow PersistUpdate = of(AcceptAction, DeleteIfPossible, RecycleInserted, ReleaseUpdate);
    public static final PruningFlow RevertUpdate = of(RevertAction, DeleteIfPossible, RecycleInserted, ReleaseUpdate);
}
