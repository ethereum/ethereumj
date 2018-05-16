package org.ethereum.db.prune;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.CountingQuotientFilter;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.QuotientFilter;
import org.ethereum.datasource.Source;
import org.ethereum.util.ByteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * This class is responsible for state pruning.
 *
 * <p>
 *     Taking the information supplied by {@link #journal} (check {@link JournalSource} for details)
 *     removes unused nodes from the {@link #storage}.
 *     There are two types of unused nodes:
 *     nodes not references in the trie after N blocks from the current one and
 *     nodes which were inserted in the forks that finally were not accepted
 *
 * <p>
 *     Each prune session uses a certain chain {@link Segment}
 *     which is going to be 'pruned'. To be confident that live nodes won't be removed,
 *     pruner must be initialized with the top of the chain, see {@link #init(List, int)}}.
 *     And after that it must be fed with each newly processed block, see {@link #feed(JournalSource.Update)}.
 *     {@link QuotientFilter} ({@link CountingQuotientFilter} implementation in particular) instance is used to
 *     efficiently keep upcoming inserts in memory and protect newly inserted nodes from being deleted during
 *     prune session. The filter is constantly recycled in {@link #prune(Segment)} method.
 *
 * <p>
 *     When 'prune.maxDepth' param is quite big, it becomes not efficient to keep reverted nodes until prune block number has come.
 *     Hence Pruner has two step mode to mitigate memory consumption, second step is initiated by {@link #withSecondStep(List, int)}.
 *     In that mode nodes from not accepted forks are deleted from storage immediately but main chain deletions are
 *     postponed for the second step.
 *     Second step uses another one instance of QuotientFilter with less memory impact, check {@link #instantiateFilter(int, int)}.
 *
 * <p>
 *     Basically, prune session initiated by {@link #prune(Segment)} method
 *     consists of 3 steps: first, it reverts forks, then it persists main chain,
 *     after that it recycles {@link #journal} by removing processed updates from it.
 *     During the session reverted and deleted nodes are propagated to the {@link #storage} immediately.
 *
 * @author Mikhail Kalinin
 * @since 25.01.2018
 */
public class Pruner {

    private static final Logger logger = LoggerFactory.getLogger("prune");

    Source<byte[], JournalSource.Update> journal;
    Source<byte[], ?> storage;
    QuotientFilter filter;
    QuotientFilter distantFilter;
    boolean ready = false;

    private static class Stats {
        int collisions = 0;
        int deleted = 0;
        double load = 0;
        @Override
        public String toString() {
            return String.format("load %.4f, collisions %d, deleted %d", load, collisions, deleted);
        }
    }
    Stats maxLoad = new Stats();
    Stats maxCollisions = new Stats();
    int maxKeysInMemory = 0;
    int statsTracker = 0;

    Stats distantMaxLoad = new Stats();
    Stats distantMaxCollisions = new Stats();

    public Pruner(Source<byte[], JournalSource.Update> journal, Source<byte[], ?> storage) {
        this.storage = storage;
        this.journal = journal;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean init(List<byte[]> forkWindow, int sizeInBlocks) {
        if (ready) return true;

        if (!forkWindow.isEmpty() && journal.get(forkWindow.get(0)) == null) {
            logger.debug("pruner init aborted: can't fetch update " + toHexString(forkWindow.get(0)));
            return false;
        }

        QuotientFilter filter = instantiateFilter(sizeInBlocks, FILTER_ENTRIES_FORK);
        for (byte[] hash : forkWindow) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("pruner init aborted: can't fetch update " + toHexString(hash));
                return false;
            }
            update.getInsertedKeys().forEach(filter::insert);
        }

        this.filter = filter;
        return ready = true;
    }

    public boolean withSecondStep() {
        return distantFilter != null;
    }

    public void withSecondStep(List<byte[]> mainChainWindow, int sizeInBlocks) {
        if (!ready) return;

        QuotientFilter filter = instantiateFilter(sizeInBlocks, FILTER_ENTRIES_DISTANT);

        if (!mainChainWindow.isEmpty()) {
            int i = mainChainWindow.size() - 1;
            for (; i >= 0; i--) {
                byte[] hash = mainChainWindow.get(i);
                JournalSource.Update update = journal.get(hash);
                if (update == null) {
                    break;
                }
                update.getInsertedKeys().forEach(filter::insert);
            }
            logger.debug("distant filter initialized with set of " + (i < 0 ? mainChainWindow.size() : mainChainWindow.size() - i) +
                    " hashes, last hash " + toHexString(mainChainWindow.get(i < 0 ? 0 : i)));
        } else {
            logger.debug("distant filter initialized with empty set");
        }

        this.distantFilter = filter;
    }

    private static final int FILTER_ENTRIES_FORK = 1 << 13; // approximate number of nodes per block
    private static final int FILTER_ENTRIES_DISTANT = 1 << 11;
    private static final int FILTER_MAX_SIZE = Integer.MAX_VALUE >> 1; // that filter will consume ~3g of mem
    private QuotientFilter instantiateFilter(int blocksCnt, int entries) {
        int size = Math.min(entries * blocksCnt, FILTER_MAX_SIZE);
        return CountingQuotientFilter.create(size, size);
    }

    public boolean init(byte[] ... upcoming) {
        return init(Arrays.asList(upcoming), 192);
    }

    public void feed(JournalSource.Update update) {
        if (ready)
            update.getInsertedKeys().forEach(filter::insert);
    }

    public void prune(Segment segment) {
        if (!ready) return;
        assert segment.isComplete();

        logger.trace("prune " + segment);

        long t = System.currentTimeMillis();
        Pruning pruning = new Pruning();
        // important for fork management, check Pruning#insertedInMainChain and Pruning#insertedInForks for details
        segment.forks.sort((f1, f2) -> Long.compare(f1.startNumber(), f2.startNumber()));
        segment.forks.forEach(pruning::revert);

        // delete updates
        for (Chain chain : segment.forks) {
            chain.getHashes().forEach(journal::delete);
        }

        int nodesPostponed = 0;
        if (withSecondStep()) {
            nodesPostponed = postpone(segment.main);
        } else {
            pruning.nodesDeleted += persist(segment.main);
            segment.main.getHashes().forEach(journal::delete);
        }

        if (logger.isTraceEnabled()) logger.trace("nodes {}, keys in mem: {}, filter load: {}/{}: {}, distinct collisions: {}",
                (withSecondStep() ? "postponed: " + nodesPostponed : "deleted: " + pruning.nodesDeleted),
                pruning.insertedInForks.size() + pruning.insertedInMainChain.size(),
                ((CountingQuotientFilter) filter).getEntryNumber(), ((CountingQuotientFilter) filter).getMaxInsertions(),
                String.format("%.4f", (double) ((CountingQuotientFilter) filter).getEntryNumber() /
                        ((CountingQuotientFilter) filter).getMaxInsertions()),
                ((CountingQuotientFilter) filter).getCollisionNumber());

        if (logger.isDebugEnabled()) {
            int collisions = ((CountingQuotientFilter) filter).getCollisionNumber();
            double load = (double) ((CountingQuotientFilter) filter).getEntryNumber() /
                    ((CountingQuotientFilter) filter).getMaxInsertions();
            if (collisions > maxCollisions.collisions) {
                maxCollisions.collisions = collisions;
                maxCollisions.load = load;
                maxCollisions.deleted = pruning.nodesDeleted;
            }
            if (load > maxLoad.load) {
                maxLoad.load = load;
                maxLoad.collisions = collisions;
                maxLoad.deleted = pruning.nodesDeleted;
            }
            maxKeysInMemory = Math.max(maxKeysInMemory, pruning.insertedInForks.size() + pruning.insertedInMainChain.size());

            if (++statsTracker % 100 == 0) {
                logger.debug("fork filter: max load: " + maxLoad);
                logger.debug("fork filter: max collisions: " + maxCollisions);
                logger.debug("fork filter: max keys in mem: " + maxKeysInMemory);
            }
        }

        logger.trace(segment + " pruned in {}ms", System.currentTimeMillis() - t);
    }

    public void persist(byte[] hash) {
        if (!ready || !withSecondStep()) return;

        logger.trace("persist [{}]", toHexString(hash));

        long t = System.currentTimeMillis();
        JournalSource.Update update = journal.get(hash);
        if (update == null) {
            logger.debug("skip [{}]: can't fetch update", HashUtil.shortHash(hash));
            return;
        }

        // persist deleted keys
        int nodesDeleted = 0;
        for (byte[] key : update.getDeletedKeys()) {
            if (!filter.maybeContains(key) && !distantFilter.maybeContains(key)) {
                ++nodesDeleted;
                storage.delete(key);
            }
        }
        // clean up filter
        update.getInsertedKeys().forEach(distantFilter::remove);
        // delete update
        journal.delete(hash);

        if (logger.isDebugEnabled()) {
            int collisions = ((CountingQuotientFilter) distantFilter).getCollisionNumber();
            double load = (double) ((CountingQuotientFilter) distantFilter).getEntryNumber() /
                    ((CountingQuotientFilter) distantFilter).getMaxInsertions();
            if (collisions > distantMaxCollisions.collisions) {
                distantMaxCollisions.collisions = collisions;
                distantMaxCollisions.load = load;
                distantMaxCollisions.deleted = nodesDeleted;
            }
            if (load > distantMaxLoad.load) {
                distantMaxLoad.load = load;
                distantMaxLoad.collisions = collisions;
                distantMaxLoad.deleted = nodesDeleted;
            }
            if (statsTracker % 100 == 0) {
                logger.debug("distant filter: max load: " + distantMaxLoad);
                logger.debug("distant filter: max collisions: " + distantMaxCollisions);
            }
        }

        if (logger.isTraceEnabled()) logger.trace("[{}] persisted in {}ms: {}/{} ({}%) nodes deleted, filter load: {}/{}: {}, distinct collisions: {}",
                HashUtil.shortHash(hash), System.currentTimeMillis() - t, nodesDeleted, update.getDeletedKeys().size(),
                nodesDeleted * 100 / update.getDeletedKeys().size(),
                ((CountingQuotientFilter) distantFilter).getEntryNumber(),
                ((CountingQuotientFilter) distantFilter).getMaxInsertions(),
                String.format("%.4f", (double) ((CountingQuotientFilter) distantFilter).getEntryNumber() /
                        ((CountingQuotientFilter) distantFilter).getMaxInsertions()),
                ((CountingQuotientFilter) distantFilter).getCollisionNumber());
    }

    private int postpone(Chain chain) {
        if (logger.isTraceEnabled())
            logger.trace("<~ postponing " + chain + ": " + strSample(chain.getHashes()));

        int nodesPostponed = 0;
        for (byte[] hash : chain.getHashes()) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("postponing: can't fetch update " + toHexString(hash));
                continue;
            }
            // feed distant filter
            update.getInsertedKeys().forEach(distantFilter::insert);
            // clean up fork filter
            update.getInsertedKeys().forEach(filter::remove);

            nodesPostponed += update.getDeletedKeys().size();
        }

        return nodesPostponed;
    }

    private int persist(Chain chain) {
        if (logger.isTraceEnabled())
            logger.trace("<~ persisting " + chain + ": " + strSample(chain.getHashes()));

        int nodesDeleted = 0;
        for (byte[] hash : chain.getHashes()) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("pruning aborted: can't fetch update of main chain " + toHexString(hash));
                return 0;
            }
            // persist deleted keys
            for (byte[] key : update.getDeletedKeys()) {
                if (!filter.maybeContains(key)) {
                    ++nodesDeleted;
                    storage.delete(key);
                }
            }
            // clean up filter
            update.getInsertedKeys().forEach(filter::remove);
        }

        return nodesDeleted;
    }

    private String strSample(Collection<byte[]> hashes) {
        String sample = hashes.stream().limit(3)
                .map(HashUtil::shortHash).collect(Collectors.joining(", "));
        if (hashes.size() > 3) {
            sample += ", ... (" + hashes.size() + " total)";
        }
        return sample;
    }

    private class Pruning {

        // track nodes inserted and deleted in forks
        // to avoid deletion of those nodes which were originally inserted in the main chain
        Set<byte[]> insertedInMainChain = new ByteArraySet();
        Set<byte[]> insertedInForks = new ByteArraySet();
        int nodesDeleted = 0;

        private void revert(Chain chain) {
            if (logger.isTraceEnabled())
                logger.trace("<~ reverting " + chain + ": " + strSample(chain.getHashes()));

            for (byte[] hash : chain.getHashes()) {
                JournalSource.Update update = journal.get(hash);
                if (update == null) {
                    logger.debug("reverting chain " + chain + " aborted: can't fetch update " + toHexString(hash));
                    return;
                }
                // clean up filter
                update.getInsertedKeys().forEach(filter::remove);

                // node that was deleted in fork considered as a node that had earlier been inserted in main chain
                update.getDeletedKeys().forEach(key -> {
                    if (!insertedInForks.contains(key)) {
                        insertedInMainChain.add(key);
                    }
                });
                update.getInsertedKeys().forEach(key -> {
                    if (!insertedInMainChain.contains(key)) {
                        insertedInForks.add(key);
                    }
                });

                // revert inserted keys
                for (byte[] key : update.getInsertedKeys()) {
                    if (!filter.maybeContains(key) && !insertedInMainChain.contains(key)) {
                        ++nodesDeleted;
                        storage.delete(key);
                    }
                }
            }
        }
    }
}
