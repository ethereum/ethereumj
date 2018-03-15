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

import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.Source;
import org.ethereum.db.prune.Pruner2;
import org.ethereum.db.prune.Segment;
import org.ethereum.db.prune.Pruner;

/**
 * Manages state pruning part of block processing.
 *
 * <p>
 *     Constructs chain segments and prune them when they are complete
 *
 * Created by Anton Nashatyrev on 10.11.2016.
 *
 * @see Segment
 * @see Pruner
 */
public class PruneManager {

    private JournalSource<?> journalSource;

    private IndexedBlockStore blockStore;

    private int pruneBlocksCnt;

    private Pruner2 pruner;

    public PruneManager(IndexedBlockStore blockStore, JournalSource<?> journalSource,
                        Source<byte[], ?> pruneStorage, int pruneBlocksCnt) {
        this.blockStore = blockStore;
        this.journalSource = journalSource;
        this.pruneBlocksCnt = pruneBlocksCnt;

        if (journalSource != null && pruneStorage != null)
            this.pruner = new Pruner2(pruneStorage, journalSource.getJournal(), blockStore,
                    pruneBlocksCnt, 192);
    }

    public void blockCommitted(BlockHeader block) {
        if (pruneBlocksCnt < 0) return; // pruning disabled

        JournalSource.Update update = journalSource.commitUpdates(block.getHash());

        pruner.lazyInit();
        pruner.feed(update);
        pruner.prune((int) block.getNumber());
    }
}
