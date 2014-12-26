package org.ethereum.core;

import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * Created on: 20/11/2014 11:10
 *
 * http://www.herongyang.com/Java/Bit-String-Set-Bit-to-Byte-Array.html
 */

public class Bloom {

    byte[] data = new byte[64];

    public Bloom() {
    }

    public Bloom(byte[] data) {
        this.data = data;
    }

    public static Bloom create(byte[] toBloom) {
        int mov1 = ((255 & toBloom[0 + 1]) + 256 * ((255 & toBloom[0]) & 1));
        int mov2 = ((255 & toBloom[2 + 1]) + 256 * ((255 & toBloom[2]) & 1));
        int mov3 = ((255 & toBloom[4 + 1]) + 256 * ((255 & toBloom[4]) & 1));

        byte[] data = new byte[64];
        Bloom bloom = new Bloom(data);

        ByteUtil.setBit(data, mov1, 1);
        ByteUtil.setBit(data, mov2, 1);
        ByteUtil.setBit(data, mov3, 1);

        return bloom;
    }

    public void or(Bloom bloom) {
        for (int i = 0; i < data.length; ++i) {
            data[i] |= bloom.data[i];
        }
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return Hex.toHexString(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bloom bloom = (Bloom) o;

        if (!Arrays.equals(data, bloom.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? Arrays.hashCode(data) : 0;
    }
}
