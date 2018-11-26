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

import static org.ethereum.crypto.HashUtil.blake2b384;

/**
 * Test for {@link BLS381}
 */
public class RealSignTest {

    List<String> messages = new ArrayList<String>() {{
            add("Small msg");
            add("121220888888822111212");
            add("Some message to sign");
            add("Some message to sign, making it bigger, ......, still bigger........................, not some entropy, hu2jnnddsssiu8921n ckhddss2222");
            add(" is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
    }};

    @Test
    public void simpleSignTest() {
        BLS381 bls381 = new BLS381();
        BLS381.KeyPair keyPair = bls381.newKeyPair();
        for (String msg : messages) {
            byte[] hash = blake2b384(msg.getBytes());
            byte[] sig = bls381.signMessage(keyPair.sigKey, hash);
            assert bls381.verifyMessage(sig, hash, keyPair.verKey);
        }
    }
}
