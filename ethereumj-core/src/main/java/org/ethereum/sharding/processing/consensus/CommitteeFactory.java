package org.ethereum.sharding.processing.consensus;

import org.ethereum.sharding.processing.state.Committee;

/**
 * An interface of committee factory.
 *
 * <p>
 *     Used to produce new shards and committees array.
 *
 * <p>
 *     in: {@code [validators]} <br/>
 *     out: {@code [slot: [shardId, [validators]]]}
 *
 *
 * @see Committee
 *
 * @author Mikhail Kalinin
 * @since 14.09.2018
 */
public interface CommitteeFactory {

    /**
     * Creates new committees set.
     *
     * @param seed seed for random shuffling
     * @param validators array of active validator numbers
     * @param startShard shard id that first committee in resulting array will be assigned to
     *
     * @return shards and committee array for each slot of cycle
     *
     * @see BeaconConstants#CYCLE_LENGTH
     */
    Committee[][] create(byte[] seed, int[] validators, int startShard);
}
