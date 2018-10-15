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
package org.ethereum.sharding.crypto;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link Sign}
 */
public class SignTest {

    Sign sign = new DummySign();

    @Test
    public void testDummy() {
        byte[] msg = Hex.decode("0737626387abcdef");
        byte[] privKey = sha3(msg);
        byte[] pubKey = sha3(privKey);
        Sign.Signature signature = sign.sign(msg, privKey);
        assertTrue(sign.verify(signature, pubKey));

        byte[] privKey2 = sha3(Hex.decode("abcdef"));
        byte[] pubKey2 = sha3(privKey);
        // FIXME: Dummy sign is always correct, not true for real implementation
        // assertFalse(sign.verify(signature, pubKey2));

        Sign.Signature signature2 = sign.sign(msg, privKey2);
        Sign.Signature aggSign = sign.aggSigns(new Sign.Signature[] {signature, signature2});
        assertTrue(sign.verify(aggSign, pubKey));
        assertTrue(sign.verify(aggSign, pubKey2));
    }
}
