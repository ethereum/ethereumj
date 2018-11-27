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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ethereum.crypto.HashUtil.blake2b384;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link BLS381}
 */
public class RealSignTest {

    BLS381 bls381 = new BLS381();

    List<String> messages = new ArrayList<String>() {{
            add("Small msg");
            add("121220888888822111212");
            add("Some message to sign");
            add("Some message to sign, making it bigger, ......, still bigger........................, not some entropy, hu2jnnddsssiu8921n ckhddss2222");
            add(" is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
    }};

    @Test
    public void simpleSignTest() {
        BLS381.KeyPair keyPair = bls381.newKeyPair();
        for (String msg : messages) {
            byte[] hash = blake2b384(msg.getBytes());
            byte[] sig = bls381.signMessage(keyPair.sigKey, hash);
            assertTrue(bls381.verifyMessage(sig, hash, keyPair.verKey));
        }
    }

    @Test
    public void successFailSignTest() {
        BLS381.KeyPair keyPair = bls381.newKeyPair();

        byte[] hash0 = blake2b384(messages.get(0).getBytes());
        byte[] sig0 = bls381.signMessage(keyPair.sigKey, hash0);

        assertTrue(bls381.verifyMessage(sig0, hash0, keyPair.verKey));

        byte[] hash1 = blake2b384(messages.get(1).getBytes());
        assertFalse(bls381.verifyMessage(sig0, hash1, keyPair.verKey));

        byte[] hash2 = blake2b384(messages.get(2).getBytes());
        assertFalse(bls381.verifyMessage(sig0, hash2, keyPair.verKey));

        byte[] sig1 = bls381.signMessage(keyPair.sigKey, hash1);
        assertFalse(bls381.verifyMessage(sig1, hash0, keyPair.verKey));
    }

    @Test
    public void aggregatedSignatureTest() {
        List<BLS381.KeyPair> keyPairs = new ArrayList<>();
        final int SIGNERS = 5;
        for (int i = 0; i < SIGNERS; ++i) {
            keyPairs.add(bls381.newKeyPair());
        }

        for (String msg : messages) {
            byte[] hash = blake2b384(msg.getBytes());

            List<byte[]> signs = new ArrayList<>();
            for (int i = 0; i < SIGNERS; ++i) {
                signs.add(bls381.signMessage(keyPairs.get(i).sigKey, hash));
            }

            // aggregate signs
            byte[] aggSigs = bls381.combineSignatures(signs);

            // aggregate verKeys
            List<byte[]> verKeys = keyPairs.stream().map(kp -> kp.verKey).collect(Collectors.toList());
            byte[] aggVerKeys = bls381.combineVerificationKeys(verKeys);

            // Verify
            assertTrue(bls381.verifyMessage(aggSigs, hash, aggVerKeys));

            // not all signs
            List<byte[]> slicedSigns = new ArrayList<>(signs.subList(0, SIGNERS - 1));
            byte[] aggSigsSliced = bls381.combineSignatures(slicedSigns);
            assertEquals(SIGNERS - 1, slicedSigns.size());
            assertFalse(bls381.verifyMessage(aggSigsSliced, hash, aggVerKeys));
            // bad sign instead
            slicedSigns.add(bls381.signMessage(bls381.newKeyPair().sigKey, hash));
            assertEquals(SIGNERS, slicedSigns.size());
            assertFalse(bls381.verifyMessage(bls381.combineSignatures(slicedSigns), hash, aggVerKeys));

            // not all verKeys
            List<byte[]> slicedVerKeys = new ArrayList<>(verKeys.subList(0, SIGNERS - 1));
            byte[] aggVerKeysSliced = bls381.combineVerificationKeys(slicedVerKeys);
            assertEquals(SIGNERS - 1, slicedVerKeys.size());
            assertFalse(bls381.verifyMessage(aggSigs, hash, aggVerKeysSliced));
            // bad verKey instead
            slicedVerKeys.add(bls381.newKeyPair().verKey);
            assertEquals(SIGNERS, slicedVerKeys.size());
            assertFalse(bls381.verifyMessage(aggSigs, hash, bls381.combineVerificationKeys(slicedVerKeys)));

            // change the order of signs, 2 at the end reversed
            assertEquals(SIGNERS, signs.size());
            List<byte[]> signsMixed = new ArrayList<>(signs.subList(0, SIGNERS - 2));
            signsMixed.add(signs.get(SIGNERS - 1));
            signsMixed.add(signs.get(SIGNERS - 2));
            assertArrayEquals(signs.get(SIGNERS - 1), signsMixed.get(SIGNERS - 2));
            assertArrayEquals(signs.get(SIGNERS - 2), signsMixed.get(SIGNERS - 1));
            byte[] aggSigsMixed = bls381.combineSignatures(signsMixed);
            assertEquals(SIGNERS, signsMixed.size());
            assertTrue(bls381.verifyMessage(aggSigsMixed, hash, aggVerKeys));

            // change the order of verKeys, 2 at the end reversed
            assertEquals(SIGNERS, verKeys.size());
            List<byte[]> mixedVerKeys = new ArrayList<>(verKeys.subList(0, SIGNERS - 2));
            mixedVerKeys.add(verKeys.get(SIGNERS - 1));
            mixedVerKeys.add(verKeys.get(SIGNERS - 2));
            assertArrayEquals(verKeys.get(SIGNERS - 1), mixedVerKeys.get(SIGNERS - 2));
            assertArrayEquals(verKeys.get(SIGNERS - 2), mixedVerKeys.get(SIGNERS - 1));
            byte[] aggVerKeysMixed = bls381.combineVerificationKeys(mixedVerKeys);
            assertEquals(SIGNERS, mixedVerKeys.size());
            assertTrue(bls381.verifyMessage(aggSigs, hash, aggVerKeysMixed));
        }
    }
}
