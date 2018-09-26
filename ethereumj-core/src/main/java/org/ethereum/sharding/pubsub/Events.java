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
package org.ethereum.sharding.pubsub;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.service.ValidatorService;

/**
 * Provides shortcuts to {@link Event} constructors.
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class Events {

    public static BeaconBlockImported onBeaconBlock(Beacon block, BeaconState state, boolean best) {
        return new BeaconBlockImported(block, state, best);
    }

    public static BeaconChainLoaded onBeaconChainLoaded(Beacon head) {
        return new BeaconChainLoaded(head);
    }

    public static BeaconChainSynced onBeaconChainSynced(Beacon head, BeaconState state) {
        return new BeaconChainSynced(head, state);
    }

    public static ValidatorStateUpdated onValidatorStateUpdated(ValidatorService.State newState) {
        return new ValidatorStateUpdated(newState);
    }
}
