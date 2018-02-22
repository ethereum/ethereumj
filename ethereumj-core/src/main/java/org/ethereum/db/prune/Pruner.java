package org.ethereum.db.prune;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.CountingQuotientFilter;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.QuotientFilter;
import org.ethereum.datasource.Source;
import org.ethereum.util.ByteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
 *     pruner must be initialized with the top of the chain, see {@link #init(List)}.
 *     And after that it must be fed with each newly processed block, see {@link #feed(JournalSource.Update)}.
 *     {@link QuotientFilter} ({@link CountingQuotientFilter} implementation in particular) instance is used to
 *     efficiently keep upcoming inserts in memory and protect newly inserted nodes from being deleted during
 *     prune session. The filter is constantly recycled in {@link #prune(Segment)} method.
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

    public Pruner(Source<byte[], JournalSource.Update> journal, Source<byte[], ?> storage) {
        this.storage = storage;
        this.journal = journal;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean init(List<byte[]> hashes) {
        if (ready) return true;

        if (!hashes.isEmpty() && journal.get(hashes.get(0)) == null) {
            logger.debug("pruning init failed: can't fetch update " + Hex.toHexString(hashes.get(0)));
            return false;
        }

        QuotientFilter filter = CountingQuotientFilter.create(1_000_000, 1_000_000);
        for (byte[] hash : hashes) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("pruning init failed: can't fetch update " + Hex.toHexString(hash));
                return false;
            }
            update.getInsertedKeys().forEach(filter::insert);
        }

        this.filter = filter;
        return ready = true;
    }

    public boolean init(byte[] ... upcoming) {
        return init(Arrays.asList(upcoming));
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
        pruning.persist(segment.main);

        if (logger.isTraceEnabled()) logger.trace("nodes deleted: {}, keys in mem: {}, filter load: {}/{}: {}, distinct collisions: {}",
                pruning.nodesDeleted, pruning.insertedInForks.size() + pruning.insertedInMainChain.size(),
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
                logger.debug("max load: " + maxLoad);
                logger.debug("max collisions: " + maxCollisions);
                logger.debug("max keys in mem: " + maxKeysInMemory);
            }
        }

        // delete updates
        segment.main.getHashes().forEach(journal::delete);
        for (Chain chain : segment.forks) {
            chain.getHashes().forEach(journal::delete);
        }

        logger.trace(segment + " pruned in {}ms", System.currentTimeMillis() - t);
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
                    logger.debug("reverting chain " + chain + " failed: can't fetch update " + Hex.toHexString(hash));
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

        private void persist(Chain chain) {
            if (logger.isTraceEnabled())
                logger.trace("<~ persisting " + chain + ": " + strSample(chain.getHashes()));

            for (byte[] hash : chain.getHashes()) {
                JournalSource.Update update = journal.get(hash);
                if (update == null) {
                    logger.debug("pruning failed: can't fetch update of main chain " + Hex.toHexString(hash));
                    return;
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
        }
    }
}
