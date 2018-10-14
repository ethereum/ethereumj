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
import org.ethereum.sharding.processing.db.TrieValidatorSet;
import org.ethereum.sharding.processing.db.ValidatorSet;

/**
 * Default implementation of {@link StateRepository}.
 *
 * @author Mikhail Kalinin
 * @since 16.08.2018
 */
public class BeaconStateRepository implements StateRepository {

    Source<byte[], byte[]> src;
    ObjectDataSource<BeaconState.Flattened> stateDS;
    Source<byte[], byte[]> crystallizedSrc;
    ObjectDataSource<CrystallizedState.Flattened> crystallizedDS;
    ObjectDataSource<ActiveState> activeDS;
    Source<byte[], byte[]> validatorSrc;
    Source<byte[], byte[]> validatorIndexSrc;

    public BeaconStateRepository(Source<byte[], byte[]> src, Source<byte[], byte[]> crystallizedSrc,
                                 Source<byte[], byte[]> activeSrc,
                                 Source<byte[], byte[]> validatorSrc, Source<byte[], byte[]> validatorIndexSrc) {
        this.src = src;
        this.crystallizedSrc = crystallizedSrc;
        this.validatorSrc = validatorSrc;
        this.validatorIndexSrc = validatorIndexSrc;

        this.stateDS = new ObjectDataSource<>(src, BeaconState.Flattened.Serializer, BeaconStore.BLOCKS_IN_MEM);
        this.crystallizedDS = new ObjectDataSource<>(crystallizedSrc,
                CrystallizedState.Flattened.Serializer, BeaconStore.BLOCKS_IN_MEM);
        this.activeDS = new ObjectDataSource<>(activeSrc,
                ActiveState.Serializer, BeaconStore.BLOCKS_IN_MEM);
    }

    @Override
    public void insert(BeaconState state) {
        CrystallizedState crystallized = state.getCrystallizedState();
        crystallizedDS.put(crystallized.getHash(), crystallized.flatten());
        ActiveState activeState = state.getActiveState();
        activeDS.put(activeState.getHash(), activeState);
        stateDS.put(state.getHash(), state.flatten());
    }

    @Override
    public BeaconState get(byte[] hash) {
        BeaconState.Flattened flattened = stateDS.get(hash);
        if (flattened == null)
            return null;

        return fromFlattened(flattened);
    }

    @Override
    public BeaconState getEmpty() {
        CrystallizedState.Flattened crystallizedFlattened = CrystallizedState.Flattened.empty();
        CrystallizedState crystallizedState = fromFlattened(crystallizedFlattened);
        ActiveState activeState = ActiveState.createEmpty();

        return new BeaconState(crystallizedState, activeState);
    }

    BeaconState fromFlattened(BeaconState.Flattened flattened) {
        CrystallizedState.Flattened crystallizedFlattened = crystallizedDS.get(flattened.getCrystallizedStateHash());
        CrystallizedState crystallizedState = fromFlattened(crystallizedFlattened);

        ActiveState activeState = activeDS.get(flattened.getActiveStateHash());

        return new BeaconState(crystallizedState, activeState);
    }

    CrystallizedState fromFlattened(CrystallizedState.Flattened flattened) {
        ValidatorSet validatorSet = new TrieValidatorSet(validatorSrc, validatorIndexSrc,
                flattened.getValidatorSetHash());
        Dynasty dynasty = new Dynasty(validatorSet, flattened.getCommittees(),
                flattened.getCurrentDynasty(),flattened.getDynastySeed(),
                flattened.getDynastySeedLastReset(), flattened.getDynastyStart());

        Finality finality = new Finality(flattened.getLastJustifiedSlot(),
                flattened.getJustifiedStreak(), flattened.getLastFinalizedSlot());

        return new CrystallizedState(flattened.getLastStateRecalc(),
                dynasty, finality, flattened.getCrosslinks());
    }

    @Override
    public void commit() {
        crystallizedDS.flush();
        stateDS.flush();
    }
}
