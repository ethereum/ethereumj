package org.ethereum.sharding;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.DbSource;

import java.security.SecureRandom;

/**
 * @author Mikhail Kalinin
 * @since 17.07.2018
 */
public class Randao {

    private static final byte[] NEXT_KEY = HashUtil.sha3("the_next_randao_image_key".getBytes());
    DbSource<byte[]> src;
    byte[] nextKey;

    public Randao(DbSource<byte[]> src) {
        this.src = src;
        this.nextKey = src.get(NEXT_KEY);
    }

    public void generate(int rounds) {
        // reset datasource
        src.reset();

        byte[] img = new byte[32];
        new SecureRandom().nextBytes(img);

        for (int i = 0; i < rounds; i++) {
            byte[] next = HashUtil.sha3(img);
            src.put(next, img);
            img = next;
        }

        updateNextKey(img);
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
