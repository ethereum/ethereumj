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
import org.ethereum.datasource.Source;
import org.ethereum.db.prune.Segment;
import org.ethereum.db.prune.Pruner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages state pruning part of block processing.
 *
 * <p>
 *     Constructs chain segments and prune them
 *     when they are complete
 *
 * Created by Anton Nashatyrev on 10.11.2016.
 *
 * @see Segment
 * @see Pruner
 */
public class PruneManager {

    private JournalSource<?> journalSource;

    @Autowired
    private IndexedBlockStore blockStore;

    private int pruneBlocksCnt;

    private Segment segment;
    private Pruner pruner;

    private static final int SEGMENT_MAX_SIZE = 64;
    private int segmentOptimalSize;

    @Autowired
    private PruneManager(SystemProperties config) {
        pruneBlocksCnt = config.databasePruneDepth();
    }

    public PruneManager(IndexedBlockStore blockStore, JournalSource<?> journalSource,
                        Source<byte[], ?> pruneStorage, int pruneBlocksCnt) {
        this.blockStore = blockStore;
        this.journalSource = journalSource;
        this.pruneBlocksCnt = pruneBlocksCnt;
        this.segmentOptimalSize = Math.min(SEGMENT_MAX_SIZE, pruneBlocksCnt / 4);

        if (journalSource != null && pruneStorage != null)
            this.pruner = new Pruner(journalSource.getJournal(), pruneStorage);
    }

    @Autowired
    public void setStateSource(StateSource stateSource) {
        journalSource = stateSource.getJournalSource();
        pruner = new Pruner(journalSource.getJournal(), stateSource.getNoJournalSource());
    }

    public void blockCommitted(BlockHeader block) {
        if (pruneBlocksCnt < 0) return; // pruning disabled

        journalSource.commitUpdates(block.getHash());

        long pruneBlockNum = block.getNumber() - pruneBlocksCnt;
        if (pruneBlockNum < 0) return;

        List<Block> pruneBlocks = blockStore.getBlocksByNumber(pruneBlockNum);
        Block chainBlock = blockStore.getChainBlockByNumber(pruneBlockNum);

        if (segment == null) {
            if (pruneBlocks.size() == 1)    // wait for a single chain
                segment = new Segment(chainBlock);
            return;
        }

        Segment.Tracker tracker = segment.startTracking();
        tracker.addMain(chainBlock);
        tracker.addAll(pruneBlocks);
        tracker.commit();

        if (segment.size() >= segmentOptimalSize && segment.isComplete()) {
            List<byte[]> upcoming = upcomingBlockHashes(segment.getMaxNumber());
            pruner.prune(segment, upcoming);
            segment = new Segment(chainBlock);
        }
    }

    private List<byte[]> upcomingBlockHashes(long fromBlock) {
        List<byte[]> upcomingHashes = new ArrayList<>();
        long max = blockStore.getMaxNumber();
        for (long num = fromBlock; num <= max; num++) {
            List<Block> blocks = blockStore.getBlocksByNumber(num);
            List<byte[]> hashes = blocks.stream().map(Block::getHash).collect(Collectors.toList());
            upcomingHashes.addAll(hashes);
        }
        return upcomingHashes;
    }
}
