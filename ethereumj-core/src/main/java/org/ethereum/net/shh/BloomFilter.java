package org.ethereum.net.shh;

import java.util.BitSet;

/**
 * Created by Anton Nashatyrev on 24.09.2015.
 */
public class BloomFilter implements Cloneable {
    private static final int BITS_PER_BLOOM = 3;
    private static final int BLOOM_BYTES = 64;

    BitSet mask = new BitSet(BLOOM_BYTES * 8);
    int[] counters = new int[BLOOM_BYTES * 8];

    private BloomFilter() {
    }

    public static BloomFilter createNone() {
        return new BloomFilter();
    }

    public static BloomFilter createAll() {
        BloomFilter bloomFilter = new BloomFilter();
        bloomFilter.mask.set(0, bloomFilter.mask.length());
        return bloomFilter;
    }

    public BloomFilter(Topic topic) {
        addTopic(topic);
    }

    public BloomFilter(byte[] bloomMask) {
        if (bloomMask.length != BLOOM_BYTES) throw new RuntimeException("Invalid bloom filter array length: " + bloomMask.length);
        mask = BitSet.valueOf(bloomMask);
    }

    private void incCounters(BitSet bs) {
        int idx = -1;
        while(true) {
            idx = bs.nextSetBit(idx + 1);
            if (idx < 0) break;
            counters[idx]++;
        }
    }

    private void decCounters(BitSet bs) {
        int idx = -1;
        while(true) {
            idx = bs.nextSetBit(idx + 1);
            if (idx < 0) break;
            if (counters[idx] > 0) counters[idx]--;
        }
    }

    private BitSet getTopicMask(Topic topic) {
        BitSet topicMask = new BitSet(BLOOM_BYTES * 8);
        for (int i = 0; i < BITS_PER_BLOOM; i++) {
            int x = topic.getBytes()[i] & 0xFF;
            if ((topic.getBytes()[BITS_PER_BLOOM] & (1 << i)) != 0) {
                x += 256;
            }
            topicMask.set(x);
        }
        return topicMask;
    }

    public void addTopic(Topic topic) {
        BitSet topicMask = getTopicMask(topic);
        incCounters(topicMask);
        mask.or(topicMask);
    }

    public void removeTopic(Topic topic) {
        BitSet topicMask = getTopicMask(topic);
        decCounters(topicMask);
        int idx = -1;
        while(true) {
            idx = topicMask.nextSetBit(idx + 1);
            if (idx < 0) break;
            if (counters[idx] == 0) mask.clear(idx);
        }
    }


    public boolean hasTopic(Topic topic) {
        BitSet m = new BloomFilter(topic).mask;
        BitSet m1 = (BitSet) m.clone();
        m1.and(mask);
        return m1.equals(m);
    }

    public byte[] toBytes() {
        byte[] ret = new byte[BLOOM_BYTES];
        byte[] bytes = mask.toByteArray();
        System.arraycopy(bytes, 0, ret, 0, bytes.length);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        return mask.equals(((BloomFilter)obj).mask);
    }

    @Override
    protected BloomFilter clone() {
        try {
            return (BloomFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
