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
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.db.ValidatorSet;
import org.ethereum.util.FastByteComparisons;
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

    private final CrystallizedState crystallizedState;
    private final ActiveState activeState;

    public BeaconState(CrystallizedState crystallizedState, ActiveState activeState) {
        this.crystallizedState = crystallizedState;
        this.activeState = activeState;
    }

    public CrystallizedState getCrystallizedState() {
        return crystallizedState;
    }

    public ActiveState getActiveState() {
        return activeState;
    }

    public byte[] getHash() {
        return flatten().getHash();
    }

    public Flattened flatten() {
        return new Flattened(crystallizedState, activeState);
    }

    public Committee[][] getCommittees() {
        return crystallizedState.getDynasty().getCommittees();
    }

    public ValidatorSet getValidatorSet() {
        return crystallizedState.getDynasty().getValidatorSet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof BeaconState)) return false;

        return FastByteComparisons.equal(((BeaconState) o).getHash(), this.getHash());
    }

    public static class Flattened {
        private final byte[] crystallizedStateHash;
        private final byte[] activeStateHash;

        public Flattened(CrystallizedState crystallizedState, ActiveState activeState) {
            this.crystallizedStateHash = crystallizedState.getHash();
            this.activeStateHash = activeState.getHash();
        }

        public Flattened(byte[] encoded) {
            RLPList list = RLP.unwrapList(encoded);
            this.crystallizedStateHash = list.get(0).getRLPData();
            this.activeStateHash = list.get(1).getRLPData();
        }

        public byte[] getCrystallizedStateHash() {
            return crystallizedStateHash;
        }

        public byte[] getActiveStateHash() {
            return activeStateHash;
        }

        public byte[] getHash() {
            return blake2b(encode());
        }

        public byte[] encode() {
            return RLP.wrapList(crystallizedStateHash, activeStateHash);
        }

        public static final org.ethereum.datasource.Serializer<Flattened, byte[]> Serializer = new Serializer<Flattened, byte[]>() {
            @Override
            public byte[] serialize(Flattened state) {
                return state == null ? null : state.encode();
            }

            @Override
            public Flattened deserialize(byte[] stream) {
                return stream == null ? null : new Flattened(stream);
            }
        };
    }
}
