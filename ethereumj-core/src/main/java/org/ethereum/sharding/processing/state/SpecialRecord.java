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
package org.ethereum.sharding.processing.state;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

import static org.ethereum.util.ByteUtil.byteArrayToInt;
import static org.ethereum.util.ByteUtil.intToBytesNoLeadZeroes;

/**
 * Special record, sort of container
 */
public class SpecialRecord {

    // Kind of object
    private final int kind;
    // Payload
    private final byte[] data;

    public SpecialRecord(int kind, byte[] data) {
        this.kind = kind;
        this.data = data;
    }

    public SpecialRecord(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);

        this.kind = byteArrayToInt(list.get(0).getRLPData());
        this.data = list.get(1).getRLPData();
    }

    public int getKind() {
        return kind;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getEncoded() {
        return RLP.wrapList(intToBytesNoLeadZeroes(kind),
                data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialRecord that = (SpecialRecord) o;
        return kind == that.kind &&
                Arrays.equals(data, that.data);
    }

    @Override
    public String toString() {
        return "SpecialRecord{" +
                "kind=" + kind +
                ", data=" + Hex.toHexString(data) +
                '}';
    }
}
