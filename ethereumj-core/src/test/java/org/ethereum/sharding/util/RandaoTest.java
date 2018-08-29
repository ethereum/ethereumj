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

import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.inmem.HashMapDBSimple;
import org.junit.Test;

import static org.ethereum.crypto.HashUtil.blake2b;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Mikhail Kalinin
 * @since 17.07.2018
 */
public class RandaoTest {

    @Test
    public void testBasics() {
        Randao rnd = new Randao(new HashMapDBSimple<>());
        int rounds = 1 << 10;

        rnd.generate(rounds);

        byte[] preImg = rnd.reveal();
        assertNotNull(preImg);

        for (int i = 0; i < rounds - 1; i++) {
            byte[] img = rnd.reveal();
            assertArrayEquals(preImg, blake2b(img));
            preImg = img;
        }

        assertNull(rnd.reveal());
    }

    @Test
    public void testPersistedState() {
        DbSource<byte[]> src = new HashMapDBSimple<>();
        int rounds = 1 << 10;

        // generate
        Randao rnd = new Randao(src);
        rnd.generate(rounds);

        // reveal a half
        int i = 0;
        byte[] img = rnd.reveal();
        for (; i < rounds / 2; i++) {
            img = rnd.reveal();
        }

        // re-init and reveal the others
        rnd = new Randao(src);
        byte[] preImg = img;
        for (; i < rounds - 1; i++) {
            img = rnd.reveal();
            assertArrayEquals(preImg, blake2b(img));
            preImg = img;
        }
    }
}
