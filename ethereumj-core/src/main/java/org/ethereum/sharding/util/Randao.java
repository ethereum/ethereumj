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

import org.ethereum.datasource.Source;

import java.security.SecureRandom;

import static org.ethereum.crypto.HashUtil.blake2b;

/**
 * Generates and maintains data samples created in a hash onion fashion.
 *
 * <p>
 *     That is a list of images made with a series of functions: <code>H_n(... H_2(H_1(s)))</code>.
 *
 * <p>
 *     Use {@link #generate(int)} to create a list of images and then {@link #revealNext()} to pick next unrevealed image;
 *     {@link #reveal(byte[])} returns image using its pre image as a key.
 *
 *
 * @author Mikhail Kalinin
 * @since 17.07.2018
 */
public class Randao {

    private static final byte[] NEXT_KEY = blake2b("the_next_randao_image_key".getBytes());
    private static final int IMAGE_SIZE = 32;

    Source<byte[], byte[]> src;
    byte[] nextKey;

    public Randao(Source<byte[], byte[]> src) {
        this.src = src;
        this.nextKey = src.get(NEXT_KEY);
    }

    public byte[] generate(int rounds) {
        byte[] seed = new byte[IMAGE_SIZE];
        new SecureRandom().nextBytes(seed);
        return generate(rounds, seed);
    }

    /**
     * Generates a hash onion data.
     *
     * @param rounds number of hashes
     * @param seed a seed to start hashing from
     * @return latest generated pre-image from which hash onion row does start
     */
    public byte[] generate(int rounds, byte[] seed) {
        assert seed != null;

        // update seed length to IMAGE_SIZE if needed
        if (seed.length != IMAGE_SIZE) {
            seed = blake2b(seed);
        }

        for (int i = 0; i < rounds; i++) {
            byte[] next = blake2b(seed);
            src.put(next, seed);
            seed = next;
        }

        updateNextKey(seed);
        flush();

        return src.get(seed);
    }

    public byte[] reveal(byte[] preImage) {
        return src.get(preImage);
    }

    public byte[] revealNext() {
        byte[] img = src.get(nextKey);
        updateNextKey(img);
        flush();
        return img;
    }

    private void updateNextKey(byte[] key) {
        this.nextKey = key;
        src.put(NEXT_KEY, key);
    }

    private void flush() {
        src.flush();
    }
}
