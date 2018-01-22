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

import org.ethereum.config.Constants;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.JournalSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Manages pruning by keeping updates and applying certain pruning rule sets to them.
 *
 * <ul>
 *     There are two possible scenarios
 *
 * <li>
 *     RevertChanges:
 *     triggered when it's a known fact that block is not a part of main chain
 *     it reverts all state changes made during processing of that block;
 *     it is triggered at the end of fork management window (usually 192 blocks away from the head)
 *
 * <li>
 *     ApplyChanges followed by PropagateDeletions:
 *     applying changes is an alternative action to the revert,
 *     it must be done for changes made by the main chain block
 *     to finalize fork management part (usually 192 blocks away from the head);
 *     ApplyChanges rule set does not propagates deletions to the storage,
 *     it postpones them until PropagateDeletions is called for the same changes;
 *     thus fork management is separated from main pruning part cause
 *     pruning window can be much larger than the window required for fork management,
 *     such two step pruning scenario can save a plenty of resources
 *
 * </ul>
 *
 * Created by Anton Nashatyrev on 10.11.2016.
 *
 * @see JournalSource
 * @see PruneRuleSet
 */
public class PruneManager {

    private JournalSource journal;

    @Autowired
    private IndexedBlockStore blockStore;

    private int pruneBlocksCnt;
    private int forkDepth;

    @Autowired
    private PruneManager(SystemProperties config) {
        pruneBlocksCnt = config.databasePruneDepth();
        forkDepth = forkDepth();
    }

    public PruneManager(IndexedBlockStore blockStore, JournalSource journal, int pruneBlocksCnt) {
        this.blockStore = blockStore;
        this.journal = journal;
        this.pruneBlocksCnt = pruneBlocksCnt;
        this.forkDepth = forkDepth();
    }

    private int forkDepth() {
        return pruneBlocksCnt < Constants.getMAX_FORK_BLOCKS() ? pruneBlocksCnt : Constants.getMAX_FORK_BLOCKS();
    }

    @Autowired
    public void setStateSource(StateSource stateSource) {
        journal = stateSource.getJournalSource();
    }

    public void blockCommitted(BlockHeader block) {
        if (pruneBlocksCnt < 0) return; // pruning disabled

        journal.commitUpdates(block.getHash());

        // fork management
        long forkBlockNum = block.getNumber() - forkDepth;
        if (forkBlockNum < 0) return;

        List<Block> pruneBlocks = blockStore.getBlocksByNumber(forkBlockNum);
        Block chainBlock = blockStore.getChainBlockByNumber(forkBlockNum);
        for (Block pruneBlock : pruneBlocks) {
            if (journal.hasUpdate(pruneBlock.getHash())) {
                if (chainBlock.isEqual(pruneBlock)) {
                    journal.processUpdate(pruneBlock.getHash(), PruneRuleSet.AcceptChanges);
                } else {
                    journal.processUpdate(pruneBlock.getHash(), PruneRuleSet.RevertChanges);
                }
            }
        }

        // main chain pruning
        long pruneBlockNum = block.getNumber() - pruneBlocksCnt;
        if (pruneBlockNum < 0) return;

        if (pruneBlockNum != forkBlockNum) {
            chainBlock = blockStore.getChainBlockByNumber(pruneBlockNum);
        }

        if (journal.hasUpdate(chainBlock.getHash())) {
            journal.processUpdate(chainBlock.getHash(), PruneRuleSet.PropagateDeletions);
        }
    }
}
