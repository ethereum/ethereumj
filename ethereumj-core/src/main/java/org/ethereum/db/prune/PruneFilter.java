package org.ethereum.db.prune;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.CountingQuotientFilter;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.QuotientFilter;
import org.ethereum.datasource.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saves nodes inserted again from being deleted from storage.
 *
 * <p>
 *     Represents an in-memory structure based on probabilistic quotient filter,
 *     uses {@link CountingQuotientFilter} implementation
 *
 * @author Mikhail Kalinin
 * @since 15.02.2018
 */
public class PruneFilter {

    private static final Logger logger = LoggerFactory.getLogger("prune");

    int size;
    boolean init = false;

    QuotientFilter quotient;

    public PruneFilter(int size) {
        this.size = size;
    }

    public void insert(byte[] key) {
        if (init) quotient.insert(key);
    }

    public void remove(byte[] key) {
        if (init) quotient.remove(key);
    }

    public boolean maybeContains(byte[] key) {
        return init && quotient.maybeContains(key);
    }

    boolean init(Source<byte[], JournalSource.Update> journal, List<byte[]> updates) {

        if (init) return true;

        logger.trace("prune filter: init with updates: " + strSample(updates));

        QuotientFilter quotient = CountingQuotientFilter.create(size, size);
        for (byte[] hash : updates) {
            JournalSource.Update update = journal.get(hash);
            if (update == null) {
                logger.debug("prune filter: init failed, can't fetch update " + Hex.toHexString(hash));
                return false;
            }
            update.getInsertedKeys().forEach(quotient::insert);
        }

        this.quotient = quotient;
        return init = true;
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
