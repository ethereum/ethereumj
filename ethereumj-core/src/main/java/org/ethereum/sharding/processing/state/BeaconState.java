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

import org.ethereum.datasource.Serializer;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.ethereum.crypto.HashUtil.blake2b;

/**
 * Beacon state data structure.
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public class BeaconState {

    private final ValidatorSet validatorSet;
    private final byte[] dynastySeed;

    public BeaconState(ValidatorSet validatorSet, byte[] dynastySeed) {
        this.validatorSet = validatorSet;
        this.dynastySeed = dynastySeed;
    }

    public ValidatorSet getValidatorSet() {
        return validatorSet;
    }

    public byte[] getDynastySeed() {
        return dynastySeed;
    }

    public byte[] getHash() {
        return blake2b(getStripped().encode());
    }

    public Stripped getStripped() {
        return new Stripped(validatorSet.getHash(), dynastySeed);
    }

    public static BeaconState empty() {
        return new BeaconState(ValidatorSet.Empty, new byte[32]);
    }

    public BeaconState withValidatorSet(ValidatorSet validatorSet) {
        return new BeaconState(validatorSet, dynastySeed);
    }

    public static class Stripped {
        private final byte[] validatorSetHash;
        private final byte[] dynastySeed;

        public Stripped(byte[] validatorSetHash, byte[] dynastySeed) {
            this.validatorSetHash = validatorSetHash;
            this.dynastySeed = dynastySeed;
        }

        public Stripped(byte[] encoded) {
            RLPList list = RLP.unwrapList(encoded);
            this.validatorSetHash = list.get(0).getRLPData();
            this.dynastySeed = list.get(1).getRLPData();
        }

        public byte[] encode() {
            return RLP.wrapList(validatorSetHash, dynastySeed);
        }

        public byte[] getValidatorSetHash() {
            return validatorSetHash;
        }

        public byte[] getDynastySeed() {
            return dynastySeed;
        }

        public static final org.ethereum.datasource.Serializer<Stripped, byte[]> Serializer = new Serializer<Stripped, byte[]>() {
            @Override
            public byte[] serialize(Stripped state) {
                return state == null ? null : state.encode();
            }

            @Override
            public Stripped deserialize(byte[] stream) {
                return stream == null ? null : new Stripped(stream);
            }
        };
    }
}
