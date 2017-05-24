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
