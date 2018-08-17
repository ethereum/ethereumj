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

import org.ethereum.datasource.ObjectDataSource;
import org.ethereum.datasource.Source;
import org.ethereum.sharding.processing.db.BeaconStore;

/**
 * @author Mikhail Kalinin
 * @since 16.08.2018
 */
public class BeaconStateRepository implements StateRepository {

    Source<byte[], byte[]> src;
    ObjectDataSource<BeaconState> stateDS;

    public BeaconStateRepository(Source<byte[], byte[]> src) {
        this.src = src;
        this.stateDS = new ObjectDataSource<>(src, BeaconState.Serializer, BeaconStore.BLOCKS_IN_MEM);
    }

    @Override
    public void insert(BeaconState state) {
        stateDS.put(state.getHash(), state);
    }

    @Override
    public BeaconState get(byte[] hash) {
        return stateDS.get(hash);
    }

    @Override
    public void commit() {
    }
}
