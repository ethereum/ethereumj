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
/*
 * Copyright (c) 2014, Stephan Fuhrmann &lt;stephan@tynne.de&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.ethereum.crypto.cryptohash;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

/**
 * Generic test utility class that gets extended from the digest test
 * classes.
 * @author Stephan Fuhrmann &lt;stephan@tynne.de&gt;
 */
public class AbstractCryptoTest {

    protected void testKatHex(Digest dig, String data, String ref) {
        testFrom(dig, Hex.decode(data), Hex.decode(ref));
    }

    /** Does the comparison using the digest and some calls on it.
     * @param digest the digest to operate on.
     * @param message the input data to pass to the digest.
     * @param expected the expected data out of the digest.
     */
    private static void testFrom(Digest digest, byte[] message, byte[] expected) {
        /*
         * First test the hashing itself.
         */
        byte[] out = digest.digest(message);
        assertArrayEquals(expected, out);

        /*
         * Now the update() API; this also exercises auto-reset.
         */
        for (int i = 0; i < message.length; i++) {
            digest.update(message[i]);
        }
        assertArrayEquals(expected, digest.digest());

        /*
         * The cloning API.
         */
        int blen = message.length;
        digest.update(message, 0, blen / 2);
        Digest dig2 = digest.copy();
        digest.update(message, blen / 2, blen - (blen / 2));
        assertArrayEquals(expected, digest.digest());
        dig2.update(message, blen / 2, blen - (blen / 2));
        assertArrayEquals(expected, dig2.digest());
    }
}