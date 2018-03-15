package org.ethereum.db.prune;

import org.ethereum.core.Block;
import org.ethereum.datasource.CountingQuotientFilter;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.QuotientFilter;
import org.ethereum.datasource.Source;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.IndexedBlockStore.BlockInfo;
import org.ethereum.util.ByteArraySet;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.util.Collections.singletonList;

/**
 * Created by Anton Nashatyrev on 12.03.2018.
 */
public class Pruner2 {
    private static final int FILTER_MAX_SIZE = Integer.MAX_VALUE >> 1; // that filter will consume ~3g of mem
    private static final int FILTER_ENTRIES_DISTANT = 1 << 11;

    private Source<byte[], ?> storage;
    private Source<byte[], JournalSource.Update> journal;
    private IndexedBlockStore blockStore;
    private QuotientFilter insertedFilter;

    private int pruneMainChainSize;
    private int pruneForksSize;

    public int statKeysInserted = 0;
    public int statKeysDeleted = 0;
    public int statKeysDeletesRejected = 0;

    public Pruner2(Source<byte[], ?> storage, Source<byte[], JournalSource.Update> journal,
                   IndexedBlockStore blockStore, int pruneMainChainSize, int pruneForksSize) {
        this.storage = storage;
        this.journal = journal;
        this.blockStore = blockStore;

        this.pruneForksSize = max(pruneForksSize, 192);
        this.pruneMainChainSize = max(pruneMainChainSize, this.pruneForksSize);
    }

    public void lazyInit() {
        if (insertedFilter != null) return;

        this.insertedFilter = instantiateFilter(pruneMainChainSize);

        // fill the insertedFilter with data from previous
        // main blocks and previous fork blocks at depth < 192
        Block bestBlock = blockStore.getBestBlock();
        long bestNum = bestBlock.getNumber();
        for (int i = 0; i < pruneMainChainSize && i <= bestNum; i++) {
            List<BlockInfo> blocks = i < pruneForksSize ?
                    blockStore.getBlockInfos(bestNum - i) :
                    singletonList(blockStore.getChainBlockInfo(bestNum - i));

            blocks.stream().map(b -> journal.get(b.getHash())).forEach(this::feed);
        }
    }

    public void feed(JournalSource.Update update) {
        if (update == null) return;
        update.getInsertedKeys().forEach(insertedFilter::insert);
        statKeysInserted += update.getInsertedKeys().size();
    }

    public void prune(int bestBlock) {
        int pruneMainHeight = bestBlock - pruneMainChainSize;
        if (pruneMainHeight > 0) {
            BlockInfo blockToPrune = blockStore.getChainBlockInfo(pruneMainHeight);
            pruneMainBlock(blockToPrune);
        }

        int pruneForkHeight = bestBlock - pruneForksSize;
        if (pruneForkHeight > 0) {
            List<BlockInfo> blocks = blockStore.getBlockInfos(pruneForkHeight);
            blocks.stream().filter(b -> !b.isMainChain()).forEach(this::pruneFork);
        }
    }

    private void pruneMainBlock(BlockInfo blockInfo) {
        JournalSource.Update update = journal.get(blockInfo.getHash());
        if (update == null) return;

        int[] deleteCnt = new int[1];
        update.getDeletedKeys().stream()
                .filter(k -> !insertedFilter.maybeContains(k))
                .forEach(k -> {
                    storage.delete(k);
                    deleteCnt[0]++;
                });
        statKeysDeleted += deleteCnt[0];
        statKeysDeletesRejected += update.getDeletedKeys().size() - deleteCnt[0];

        update.getInsertedKeys().forEach(insertedFilter::remove);
    }

    private void pruneFork(BlockInfo blockInfo) {
        JournalSource.Update update = journal.get(blockInfo.getHash());
        if (update == null) return;

        // we need to prune the whole fork chain in a single pass
        // to track the cases when an existing key was initially deleted in
        // the fork and then inserted. In such cases we shouldn't delete this
        // key from storage, e.g. :
        // node with key k1 exists from ancient times
        // k1 exists ------(b1)-------(b2           )-------(b3           ) -------> (b4) k1 still exists
        //                  \-------- (b2' delete k1) ----> (b3' insert k1)
        // If we have 'insert [k1]' in block (bn') in the branch starting from (b2') there are two cases:
        // 1. The key [k1] doesn't exist in main block (b1)
        // 2. The key [k1] existed in (b1) but was deleted in some previous fork block [b2'...b(n-1)']
        // In the case 2 we shouldn't delete [k1] when pruning the fork
        // In the case 1 there are 2 subcases:
        //   1a. The [k1] 100% was not inserted in any block starting from (b2) (including forks)
        //   1b. The [k1] maybe was inserted in some block(s) starting from (b2) (including forks)
        // This is tracked by counting QuotientFilter
        // In case 1a we can safely delete [k1] from storage
        // In case 1b we shouldn't delete [k1] from storage. It could be deleted later if [k1] was inserted
        // in some fork

        pruneForkRecursive(blockStore.getBlockByHash(blockInfo.getHash()), new ByteArraySet());
    }

    private void pruneForkRecursive(Block b, ByteArraySet deletedKeys) {
        // this procedure may not 100% clear obsolete nodes and may leave
        // some garbage in storage (presumably not very high %%)
        // e.g. the following case will leave the key [k1]
        // --(b1)------------------------------------------------------> main
        //     \--->(b2' insert k1)-->(b3' delete k1)-->(b4' insert k1)
        //
        // when pruning b2' we still have [k1] in the insertedFilter (due to b4')
        // when pruning b3' we will add [k1] to deletedKeys
        // when pruning b4' [k1] is no more in the insertedFilter but it is now in deletedKeys

        JournalSource.Update update = journal.get(b.getHash());

        update.getInsertedKeys().forEach(insertedFilter::remove);
        update.getInsertedKeys().stream()
                .filter(k -> !deletedKeys.contains(k) && !insertedFilter.maybeContains(k))
                .forEach(storage::delete);

        journal.delete(b.getHash());

        List<Block> children = getChildren(b);
        if (!children.isEmpty()) {
            ByteArraySet deletedKeys1 = new ByteArraySet();
            deletedKeys1.addAll(deletedKeys);
            deletedKeys1.addAll(update.getDeletedKeys());
            for (Block child : children) {
                pruneForkRecursive(child, deletedKeys1);
            }
        }
    }

    private List<Block> getChildren(Block b) {
        List<Block> ret = new ArrayList<>();
        List<Block> childBlocks = blockStore.getBlocksByNumber(b.getNumber() + 1);
        for (Block maybeChildBlock : childBlocks) {
            if (b.isParentOf(maybeChildBlock)) {
                ret.add(maybeChildBlock);
            }
        }
        return ret;
    }

    private QuotientFilter instantiateFilter(int blocksCnt) {
        int size = Math.min(FILTER_ENTRIES_DISTANT * blocksCnt, FILTER_MAX_SIZE);
        return CountingQuotientFilter.create(size, size);
    }
}
