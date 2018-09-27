package org.ethereum.sharding.processing.consensus;

import org.ethereum.crypto.HashUtil;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.util.FastByteComparisons;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.MIN_COMMITTEE_SIZE;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.SHARD_COUNT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Mikhail Kalinin
 * @since 24.09.2018
 */
public class ShufflingCommitteeFactoryTest {

    @Test
    public void testCreate() {
        int startShard = 3;
        Committee[][] committees = new ShufflingCommitteeFactory().create(HashUtil.randomHash(),
                IntStream.range(1, 3 * SHARD_COUNT * MIN_COMMITTEE_SIZE * CYCLE_LENGTH / 2).toArray(), startShard);

        // each committee dedicated for own shard
        int shard = startShard;
        for (Committee[] slot : committees) {
            for (Committee committee : slot) {
                assertEquals(shard % SHARD_COUNT, committee.getShardId());
                shard += 1;
            }
        }

        // all committees are with the same shard
        committees = new ShufflingCommitteeFactory().create(HashUtil.randomHash(),
                IntStream.range(1, CYCLE_LENGTH + 1).toArray(), startShard);
        for (Committee[] slot : committees) {
            for (Committee committee : slot) {
                assertEquals(startShard % SHARD_COUNT, committee.getShardId());
            }
        }
    }

    @Test
    public void slotsPerShard() {
        ShufflingCommitteeFactory factory = new ShufflingCommitteeFactory();
        assertEquals(SHARD_COUNT / CYCLE_LENGTH, factory.calcShardsPerSlot(ShufflingCommitteeFactory.MAX_SZ));
        assertEquals(3, factory.calcShardsPerSlot(2 * MIN_COMMITTEE_SIZE * CYCLE_LENGTH * 2));
        assertEquals(2, factory.calcShardsPerSlot(2 * MIN_COMMITTEE_SIZE * CYCLE_LENGTH));
        assertEquals(1, factory.calcShardsPerSlot(CYCLE_LENGTH));
        assertEquals(1, factory.calcShardsPerSlot(CYCLE_LENGTH - 1));
        assertEquals(1, factory.calcShardsPerSlot(1));

        assertEquals(CYCLE_LENGTH, factory.calcSlotsPerShard(1));
        assertEquals(2, factory.calcSlotsPerShard(CYCLE_LENGTH * MIN_COMMITTEE_SIZE / 2));
        assertEquals(4, factory.calcSlotsPerShard(CYCLE_LENGTH * MIN_COMMITTEE_SIZE / 3));
        assertEquals(4, factory.calcSlotsPerShard(CYCLE_LENGTH * MIN_COMMITTEE_SIZE / 4));
        assertEquals(8, factory.calcSlotsPerShard(CYCLE_LENGTH * MIN_COMMITTEE_SIZE / 7));
        assertEquals(1, factory.calcSlotsPerShard(CYCLE_LENGTH * MIN_COMMITTEE_SIZE));
        assertEquals(1, factory.calcSlotsPerShard(CYCLE_LENGTH * MIN_COMMITTEE_SIZE * 2));
    }

    @Test
    public void testShuffle() {
        int[] in = IntStream.range(1, BeaconConstants.MIN_COMMITTEE_SIZE * 1024).toArray();
        int[] out = new ShufflingCommitteeFactory().shuffle(HashUtil.randomHash(), in);
        assertFalse(Arrays.equals(in, out));
        Arrays.sort(out);
        assertArrayEquals(in, out);
    }

    @Test
    public void testSplit() {
        // check full
        int[] in = IntStream.range(1, BeaconConstants.MIN_COMMITTEE_SIZE * 1024).toArray();
        int[][] out = new ShufflingCommitteeFactory().split(in, CYCLE_LENGTH);

        checkUniversal(in, out);
        checkGapSizeDistribution(in, out, CYCLE_LENGTH);

        // check sparse
        in = IntStream.range(1, 10).toArray();
        out = new ShufflingCommitteeFactory().split(in, CYCLE_LENGTH);

        checkUniversal(in, out);
        assertEquals(CYCLE_LENGTH, out.length);
        assertEquals(1, out[out.length - 1].length);
        assertEquals(in[in.length - 1], out[out.length - 1][0]);
        checkGapSizeDistribution(in, out, CYCLE_LENGTH);

        in = IntStream.range(1, CYCLE_LENGTH / 4 + 1).toArray();
        out = new ShufflingCommitteeFactory().split(in, CYCLE_LENGTH);

        checkUniversal(in, out);
        assertEquals(CYCLE_LENGTH, out.length);
        assertEquals(1, out[out.length - 1].length);
        assertEquals(in[in.length - 1], out[out.length - 1][0]);
        checkGapSizeDistribution(in, out, CYCLE_LENGTH);

        in = IntStream.range(1, CYCLE_LENGTH).toArray();
        out = new ShufflingCommitteeFactory().split(in, CYCLE_LENGTH);

        checkUniversal(in, out);
        assertEquals(CYCLE_LENGTH, out.length);
        assertEquals(1, out[out.length - 1].length);
        assertEquals(in[in.length - 1], out[out.length - 1][0]);
        checkGapSizeDistribution(in, out, CYCLE_LENGTH);

        // check semi-sparse
        in = IntStream.range(1, CYCLE_LENGTH + CYCLE_LENGTH / 3 + 1).toArray();
        out = new ShufflingCommitteeFactory().split(in, CYCLE_LENGTH);

        checkUniversal(in, out);
        assertEquals(CYCLE_LENGTH, out.length);
        assertEquals(in[in.length - 1], out[out.length - 1][out[out.length - 1].length - 1]);
        checkGapSizeDistribution(in, out, CYCLE_LENGTH);
    }

    void checkUniversal(int[] in, int[][] out) {
        int[] actual = new int[in.length];
        for (int idx = 0, i = 0; i < out.length; i++) {
            for (int j = 0; j < out[i].length; j++)
                actual[idx++] = out[i][j];
        }
        assertArrayEquals(in, actual);
    }

    void checkGapSizeDistribution(int[] in, int[][] out, int slicesQty) {
        int min = Integer.MAX_VALUE, max = 0;
        int cur = 0;
        for (int[] slice : out) {
            if (slice.length == 0) {
                cur += 1;
            } else if (cur > 0) {
                min = Math.min(cur, min);
                max = Math.max(cur, max);
                cur = 0;
            }
        }

        if (in.length > slicesQty) {
            assertEquals(0, max);
            return;
        }

        int spread = slicesQty / in.length;

        if (slicesQty % in.length > 0) {
            assertTrue(min >= spread - 1 && max <= spread);
        } else {
            assertTrue(min == spread - 1 && max == spread - 1);
        }
    }
}
