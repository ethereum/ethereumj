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

import org.ethereum.util.FastByteComparisons;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.sharding.crypto.Sign.KeyPair;
import static org.ethereum.crypto.HashUtil.blake2b384;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link Sign}
 */
public class SignTest {

    private Sign sign;

    @Before
    public void setup() {
        this.sign = new DummySign();
//        this.sign = new BLS381Sign();
    }

    @Test
    public void testBasics() {
        byte[] msg = blake2b384(Hex.decode("0737626387abcdef"));
        KeyPair keyPair = sign.newKeyPair();
        BigInteger privKey = keyPair.sigKey;
        BigInteger pubKey = keyPair.verKey;
        byte[] signature = sign.sign(msg, privKey);
        assertTrue(sign.verify(signature, msg, pubKey));
        assertFalse(sign.verify(signature, msg, pubKey.add(BigInteger.ONE)));
        byte[] brokenSignature = new byte[signature.length];
        System.arraycopy(signature, 0, brokenSignature, 0, signature.length);
        brokenSignature[signature.length - 1] = 0x1c;
        assertFalse(FastByteComparisons.equal(signature, brokenSignature));
        assertFalse(sign.verify(brokenSignature, msg, pubKey));
        msg[0] = 13;
        assertFalse(sign.verify(signature, msg, pubKey));
    }

    @Test
    @Ignore("Fails in dummy, passes in real implementation")
    public void testKeyNotDummy() {
        byte[] msg = blake2b384(Hex.decode("0737626387abcdef"));
        KeyPair keyPair = sign.newKeyPair();
        BigInteger privKey = keyPair.sigKey;
        byte[] signature = sign.sign(msg, privKey);
        assertFalse(sign.verify(signature, msg, privKey)); // pubKey should be here
    }

    @Test
    public void testAggregation() {
        byte[] msg = blake2b384(Hex.decode("0737626387abcdef"));

        KeyPair keyPair = sign.newKeyPair();
        BigInteger privKey = keyPair.sigKey;
        BigInteger pubKey = keyPair.verKey;
        byte[] signature = sign.sign(msg, privKey);

        KeyPair keyPair2 = sign.newKeyPair();
        BigInteger privKey2 = keyPair2.sigKey;
        BigInteger pubKey2 = keyPair2.verKey;
        byte[] signature2 = sign.sign(msg, privKey2);

        List<byte[]> aggSigns = new ArrayList<>();
        aggSigns.add(signature);
        aggSigns.add(signature2);
        byte[] aggSign = sign.aggSigns(aggSigns);

        List<BigInteger> pubKeys = new ArrayList<>();
        pubKeys.add(pubKey);
        pubKeys.add(pubKey2);
        BigInteger aggPubs = sign.aggPubs(pubKeys);

        assertTrue(sign.verify(aggSign, msg, aggPubs));
    }

    @Test
    @Ignore("Fails in dummy, passes in real implementation")
    public void testAggregationIsReal() {
        byte[] msg = blake2b384(Hex.decode("0737626387abcdef"));

        KeyPair keyPair = sign.newKeyPair();
        BigInteger privKey = keyPair.sigKey;
        BigInteger pubKey = keyPair.verKey;
        byte[] signature = sign.sign(msg, privKey);

        KeyPair keyPair2 = sign.newKeyPair();
        BigInteger privKey2 = keyPair2.sigKey;
        BigInteger pubKey2 = keyPair2.verKey;
        byte[] signature2 = sign.sign(msg, privKey2);

        KeyPair keyPair3 = sign.newKeyPair();
        BigInteger privKey3 = keyPair3.sigKey;
        BigInteger pubKey3 = keyPair3.verKey;
        byte[] signature3 = sign.sign(msg, privKey3);

        byte[] aggSign12 = sign.aggSigns(new ArrayList<byte[]>(){{add(signature); add(signature2);}});
        byte[] aggSign13 = sign.aggSigns(new ArrayList<byte[]>(){{add(signature); add(signature3);}});
        byte[] aggSign23 = sign.aggSigns(new ArrayList<byte[]>(){{add(signature2); add(signature3);}});

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
