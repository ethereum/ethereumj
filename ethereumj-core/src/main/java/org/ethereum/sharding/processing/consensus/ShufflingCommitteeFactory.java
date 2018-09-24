package org.ethereum.sharding.processing.consensus;

import org.ethereum.crypto.HashUtil;
import org.ethereum.sharding.processing.state.Committee;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.CYCLE_LENGTH;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.MIN_COMMITTEE_SIZE;
import static org.ethereum.sharding.processing.consensus.BeaconConstants.SHARD_COUNT;
import static org.ethereum.util.ByteUtil.byteArrayToInt;

/**
 * Tricky implementation that shuffles and splits {@code validators} array into committees.
 *
 * <p>
 *     Get into description of particular steps to understand how it works.
 *
 * @author Mikhail Kalinin
 * @since 14.09.2018
 */
public class ShufflingCommitteeFactory implements CommitteeFactory {

    @Override
    public Committee[][] create(byte[] seed, int[] validators, int startShard) {

        int shardsPerSlot = calcShardsPerSlot(validators.length);
        int slotsPerShard = calcSlotsPerShard(validators.length);

        int[] shuffled = shuffle(seed, validators);

        int[][] validatorsPerSlot = split(shuffled, CYCLE_LENGTH);

        Committee[][] committees = new Committee[CYCLE_LENGTH][];
        for (int slot = 0; slot < CYCLE_LENGTH; slot++) {
            // if slotsPerShard > 1 then assign same shard for several slots
            // otherwise assign same slot for several shards
            int slotOffset = slotsPerShard > 1 ? slot / slotsPerShard : slot * shardsPerSlot;
            committees[slot] = createSlotCommittees(startShard + slotOffset, shardsPerSlot, validatorsPerSlot[slot]);
        }

        return committees;
    }

    /**
     * Creates shard committees for particular {@code slot}.
     *
     * @param slotStartShard number of shard that slot committees start from
     * @param shardsPerSlot number of shards that this slot will work with
     * @param indices validator indices assigned for the slot
     *
     * @return committees for the slot
     */
    Committee[] createSlotCommittees(int slotStartShard, int shardsPerSlot, int[] indices) {
        int[][] indicesPerCommittee = split(indices, shardsPerSlot);
        Committee[] committees = new Committee[indicesPerCommittee.length];
        for (int idx = 0; idx < committees.length; idx++) {
            int shardId = (slotStartShard + idx) % SHARD_COUNT;
            committees[idx] = new Committee((short) shardId, indicesPerCommittee[idx]);
        }

        return committees;
    }

    /**
     * Returns a number of shards that will pass through cross-linking process in each slot.
     *
     * <p>
     *     Has direct influence on size of the committee.
     *
     * <p>
     *     Returns {@code 1} unless {@code validatorsPerSlot > MIN_COMMITTEE_SIZE * 2}. <br/>
     *     Otherwise, returns {@code value} that results in size of committees lay
     *     between {@code MIN_COMMITTEE_SIZE} and {@code MIN_COMMITTEE_SIZE * 2}. <br/>
     *
     *     Size of committees approaches {@code MIN_COMMITTEE_SIZE * 2} at infinity.
     *
     * <p>
     *     Examples:
     *     <ul>
     *         <li> if {@code totalValidators = 12288} then {@code validatorsPerSlot = 192},
     *              committee size is {@code 192}
     *
     *         <li> if {@code totalValidators = 81920} then {@code validatorsPerSlot = 1280},
     *              committee size is {@code 213}
     *
     *         <li> if {@code totalValidators = 8192000} then {@code validatorsPerSlot = 128000},
     *              committee size is {@code 255}
     *     </ul>
     */
    int calcShardsPerSlot(int totalValidators) {
        int validatorsPerSlot = totalValidators / CYCLE_LENGTH;
        return validatorsPerSlot / (MIN_COMMITTEE_SIZE * 2) + 1;
    }

    /**
     * Returns a number of committees that will have to process the same shard during a cycle.
     *
     * <p>
     *     This function valuable only if {@code validatorsPerSlot < MIN_COMMITTEE_SIZE},
     *     when validators can't compose minimal sized committee. <br/>
     *
     *     If there is an ability to create at least one well sized committee per slot,
     *     then function returns {@code 1} which does not affect the following calculations.
     *
     * <p>
     *     Also, the result is capped at {@code CYCLE_LENGTH} which means that same shard can't
     *     be process more times than a number of slots in cycle. Reasonable thing :)
     *
     * <p>
     *     Examples:
     *     <ul>
     *         <li> for {@code validatorsPerSlot < 4}, returns {@code CYCLE_LENGTH},
     *              hence, whole cycle will be spent on cross-linking of the same shard.
     *
     *         <li> if {@code validatorsPerSlot = 4}, returns {@code 32},
     *              hence, {@code 2} different shards will be cross-linked during the cycle,
     *              cause committees of 32 slots are interpreted as one well size committee with {@code size = 128}
     *
     *         <li> {@code validatorsPerSlot = 64}, returns {@code 2},
     *              hence, {@code 32} different shards will be cross-linked during the cycle,
     *              in other words, one shard is processed by each coupled slots.
     *     </ul>
     */
    int calcSlotsPerShard(int totalValidators) {
        int slotsPerCommittee = 1;
        while (totalValidators * slotsPerCommittee < CYCLE_LENGTH * MIN_COMMITTEE_SIZE &&
                slotsPerCommittee < CYCLE_LENGTH)
            slotsPerCommittee *= 2;

        return slotsPerCommittee;
    }

    /**
     * A tricky split function.
     *
     * <p>
     *     Splits given array into {@code slicesQty} number of slices in a way that
     *     union of output arrays equals input array: <br/>
     *     {@code indices = result[0] V result[1] V ... V result[N]}
     *
     * <p>
     *     Another feature of this function is the way it works when {@code indices.length < slicesQty}:
     *     <ol>
     *         <li> It returns strict {@code sliceQty} number of arrays, gaps are filled with empty arrays
     *         <li> Latest array is always filled
     *         <li> Sizes of gaps are uniformly distributed
     *     </ol>
     * <p>
     *     Examples:
     *     <ul>
     *         <li> {@code split([1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 3) = [[1, 2, 3], [4, 5, 6], [7, 8, 9, 10]]}
     *         <li> {@code split([1, 2, 3], 10) = [[], [], [], [1], [], [], [2], [], [], [3]]}
     *     </ul>
     */
    int[][] split(int[] indices, int slicesQty) {
        int[][] sliced = new int[slicesQty][];
        for (int i = 0; i < slicesQty; i++) {
            int startIdx = indices.length * i / slicesQty;
            int endIdx = indices.length * (i + 1) / slicesQty;
            sliced[i] = new int[endIdx - startIdx];
            System.arraycopy(indices, startIdx, sliced[i], 0, sliced[i].length);
        }

        return sliced;
    }

    /**
     * Returns a new array with shuffled indices.
     *
     * <p>
     *     Uses Fisher-Yates algorithm for array shuffling. <br/>
     *     This function is, also, featured with modulo bias protection,
     *     https://github.com/ethereum/beacon_chain/issues/57.
     *
     * <p>
     *     Fisher-Yates algorithm: <br/>
     *     <code>
     *         for i from 0 to n−2 do <br/>&nbsp;&nbsp;
     *              j ← random integer such that i ≤ j < n <br/>&nbsp;&nbsp;
     *              exchange a[i] and a[j]
     *     </code>
     *
     * <p>
     *     Modulo bias protection: <br/>
     *     <code>
     *         while true <br/>&nbsp;&nbsp;
     *              x = rand % n <br/>&nbsp;&nbsp;
     *              if x < rand_max - rand_max % n <br/>&nbsp;&nbsp;&nbsp;&nbsp;
     *                  break <br/>
     *         return x
     *     </code>
     *
     * <p>
     *     The seed is derived with help of {@link HashUtil#blake2b(byte[])} hash function.
     */
    int[] shuffle(byte[] seed, int[] indices) {
        int sz = indices.length;
        assert sz <= MAX_SZ;

        int[] shuffled = Arrays.copyOf(indices, sz);

        for (int i = 0; i < sz;) {
            seed = blake2b(seed);
            for (int pos = 0; pos < 30 && i < sz; pos += 3) {
                int m = byteArrayToInt(Arrays.copyOfRange(seed, pos, pos + RAND_BYTES));
                int remaining = sz - i;
                int randMax = MAX_SZ - MAX_SZ % remaining;
                if (m < randMax) {
                    int j = m % remaining + i;
                    int idx = shuffled[i];
                    shuffled[i] = shuffled[j];
                    shuffled[j] = idx;
                    i += 1;
                }
            }
        }

        return shuffled;
    }

    private static final int RAND_BYTES = 3;
    private static final int MAX_SZ = 1 << (RAND_BYTES * Byte.SIZE);
}
