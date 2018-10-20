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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link Bitfield}
 */
public class BitfieldTest {

    @Test
    public void testSize() {
        Bitfield bitfield = Bitfield.createEmpty(13);
        assertEquals(16, bitfield.size());

        Bitfield bitfield2 = Bitfield.createEmpty(113);
        assertEquals(120, bitfield2.size());
    }

    @Test
    public void testBasics() {
        Bitfield bitfield = Bitfield.createEmpty(35);
        assertEquals(0, bitfield.calcVotes());

        bitfield = bitfield.markVote(32);
        assertEquals(1, bitfield.calcVotes());
        for (int i = 0; i < 35; ++i) {
            if (i != 32) {
                assertFalse(bitfield.hasVoted(i));
            } else {
                assertTrue(bitfield.hasVoted(i));
            }
        }

        bitfield = bitfield.markVote(0);
        assertEquals(2, bitfield.calcVotes());
        assertTrue(bitfield.hasVoted(0));
        assertFalse(bitfield.hasVoted(1));
        assertTrue(bitfield.hasVoted(32));
    }

    @Test
    public void testSerialize() {
        Bitfield bitfield = Bitfield.createEmpty(125);
        bitfield = bitfield.markVote(124);
        bitfield = bitfield.markVote(116);
        byte[] serialized = bitfield.getData();

        Bitfield restored = new Bitfield(serialized);
        assertTrue(restored.hasVoted(124));
        assertTrue(restored.hasVoted(116));
        assertEquals(2, restored.calcVotes());
    }
}
