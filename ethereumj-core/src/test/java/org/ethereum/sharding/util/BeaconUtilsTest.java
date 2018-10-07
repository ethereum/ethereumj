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
package org.ethereum.sharding.util;

import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.processing.consensus.BeaconConstants;
import org.ethereum.sharding.processing.state.Committee;
import org.junit.Test;

import static org.ethereum.sharding.validator.BeaconProposer.SLOT_DURATION;
import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 25.09.2018
 */
public class BeaconUtilsTest {

    @Test
    public void testScanCommittees() {
        int slotCommittees = 16;

        Committee[][] committees = new Committee[BeaconConstants.CYCLE_LENGTH][];
        for (int slot = 0; slot < committees.length; slot++) {
            Committee[] inSlot = new Committee[slotCommittees];
            for (int i = 0; i < inSlot.length; i++) {
                inSlot[i] = new Committee((short) 0, new int[]{});
            }
            committees[slot] = inSlot;
        }

        short s1 = 10, s2 = 13;
        int sl1 = BeaconConstants.CYCLE_LENGTH / 3, sl2 = BeaconConstants.CYCLE_LENGTH / 5;
        int ci1 = slotCommittees / 3, ci2 = slotCommittees / 5;

        Committee c1 = new Committee(s1, new int[] {1, 2, 5, 4, 3, 6});
        Committee c2 = new Committee(s2, new int[] {7, 11, 9});

        committees[sl1][ci1] = c1;
        committees[sl2][ci2] = c2;

        Committee.Index idx = BeaconUtils.scanCommittees(1, committees);
        assertEquals(new Committee.Index(1, s1, sl1, ci1, 6, 0), idx);

        idx = BeaconUtils.scanCommittees(4, committees);
        assertEquals(new Committee.Index(4, s1, sl1, ci1, 6, 3), idx);

        idx = BeaconUtils.scanCommittees(11, committees);
        assertEquals(new Committee.Index(11, s2, sl2, ci2, 3, 1), idx);
    }

    @Test
    public void testSlotCalculations() {

        long genesisTimestamp = BeaconGenesis.instance().getTimestamp();

        assertEquals(0L, BeaconUtils.getSlotNumber(genesisTimestamp));
        assertEquals(0L, BeaconUtils.getSlotNumber(genesisTimestamp + SLOT_DURATION / 2));
        assertEquals(1L, BeaconUtils.getSlotNumber(genesisTimestamp + SLOT_DURATION));
        assertEquals(1L, BeaconUtils.getSlotNumber(genesisTimestamp + SLOT_DURATION + SLOT_DURATION / 2));
        assertEquals(49L, BeaconUtils.getSlotNumber(genesisTimestamp + SLOT_DURATION * 49));
        assertEquals(49L, BeaconUtils.getSlotNumber(genesisTimestamp + SLOT_DURATION * 49 + SLOT_DURATION / 100));

        assertEquals(genesisTimestamp, BeaconUtils.getSlotStartTime(0L));
        assertEquals(genesisTimestamp + SLOT_DURATION, BeaconUtils.getSlotStartTime(1L));
        assertEquals(genesisTimestamp + SLOT_DURATION * 49, BeaconUtils.getSlotStartTime(49L));
    }
}
