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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ethereum.crypto.HashUtil.blake2b384;
import static org.junit.Assert.assertTrue;
import static org.ethereum.sharding.crypto.Sign.KeyPair;

/**
 * Benchmark for {@link BLS381Sign}
 */
@Ignore
public class SignBenchmarkTest {

    BLS381Sign bls381 = new BLS381Sign();

    private static final int MESSAGE_BYTES = 4096;
    private static final int ROUNDS = 10;
    private static final int SIGNERS = 512;
    SecureRandom random = new SecureRandom();

    @Test
    public void aggregatedSignatureTest() {
        for (int o = 0; o < ROUNDS; ++o) {
            System.out.println();
            System.out.println("Starting round #" + (o + 1));
            byte[] message = new byte[MESSAGE_BYTES];
            random.nextBytes(message);

            List<KeyPair> keyPairs = new ArrayList<>();

            long start = System.nanoTime();
            for (int i = 0; i < SIGNERS; ++i) {
                keyPairs.add(bls381.newKeyPair());
            }
            long finish = System.nanoTime();
            System.out.println(String.format("Generated %s key pairs in %s", SIGNERS, formatNs(finish - start)));

            byte[] hash = blake2b384(message);

            List<byte[]> signs = new ArrayList<>();
            start = System.nanoTime();
            for (int i = 0; i < SIGNERS; ++i) {
                signs.add(bls381.sign(hash, keyPairs.get(i).sigKey));
            }
            finish = System.nanoTime();
            System.out.println(String.format("Signs message by %s signers in %s", SIGNERS, formatNs(finish - start)));

            // aggregate signs
            start = System.nanoTime();
            byte[] aggSigs = bls381.aggSigns(signs);
            finish = System.nanoTime();
            System.out.println(String.format("Aggregated %s signatures in %s", SIGNERS, formatNs(finish - start)));


            // aggregate verKeys
            List<BigInteger> verKeys = keyPairs.stream().map(kp -> kp.verKey).collect(Collectors.toList());
            start = System.nanoTime();
            BigInteger aggVerKeys = bls381.aggPubs(verKeys);
            finish = System.nanoTime();
            System.out.println(String.format("Aggregated %s verKeys in %s", SIGNERS, formatNs(finish - start)));

            // Verify
            start = System.nanoTime();
            assertTrue(bls381.verify(aggSigs, hash, aggVerKeys));
            finish = System.nanoTime();
            System.out.println(String.format("Verified signature in %s", formatNs(finish - start)));
        }
    }


    private String formatNs(long nanoSeconds) {
        return String.format("%.2f ms", nanoSeconds / 1_000_000.0);
    }
}
