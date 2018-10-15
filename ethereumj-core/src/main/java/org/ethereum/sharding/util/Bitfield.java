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

import java.util.BitSet;
import java.util.List;

/**
 * Bitfield utility methods
 *
 * Bitfield is bit array where every bit represents status of
 * attester with corresponding index
 */
public class Bitfield {

    /**
     * Creates empty bitfield for estimated number of attesters
     * @param validatorsCount   Number of attesters
     * @return  empty bitfield with correct length
     */
    public static BitSet createEmpty(int validatorsCount) {
        return new BitSet(validatorsCount);
    }

    /**
     * Modifies bitfield to represent attester's vote
     * Should place its bit on the right place
     * Doesn't modify original bitfield
     * @param bitfield  Original bitfield
     * @param index     Index number of attester
     * @return  bitfield with vote in place
     */
    public static BitSet markVote(final BitSet bitfield, int index) {
        BitSet newBitfield = (BitSet) bitfield.clone();
        newBitfield.set(index);
        return newBitfield;
    }

    /**
     * Checks whether validator with provided index did his vote
     * @param bitfield  Bitfield
     * @param index     Index number of attester
     */
    public static boolean hasVoted(BitSet bitfield, int index) {
        return bitfield.get(index);
    }

    /**
     * Calculate number of votes in provided bitfield
     * @param bitfield  Bitfield
     * @return  number of votes
     */
    public static int calcVotes(BitSet bitfield) {
        int votes = 0;
        for (int i = 0; i < bitfield.size(); ++i) {
            if (hasVoted(bitfield, i)) ++votes;
        }

        return votes;
    }

    /**
     * OR aggregation function
     * OR aggregation of input bitfields
     * @param bitfields  Bitfields
     * @return All bitfields aggregated using OR
     */
    public static BitSet orBitfield(List<BitSet> bitfields) {
        if (bitfields.isEmpty()) return null;

        int bitfieldLen = bitfields.get(0).size();
        BitSet aggBitfield = new BitSet(bitfieldLen);
        for (int i = 0; i < bitfieldLen; ++i) {
            for (BitSet bitfield : bitfields) {
                if (aggBitfield.get(i) | bitfield.get(i)) {
                    aggBitfield.set(i);
                }
            }
        }

        return aggBitfield;
    }
}
