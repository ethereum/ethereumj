package org.ethereum.sharding;

import org.ethereum.datasource.DbSource;

import java.security.SecureRandom;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Generates and maintains data samples created in a hash onion fashion.
 *
 * <p>
 *     That is a list of images made with a series of functions: <code>H_n(... H_2(H_1(s)))</code>.
 *
 * <p>
 *     Use {@link #generate(int)} to create a list of images and then {@link #reveal()} to pick last image.
 *
 * @author Mikhail Kalinin
 * @since 17.07.2018
 */
public class Randao {

    private static final byte[] NEXT_KEY = sha3("the_next_randao_image_key".getBytes());
    private static final int IMAGE_SIZE = 32;

    DbSource<byte[]> src;
    byte[] nextKey;

    public Randao(DbSource<byte[]> src) {
        this.src = src;
        this.nextKey = src.get(NEXT_KEY);
    }

    public void generate(int rounds) {
        byte[] seed = new byte[IMAGE_SIZE];
        new SecureRandom().nextBytes(seed);
        generate(rounds, seed);
    }

    public void generate(int rounds, byte[] seed) {
        assert seed != null;

        // update seed length to IMAGE_SIZE if needed
        if (seed.length != IMAGE_SIZE) {
            seed = sha3(seed);
        }

        // reset datasource
        src.reset();

        for (int i = 0; i < rounds; i++) {
            byte[] next = sha3(seed);
            src.put(next, seed);
            seed = next;
        }

        updateNextKey(seed);
    }

    public byte[] reveal() {
        byte[] img = src.get(nextKey);
        updateNextKey(img);
        return img;
    }

    private void updateNextKey(byte[] key) {
        this.nextKey = key;
        src.put(NEXT_KEY, key);
    }
}
