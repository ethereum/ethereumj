package org.ethereum.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * Supplies {@link QuotientFilter} with collisions counter map.
 *
 * <p>
 *     Hence it can handle any number of hard and/or soft collisions without performance lack.
 *     While {@link QuotientFilter} experiencing performance problem when collision number tends to 10_000.
 *
 * @author Mikhail Kalinin
 * @since 14.02.2018
 */
public class CountingQuotientFilter extends QuotientFilter {

    long FINGERPRINT_MASK;

    private Map<Long, Counter> counters = new HashMap<>();

    private CountingQuotientFilter(int quotientBits, int remainderBits) {
        super(quotientBits, remainderBits);
        this.FINGERPRINT_MASK = LOW_MASK(QUOTIENT_BITS + REMAINDER_BITS);
    }

    public static CountingQuotientFilter create(long largestNumberOfElements, long startingElements) {
        QuotientFilter filter = QuotientFilter.create(largestNumberOfElements, startingElements);
        return new CountingQuotientFilter(filter.QUOTIENT_BITS, filter.REMAINDER_BITS);
    }

    @Override
    public synchronized void insert(long hash) {
        if (super.maybeContains(hash)) {
            addRef(hash);
        } else {
            super.insert(hash);
        }
    }

    @Override
    public synchronized void remove(long hash) {
        if (super.maybeContains(hash) && delRef(hash) < 0) {
            super.remove(hash);
        }
    }

    @Override
    protected long hash(byte[] bytes) {
        long hash = 1;
        for (byte b : bytes) {
            hash = 31 * hash + b;
        }
        return hash;
    }

    public synchronized int getCollisionNumber() {
        return counters.size();
    }

    public long getEntryNumber() {
        return entries;
    }

    public long getMaxInsertions() {
        return MAX_INSERTIONS;
    }

    private void addRef(long hash) {
        long fp = fingerprint(hash);
        Counter cnt = counters.get(fp);
        if (cnt == null) {
            counters.put(fp, new Counter());
        } else {
            cnt.refs++;
        }
    }

    private int delRef(long hash) {
        long fp = fingerprint(hash);
        Counter cnt = counters.get(fp);
        if (cnt == null) {
            return -1;
        }
        if (--cnt.refs < 1) {
            counters.remove(fp);
        }
        return cnt.refs;
    }

    private long fingerprint(long hash) {
        return hash & FINGERPRINT_MASK;
    }

    private static class Counter {
        int refs = 1;
    }
}
