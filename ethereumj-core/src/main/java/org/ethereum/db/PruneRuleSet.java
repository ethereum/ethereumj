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
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static org.ethereum.datasource.prune.JournalAction.DELETION;

/**
 * Describes main pruning rule sets which are used by {@link PruneManager}
 * when it calls {@link JournalSource#processUpdate(byte[], PruneRuleSet...)}
 *
 * <p>
 *      Rules are applied to {@link PruneEntry} according to the order
 *      declared in construction of certain set
 *
 * @author Mikhail Kalinin
 * @since 18.01.2018
 *
 * @see PruneManager
 * @see PruneEntry
 * @see PruneRule
 */
public class PruneRuleSet implements PruneRule {

    private LinkedHashSet<PruneRule> rules;
    private EnumSet<JournalAction> actions = EnumSet.allOf(JournalAction.class);

    private PruneRuleSet(LinkedHashSet<PruneRule> rules) {
        this.rules = rules;
    }

    static PruneRuleSet of(PruneRule... rules) {
        return new PruneRuleSet(new LinkedHashSet<>(Arrays.asList(rules)));
    }

    @Override
    public boolean apply(PruneEntry entry, JournalAction action) {
        boolean ret = false;
        for (PruneRule rule : rules) {
            ret |= rule.apply(entry, action);
        }
        return ret;
    }

    public boolean has(PruneRule rule) {
        return rules.contains(rule);
    }

    public boolean isApplicableTo(JournalAction action) {
        return actions.contains(action);
    }

    public PruneRuleSet onlyFor(JournalAction ... actions) {
        this.actions = EnumSet.copyOf(Arrays.asList(actions));
        return this;
    }

    /**
     * A part of fork management.
     * Affirms a fact that changes become a part of main chain
     *
     * When applied to a certain set of changes:
     * - Claims that both deletions and insertions are accepted
     * - Recycles pruning by evicting inserted nodes
     * - Postpones deleted nodes to {@link #PropagateDeletions} set
     * - Keeps updates until they are processed by {@link #PropagateDeletions}
     */
    public static final PruneRuleSet AcceptChanges = of(Accept, RecycleInsertion);

    /**
     * A part of fork management.
     * Revert changes as they were made in a side chain.
     *
     * When applied to a certain set of changes:
     * - Reverts both deletions and insertions, remove inserted nodes from storage
     * - Recycles pruning by evicting inserted nodes
     * - Removes set of changes from storage
     */
    public static final PruneRuleSet RevertChanges = of(Revert, RecycleInsertion, ReleaseUpdate);

    /**
     * A set of rules for the main chain only.
     * Works with deleted nodes propagated by {@link #AcceptChanges}.
     *
     * When applied to a certain set of changes:
     * - If entry is dirty it is sent back to the fork management, see {@link #PropagateDeletion} for details
     * - Otherwise, deletions are propagated to storage and removed from pruning
     * - Removes set of changes from storage
     */
    public static final PruneRuleSet PropagateDeletions = of(PropagateDeletion, ReleaseUpdate).onlyFor(DELETION);
}
