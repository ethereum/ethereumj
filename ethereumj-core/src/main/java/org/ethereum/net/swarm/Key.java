package org.ethereum.net.swarm;

import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * Data key, just a wrapper for byte array. Normally the SHA(data)
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class Key {
    public static Key zeroKey() {
        return new Key(new byte[0]);
    }

    private final byte[] bytes;

    public Key(byte[] bytes) {
        this.bytes = bytes;
    }

    public Key(String hexKey) {
        this(Hex.decode(hexKey));
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean isZero() {
        if (bytes == null  || bytes.length == 0) return true;
        for (byte b: bytes) {
            if (b != 0) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        return Arrays.equals(bytes, key.bytes);

    }

    public String getHexString() {
        return Hex.toHexString(getBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return getBytes() == null ? "<null>" : ByteUtil.toHexString(getBytes());
    }
}
