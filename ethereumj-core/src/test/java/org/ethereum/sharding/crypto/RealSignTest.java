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

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ethereum.crypto.HashUtil.blake2b384;
import static org.ethereum.sharding.crypto.Sign.Signature;
import static org.ethereum.sharding.crypto.Sign.KeyPair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link BLS381Sign}
 */
public class RealSignTest {

    private Sign sign;

    private List<String> messages = new ArrayList<String>() {{
            add("Lorem ipsum");
            add("8874187471849717971");
            add("Lorem ipsum dolor");
            add("Lorem ipsum dolor sit amet, eum oratio dictas consequuntur ut. Melius posidonium te vel vide hdh313fdhqbif89389hd2dnqd!@#!@");
            add("Lorem ipsum dolor sit amet, eum oratio dictas consequuntur ut. Melius posidonium te vel vide hdh313fdhqbif89389hd2dnqd!@#!@ Lorem ipsum dolor sit amet, qui audiam regione deterruisset ad, alia fugit signiferumque ad sit. An liber debet utroque est, id vim molestiae prodesset. Cum quas labore ex. Eos homero iuvaret ut. Adipisci erroribus ne duo, cu eos movet facilis sadipscing. Cu suscipiantur interpretaris nam, vix ex dicat zril. Vis dicta doming appareat ex, sit ex cibo perfecto instructior, ubique dissentiet delicatissimi ius eu. Mentitum argumentum ad mea, vim ex prima eirmod. Mei ad omnes maluisset. Ne vis nonumy antiopam tincidunt, laoreet consulatu ius ne, sea in ferri elitr sapientem.");
    }};


    @Before
    public void setup() {
        this.sign = new BLS381Sign();
    }

    @Test
    public void simpleSignTest() {
        KeyPair keyPair = sign.newKeyPair();
        for (String msg : messages) {
            byte[] hash = blake2b384(msg.getBytes());
            Signature sig = sign.sign(hash, keyPair.sigKey);
            assertTrue(sign.verify(sig, hash, keyPair.verKey));
        }
    }

    @Test
    public void successFailSignTest() {
        KeyPair keyPair = sign.newKeyPair();

        byte[] hash0 = blake2b384(messages.get(0).getBytes());
        Signature sig0 = sign.sign(hash0, keyPair.sigKey);

        assertTrue(sign.verify(sig0, hash0, keyPair.verKey));

        byte[] hash1 = blake2b384(messages.get(1).getBytes());
        assertFalse(sign.verify(sig0, hash1, keyPair.verKey));

        byte[] hash2 = blake2b384(messages.get(2).getBytes());
        assertFalse(sign.verify(sig0, hash2, keyPair.verKey));

        Signature sig1 = sign.sign(hash1, keyPair.sigKey);
        assertFalse(sign.verify(sig1, hash0, keyPair.verKey));
    }

    @Test
    public void aggregatedSignatureTest() {
        List<KeyPair> keyPairs = new ArrayList<>();
        final int SIGNERS = 5;
        for (int i = 0; i < SIGNERS; ++i) {
            keyPairs.add(sign.newKeyPair());
        }

        for (String msg : messages) {
            byte[] hash = blake2b384(msg.getBytes());

            List<Signature> signs = new ArrayList<>();
            for (int i = 0; i < SIGNERS; ++i) {
                signs.add(sign.sign(hash, keyPairs.get(i).sigKey));
            }

            // aggregate signs
            Signature aggSigs = sign.aggSigns(signs);

            // aggregate verKeys
            List<BigInteger> verKeys = keyPairs.stream().map(kp -> kp.verKey).collect(Collectors.toList());
            BigInteger aggVerKeys = sign.aggPubs(verKeys);

            // Verify
            assertTrue(sign.verify(aggSigs, hash, aggVerKeys));

            // not all signs
            List<Signature> slicedSigns = new ArrayList<>(signs.subList(0, SIGNERS - 1));
            Signature aggSigsSliced = sign.aggSigns(slicedSigns);
            assertEquals(SIGNERS - 1, slicedSigns.size());
            assertFalse(sign.verify(aggSigsSliced, hash, aggVerKeys));
            // bad sign instead
            slicedSigns.add(sign.sign(hash, sign.newKeyPair().sigKey));
            assertEquals(SIGNERS, slicedSigns.size());
            assertFalse(sign.verify(sign.aggSigns(slicedSigns), hash, aggVerKeys));

            // not all verKeys
            List<BigInteger> slicedVerKeys = new ArrayList<>(verKeys.subList(0, SIGNERS - 1));
            BigInteger aggVerKeysSliced = sign.aggPubs(slicedVerKeys);
            assertEquals(SIGNERS - 1, slicedVerKeys.size());
            assertFalse(sign.verify(aggSigs, hash, aggVerKeysSliced));
            // bad verKey instead
            slicedVerKeys.add(sign.newKeyPair().verKey);
            assertEquals(SIGNERS, slicedVerKeys.size());
            assertFalse(sign.verify(aggSigs, hash, sign.aggPubs(slicedVerKeys)));

            // change the order of signs, 2 at the end reversed
            assertEquals(SIGNERS, signs.size());
            List<Signature> signsMixed = new ArrayList<>(signs.subList(0, SIGNERS - 2));
            signsMixed.add(signs.get(SIGNERS - 1));
            signsMixed.add(signs.get(SIGNERS - 2));
            assertEquals(signs.get(SIGNERS - 1), signsMixed.get(SIGNERS - 2));
            assertEquals(signs.get(SIGNERS - 2), signsMixed.get(SIGNERS - 1));
            Signature aggSigsMixed = sign.aggSigns(signsMixed);
            assertEquals(SIGNERS, signsMixed.size());
            assertTrue(sign.verify(aggSigsMixed, hash, aggVerKeys));

            // change the order of verKeys, 2 at the end reversed
            assertEquals(SIGNERS, verKeys.size());
            List<BigInteger> mixedVerKeys = new ArrayList<>(verKeys.subList(0, SIGNERS - 2));
            mixedVerKeys.add(verKeys.get(SIGNERS - 1));
            mixedVerKeys.add(verKeys.get(SIGNERS - 2));
            assertEquals(verKeys.get(SIGNERS - 1), mixedVerKeys.get(SIGNERS - 2));
            assertEquals(verKeys.get(SIGNERS - 2), mixedVerKeys.get(SIGNERS - 1));
            BigInteger aggVerKeysMixed = sign.aggPubs(mixedVerKeys);
            assertEquals(SIGNERS, mixedVerKeys.size());
            assertTrue(sign.verify(aggSigs, hash, aggVerKeysMixed));
        }
    }
}
