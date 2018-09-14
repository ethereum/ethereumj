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

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2018
 */
public class BeaconGenesisTest {

    @Test
    public void testLoadFromJson() {
        BeaconGenesis.Json json = BeaconGenesis.Json.fromResource("genesis/beacon-genesis-parse-test.json");
        BeaconGenesis genesis = new BeaconGenesis(json);

        assertArrayEquals(Hex.decode("d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3"), genesis.getParentHash());
        assertArrayEquals(Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4"), genesis.getRandaoReveal());
        assertArrayEquals(Hex.decode("01791102999c339c844880b23950704cc43aa840f3739e365323cda4dfa89e7a"), genesis.getMainChainRef());
        assertEquals(1535474832000L, genesis.getTimestamp());
    }

    @Test
    public void testValidatorSet() {
        BeaconGenesis.Json json = BeaconGenesis.Json.fromResource("genesis/beacon-genesis-parse-test.json");
        BeaconGenesis genesis = new BeaconGenesis(json);

        assertEquals("0947751e3022ecf3016be03ec77ab0ce3c2662b4843898cb068d74f698ccc8ad", genesis.getInitialValidators().get(0));
        assertEquals("75aa17564ae80a20bb044ee7a6d903e8e8df624b089c95d66a0570f051e5a05b", genesis.getInitialValidators().get(1));
    }
}
