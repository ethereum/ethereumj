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

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Serializer;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.util.ByteUtil;
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

    private final byte[] validatorSetHash;
    private final byte[] dynastySeed;

    public BeaconState(byte[] validatorSetHash, byte[] dynastySeed) {
        this.validatorSetHash = validatorSetHash;
        this.dynastySeed = dynastySeed;
    }

    public BeaconState(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);
        this.validatorSetHash = list.get(0).getRLPData();
        this.dynastySeed = list.get(1).getRLPData();
    }

    public byte[] getHash() {
        return blake2b(getEncoded());
    }

    public byte[] getEncoded() {
        return RLP.wrapList(validatorSetHash, dynastySeed);
    }
    
    public static BeaconState empty() {
        return new BeaconState(HashUtil.EMPTY_TRIE_HASH, new byte[32]);
    }

    public static final Serializer<BeaconState, byte[]> Serializer = new Serializer<BeaconState, byte[]>() {
        @Override
        public byte[] serialize(BeaconState state) {
            return state == null ? null : state.getEncoded();
        }

        @Override
        public BeaconState deserialize(byte[] stream) {
            return stream == null ? null : new BeaconState(stream);
        }
    };
}
