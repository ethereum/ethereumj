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
package org.ethereum.sharding.processing.state;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.util.ByteUtil.byteArrayToInt;
import static org.ethereum.util.ByteUtil.intToBytesNoLeadZeroes;

/**
 * @author Mikhail Kalinin
 * @since 06.09.2018
 */
public class Committee {

    private final int shardId;
    private final int[] validators;

    public Committee(short shardId, int[] validators) {
        this.shardId = shardId;
        this.validators = validators;
    }

    public Committee(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);
        this.shardId = byteArrayToInt(list.get(0).getRLPData());

        RLPList validatorList = RLP.unwrapList(list.get(1).getRLPData());
        this.validators = new int[validatorList.size()];
        for (int i = 0; i < validatorList.size(); i++)
            this.validators[i] = byteArrayToInt(validatorList.get(i).getRLPData());
    }

    public int getShardId() {
        return shardId;
    }

    public int[] getValidators() {
        return validators;
    }

    public byte[] getEncoded() {
        byte[][] encodedValidators = new byte[validators.length][];
        for (int i = 0; i < validators.length; i++)
            encodedValidators[i] = intToBytesNoLeadZeroes(validators[i]);

        return RLP.wrapList(intToBytesNoLeadZeroes(shardId), RLP.wrapList(encodedValidators));
    }

    public static class Index {
        /* index of validator in active validator set */
        private final int validatorIdx;
        /* shard Id */
        private final int shardId;
        /* an offset from cycle start slot */
        private final int slotOffset;
        /* index in slot committees array */
        private final int committeeIdx;
        /* length of validators array */
        private final int committeeSize;
        /* index in validators array */
        private final int arrayIdx;

        public static final Index EMPTY = new Index(-1, -1, -1, -1, -1, -1);

        public Index(int validatorIdx, int shardId, int slotOffset, int committeeIdx, int committeeSize, int arrayIdx) {
            this.validatorIdx = validatorIdx;
            this.shardId = shardId;
            this.slotOffset = slotOffset;
            this.committeeIdx = committeeIdx;
            this.committeeSize = committeeSize;
            this.arrayIdx = arrayIdx;
        }

        public boolean isEmpty() {
            return shardId < 0 || slotOffset < 0 || committeeIdx < 0;
        }

        public int getValidatorIdx() {
            return validatorIdx;
        }

        public int getShardId() {
            return shardId;
        }

        public int getSlotOffset() {
            return slotOffset;
        }

        public int getCommitteeIdx() {
            return committeeIdx;
        }

        public int getCommitteeSize() {
            return committeeSize;
        }

        public int getArrayIdx() {
            return arrayIdx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Index index = (Index) o;

            if (validatorIdx != index.validatorIdx) return false;
            if (shardId != index.shardId) return false;
            if (slotOffset != index.slotOffset) return false;
            if (committeeIdx != index.committeeIdx) return false;
            if (committeeSize != index.committeeSize) return false;
            return arrayIdx == index.arrayIdx;

        }

        @Override
        public String toString() {
            return "Index{" +
                    "validatorIdx=" + validatorIdx +
                    ", shardId=" + shardId +
                    ", slotOffset=" + slotOffset +
                    ", committeeIdx=" + committeeIdx +
                    ", committeeSize=" + committeeSize +
                    ", arrayIdx=" + arrayIdx +
                    '}';
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(shardId).append(": [");

        for (int v : validators)
            builder.append(v).append(",");

        if (validators.length > 0)
            builder.delete(builder.length() - 1, builder.length());

        builder.append("]");
        return builder.toString();
    }
}
