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

/**
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public class BeaconState {

    public BeaconState() {
    }

    public BeaconState(byte[] encoded) {
    }

    public byte[] getHash() {
        return new byte[] {};
    }

    public byte[] getEncoded() {
        return new byte[] {};
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
