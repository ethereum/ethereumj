package org.ethereum.net.swarm;

import org.ethereum.util.ByteUtil;

import java.util.Arrays;

/**
 * Created by Admin on 18.06.2015.
 */
public class Key {
    private final byte[] bytes;

    public Key(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        return Arrays.equals(bytes, key.bytes);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return ByteUtil.toHexString(getBytes());
    }
}
