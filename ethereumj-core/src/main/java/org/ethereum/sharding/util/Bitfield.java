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

import java.util.Arrays;

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
    public static byte[] createEmpty(int validatorsCount) {
        return new byte[calcLength(validatorsCount)];
    }

    // TODO: Add test
    /**
     * Calculates attesters bitfield length
     * @param num  Number of attesters
     * @return  Bitfield length in bytes
     */
    public static int calcLength(int num) {
        return (num + 7) / Byte.SIZE;
    }

    /**
     * Modifies bitfield to represent attester's vote
     * Should place its bit on the right place
     * Doesn't modify original bitfield
     * @param bitfield  Original bitfield
     * @param index     Index number of attester
     * @return  bitfield with vote in place
     */
    public static byte[] markVote(final byte[] bitfield, int index) {
        byte[] newBitfield = Arrays.copyOf(bitfield, bitfield.length);
        int byteIndex = index / Byte.SIZE;
        int bitIndex = index % Byte.SIZE;
        newBitfield[byteIndex] |= 128 >> bitIndex;
        return newBitfield;
    }

    /**
     * Checks whether validator with provided index did his vote
     * @param bitfield  Bitfield
     * @param index     Index number of attester
     */
    public static boolean hasVoted(byte[] bitfield, int index) {
        int byteIndex = index / Byte.SIZE;
        int bitIndex = index % Byte.SIZE;

        return (bitfield[byteIndex] & (128 >> bitIndex)) == 1;
    }

    /**
     * Calculate number of votes in provided bitfield
     * @param bitfield  Bitfield
     * @return  number of votes
     */
    public static int calcVotes(byte[] bitfield) {
        int votes = 0;
        for (int i = 0; i < (bitfield.length * Byte.SIZE); ++i) {
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
    public static byte[] orBitfield(byte[][] bitfields) {
        int bitfieldLen = bitfields[0].length;
        byte[] aggBitfield = new byte[bitfieldLen];
        for (int i = 0; i < bitfieldLen; ++i) {
            for (byte[] bitfield : bitfields) {
                aggBitfield[i] |= bitfield[i];
            }
        }

        return aggBitfield;
    }
}
