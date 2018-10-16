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

import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link Sign}
 */
public class SignTest {

    Sign sign = new DummySign();

    @Test
    public void testBasics() {
        byte[] msg = Hex.decode("0737626387abcdef");
        BigInteger privKey = new BigInteger(sha3(msg));
        BigInteger pubKey = sign.privToPub(privKey);
        Sign.Signature signature = sign.sign(msg, privKey);
        assertTrue(sign.verify(signature, msg, pubKey));
        assertFalse(sign.verify(signature, msg, pubKey.add(BigInteger.ONE)));
        Sign.Signature brokenSignature = new Sign.Signature();
        brokenSignature.r = signature.r.subtract(BigInteger.ONE);
        brokenSignature.s = signature.s;
        assertFalse(sign.verify(brokenSignature, msg, pubKey));
        msg[0] = 13;
        assertFalse(sign.verify(signature, msg, pubKey));
    }

    @Test
    @Ignore("Until real implementation")
    public void testKeyNotDummy() {
        byte[] msg = Hex.decode("0737626387abcdef");
        BigInteger privKey = new BigInteger(sha3(msg));
        Sign.Signature signature = sign.sign(msg, privKey);
        assertFalse(sign.verify(signature, msg, privKey)); // pubKey should be here
    }

    @Test
    public void testAggregation() {
        byte[] msg = Hex.decode("0737626387abcdef");

        BigInteger privKey = new BigInteger(sha3(msg));
        BigInteger pubKey = sign.privToPub(privKey);
        Sign.Signature signature = sign.sign(msg, privKey);

        BigInteger privKey2 = new BigInteger(sha3(Hex.decode("abcdef")));
        BigInteger pubKey2 = sign.privToPub(privKey2);
        Sign.Signature signature2 = sign.sign(msg, privKey2);

        List<Sign.Signature> aggSigns = new ArrayList<>();
        aggSigns.add(signature);
        aggSigns.add(signature2);
        Sign.Signature aggSign = sign.aggSigns(aggSigns);

        List<BigInteger> pubKeys = new ArrayList<>();
        pubKeys.add(pubKey);
        pubKeys.add(pubKey2);
        BigInteger aggPubs = sign.aggPubs(pubKeys);

        assertTrue(sign.verify(aggSign, msg, aggPubs));
    }

    @Test
    @Ignore("Until real implementation")
    public void testAggregationIsReal() {
        byte[] msg = Hex.decode("0737626387abcdef");

        BigInteger privKey = new BigInteger(sha3(msg));
        BigInteger pubKey = sign.privToPub(privKey);
        Sign.Signature signature = sign.sign(msg, privKey);

        BigInteger privKey2 = new BigInteger(sha3(Hex.decode("abcdef")));
        BigInteger pubKey2 = sign.privToPub(privKey);
        Sign.Signature signature2 = sign.sign(msg, privKey2);

        BigInteger privKey3 = new BigInteger(sha3(Hex.decode("1dfb0a")));
        BigInteger pubKey3 = sign.privToPub(privKey3);
        Sign.Signature signature3 = sign.sign(msg, privKey3);

        Sign.Signature aggSign12 = sign.aggSigns(new ArrayList<Sign.Signature>(){{add(signature); add(signature2);}});
        Sign.Signature aggSign13 = sign.aggSigns(new ArrayList<Sign.Signature>(){{add(signature); add(signature3);}});
        Sign.Signature aggSign23 = sign.aggSigns(new ArrayList<Sign.Signature>(){{add(signature2); add(signature3);}});

        BigInteger aggPubs12 = sign.aggPubs(new ArrayList<BigInteger>(){{add(pubKey); add(pubKey2);}});
        BigInteger aggPubs13 = sign.aggPubs(new ArrayList<BigInteger>(){{add(pubKey); add(pubKey3);}});
        BigInteger aggPubs23 = sign.aggPubs(new ArrayList<BigInteger>(){{add(pubKey2); add(pubKey3);}});

        assertTrue(sign.verify(aggSign12, msg, aggPubs12));
        assertFalse(sign.verify(aggSign12, msg, aggPubs23));
        assertFalse(sign.verify(aggSign12, msg, aggPubs13));

        assertTrue(sign.verify(aggSign13, msg, aggPubs13));
        assertFalse(sign.verify(aggSign13, msg, aggPubs23));
        assertFalse(sign.verify(aggSign13, msg, aggPubs12));

        assertTrue(sign.verify(aggSign23, msg, aggPubs23));
        assertFalse(sign.verify(aggSign23, msg, aggPubs12));
        assertFalse(sign.verify(aggSign23, msg, aggPubs13));
    }
}
