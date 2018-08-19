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
package org.ethereum.sharding.domain;

import org.ethereum.sharding.processing.state.BeaconState;

import java.math.BigInteger;

/**
 * A special beacon chain block that is the first block of the chain.
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public class BeaconGenesis extends Beacon {

    public static final long SLOT = 0;

    private static final byte[] NULL = new byte[32];

    private static BeaconGenesis instance;

    private BeaconGenesis() {
        super(NULL, NULL, NULL, NULL, SLOT);
        setStateHash(getState().getHash());
    }

    public static BeaconGenesis instance() {
        if (instance == null)
            instance = new BeaconGenesis();
        return instance;
    }

    public BeaconState getState() {
        return new BeaconState();
    }

    public BigInteger getScore() {
        return BigInteger.ZERO;
    }
}
