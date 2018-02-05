package org.ethereum.db.prune;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.JournalSource;
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
 *     nodes which were inserted in the forks which finally were not accepted
 *
 * <p>
 *     Each prune session uses a certain chain {@link Segment}
 *     which is going to be 'pruned' and upcoming block hashes to
 *     take in account updates made in blocks imported after segment blocks.
 *
 * <p>
 *     Basically, prune session initiated by {@link #prune(Segment, List)} method
 *     consists of 4 steps: first, it reverts forks, then it persists main chain
 *     and evicts upcoming updates from pruning and
 *     finally it propagates deletes to the {@link #storage}.
 *
 * @author Mikhail Kalinin
 * @since 25.01.2018
 */
public class Pruner {

    private static final Logger logger = LoggerFactory.getLogger("prune");

    Source<byte[], JournalSource.Update> journal;
    Source<byte[], ?> storage;

    public Pruner(Source<byte[], JournalSource.Update> journal, Source<byte[], ?> storage) {
        this.storage = storage;
        this.journal = journal;
    }

    public void prune(Segment segment, List<byte[]> upcoming) {
        assert segment.isComplete();

        logger.trace("prune " + segment + " segment");

        if (!upcoming.isEmpty() && journal.get(upcoming.get(0)) == null) {
            logger.debug("pruning failed: can't fetch upcoming update " + Hex.toHexString(upcoming.get(0)));
            return;
        }

        Pruning pruning = new Pruning();
        segment.forks.forEach(pruning::revert);
        pruning.persist(segment.main);
        pruning.evict(upcoming);

        if (pruning.propagating != null) {
            if (logger.isTraceEnabled())
                logger.trace("<~ propagating: " + strSample(pruning.propagating));

            pruning.propagating.forEach(storage::delete);
        }

        // delete updates
        segment.main.getHashes().forEach(journal::delete);
        for (Chain chain : segment.forks) {
            chain.getHashes().forEach(journal::delete);
        }
    }

    public void prune(Segment segment, byte[] ... upcoming) {
        prune(segment, Arrays.asList(upcoming));
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
        Set<byte[]> propagating = new ByteArraySet();

        void revert(Chain chain) {
            if (logger.isTraceEnabled())
                logger.trace("<~ reverting " + chain + ": " + strSample(chain.getHashes()));

            Set<byte[]> insertedKeys = new ByteArraySet();
            Set<byte[]> deletedKeys = new ByteArraySet();
            for (byte[] hash : chain.getHashes()) {
                JournalSource.Update update = journal.get(hash);
                if (update == null) {
                    logger.debug("reverting chain " + chain + " failed: can't fetch update " + Hex.toHexString(hash));
                    return;
                }
                deletedKeys.addAll(update.getDeletedKeys());
                update.getInsertedKeys().forEach(key -> {
                    if (!deletedKeys.contains(key))         // revert newly introduced keys only
                        insertedKeys.add(key);
                });
            }
            propagating.addAll(insertedKeys);
        }

        void persist(Chain chain) {
            if (logger.isTraceEnabled())
                logger.trace("<~ persisting " + chain + ": " + strSample(chain.getHashes()));

            for (byte[] hash : chain.getHashes()) {
                JournalSource.Update update = journal.get(hash);
                if (update == null) {
                    logger.debug("pruning failed: can't fetch update of main chain " + Hex.toHexString(hash));
                    propagating = null;
                    return;
                }
                propagating.addAll(update.getDeletedKeys());
                propagating.removeAll(update.getInsertedKeys());
            }
        }

        void evict(List<byte[]> upcoming) {
            if (propagating == null)
                return;

            if (logger.isTraceEnabled())
                logger.trace("<~ evicting updates: " + strSample(upcoming));

            for (byte[] hash : upcoming) {
                JournalSource.Update update = journal.get(hash);
                if (update == null) {
                    logger.debug("pruning failed: can't fetch upcoming update " + Hex.toHexString(hash));
                    propagating = null;
                    return;
                }
                propagating.removeAll(update.getInsertedKeys());
            }
        }
    }
}
