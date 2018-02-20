package org.ethereum.db.prune;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.CountingQuotientFilter;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.QuotientFilter;
import org.ethereum.datasource.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
        int nodesDeleted = 0;
        for (Chain fork : segment.forks) {
            nodesDeleted += revert(fork);
        }
        nodesDeleted += persist(segment.main);

        if (logger.isTraceEnabled()) logger.trace("nodes deleted: {}, filter load: {}/{}: {}, distinct collisions: {}",
                nodesDeleted,
                ((CountingQuotientFilter) filter).getEntryNumber(), ((CountingQuotientFilter) filter).getMaxInsertions(),
                String.format("%.4f", (double) ((CountingQuotientFilter) filter).getEntryNumber() /
                        ((CountingQuotientFilter) filter).getMaxInsertions()),
                ((CountingQuotientFilter) filter).getCollisionNumber());

        // delete updates
        segment.main.getHashes().forEach(journal::delete);
        for (Chain chain : segment.forks) {
            chain.getHashes().forEach(journal::delete);
        }

        logger.trace(segment + " pruned in {}ms", System.currentTimeMillis() - t);
    }

    private int revert(Chain chain) {
        int nodesDeleted = 0;
        if (logger.isTraceEnabled())
            logger.trace("<~ reverting " + chain + ": " + strSample(chain.getHashes()));

        for (byte[] hash : chain.getHashes()) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("reverting chain " + chain + " failed: can't fetch update " + Hex.toHexString(hash));
                return 0;
            }
            // clean up filter
            update.getInsertedKeys().forEach(filter::remove);
            // revert inserted keys
            for (byte[] key : update.getInsertedKeys()) {
                if (!filter.maybeContains(key)) {
                    ++nodesDeleted;
                    storage.delete(key);
                }
            }
        }

        return nodesDeleted;
    }

    private int persist(Chain chain) {
        int nodesDeleted = 0;
        if (logger.isTraceEnabled())
            logger.trace("<~ persisting " + chain + ": " + strSample(chain.getHashes()));

        for (byte[] hash : chain.getHashes()) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("pruning failed: can't fetch update of main chain " + Hex.toHexString(hash));
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
}
