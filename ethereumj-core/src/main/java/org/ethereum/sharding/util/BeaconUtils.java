/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.sharding.util;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.processing.consensus.BeaconConstants;
import org.ethereum.sharding.processing.state.Committee;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.ethereum.sharding.processing.consensus.BeaconConstants.SLOT_DURATION;


/**
 * @author Mikhail Kalinin
 * @since 25.09.2018
 */
public class BeaconUtils {

    /**
     * Shortcut to {@link #cycleStartSlot(long)}
     */
    public static long cycleStartSlot(Beacon block) {
        return cycleStartSlot(block.getSlotNumber());
    }

    /**
     * Returns start slot of a cycle that given slot number does belong to
     */
    public static long cycleStartSlot(long slotNumber) {
        return slotNumber - slotNumber % BeaconConstants.CYCLE_LENGTH;
    }

    /**
     * Scans committees array and returns committee index, {@link Committee.Index}.
     *
     * @param validatorIdx validator index taken from validator set
     * @param committees committees array
     *
     * @return valid committee index if validator is found, empty index otherwise
     */
    public static Committee.Index scanCommittees(int validatorIdx, Committee[][] committees) {
        for (int slotOffset = 0; slotOffset < committees.length; slotOffset++) {
            for (int committeeIdx = 0; committeeIdx < committees[slotOffset].length; committeeIdx++) {
                Committee committee = committees[slotOffset][committeeIdx];
                int[] validators = committee.getValidators();
                for (int idx = 0; idx < validators.length; idx++) {
                    if (validatorIdx == validators[idx]) {
                        return new Committee.Index(validatorIdx, committee.getShardId(), slotOffset,
                                committeeIdx, validators.length, idx);
                    }
                }
            }
        }

        return Committee.Index.EMPTY;
    }

    /**
     * A shortcut to {@link #scanCommittees(int, Committee[][])} that accepts a set of validator indices.
     */
    public static Set<Committee.Index> scanCommittees(Collection<Integer> validatorIndices, Committee[][] committees) {
        Set<Committee.Index> ret = new HashSet<>();
        for (int slotOffset = 0; slotOffset < committees.length; slotOffset++) {
            for (int committeeIdx = 0; committeeIdx < committees[slotOffset].length; committeeIdx++) {
                Committee committee = committees[slotOffset][committeeIdx];
                int[] validators = committee.getValidators();
                for (int idx = 0; idx < validators.length; idx++) {
                    if (validatorIndices.contains(validators[idx])) {
                        ret.add(new Committee.Index(validators[idx], committee.getShardId(), slotOffset,
                                committeeIdx, validators.length, idx));
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Calculates the next slot that proposer is assigned to
     *
     * @param currentSlot slot that current moment in time does land on
     * @param slotOffset offset of slot number, starting from the beginning of cycle
     *
     * @return number of slot in the next or current cycle
     */
    public static long calcNextProposingSlot(long currentSlot, int slotOffset) {
        long slotNumberInCurrentCycle = cycleStartSlot(currentSlot) + slotOffset;
        if (currentSlot >= slotNumberInCurrentCycle) {
            return slotNumberInCurrentCycle + BeaconConstants.CYCLE_LENGTH;
        } else {
            return slotNumberInCurrentCycle;
        }
    }

    /**
     * Calculates a moment in time that specified slot does start from.
     *
     * @param slotNumber slot number
     * @return timestamp in milliseconds
     */
    public static long getSlotStartTime(long slotNumber) {
        return BeaconGenesis.instance().getTimestamp() + slotNumber * SLOT_DURATION;
    }

    /**
     * Calculates a number of slot that given moment of time does fit into.
     * Uses {@link BeaconGenesis#timestamp} and {@link BeaconConstants#SLOT_DURATION}
     *
     * @param timestamp timestamp in milliseconds
     * @return slot number
     */
    public static long getSlotNumber(long timestamp) {
        return (timestamp - BeaconGenesis.instance().getTimestamp()) / SLOT_DURATION;
    }

    /**
     * Calculates a number of the slot that current moment in time does fit into.
     */
    public static long getCurrentSlotNumber() {
        return getSlotNumber(System.currentTimeMillis());
    }
}
