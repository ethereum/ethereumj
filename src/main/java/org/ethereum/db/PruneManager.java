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

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.JournalSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Anton Nashatyrev on 10.11.2016.
 */
public class PruneManager {

    private JournalSource journal;

    @Autowired
    private IndexedBlockStore blockStore;

    private int pruneBlocksCnt;

    @Autowired
    private PruneManager(SystemProperties config) {
        pruneBlocksCnt = config.databasePruneDepth();
    }

    public PruneManager(IndexedBlockStore blockStore, JournalSource journal, int pruneBlocksCnt) {
        this.blockStore = blockStore;
        this.journal = journal;
        this.pruneBlocksCnt = pruneBlocksCnt;
    }

    @Autowired
    public void setStateSource(StateSource stateSource) {
        journal = stateSource.getJournalSource();
    }

    public void blockCommitted(BlockHeader block) {
        if (pruneBlocksCnt < 0) return; // pruning disabled

        journal.commitUpdates(block.getHash());
        long pruneBlockNum = block.getNumber() - pruneBlocksCnt;
        if (pruneBlockNum < 0) return;

        List<Block> pruneBlocks = blockStore.getBlocksByNumber(pruneBlockNum);
        Block chainBlock = blockStore.getChainBlockByNumber(pruneBlockNum);
        for (Block pruneBlock : pruneBlocks) {
            if (journal.hasUpdate(pruneBlock.getHash())) {
                if (chainBlock.isEqual(pruneBlock)) {
                    journal.persistUpdate(pruneBlock.getHash());
                } else {
                    journal.revertUpdate(pruneBlock.getHash());
                }
            }
        }
    }
}
