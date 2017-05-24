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
package org.ethereum.net.swarm.bzz;

import org.ethereum.net.client.Capability;
import org.ethereum.net.swarm.Key;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BzzStoreReqMessage extends BzzMessage {

    private Key key;
    private byte[] data;

    // optional
    byte[] metadata = new byte[0];

    public BzzStoreReqMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzStoreReqMessage(long id, Key key, byte[] data) {
        this.id = id;
        this.key = key;
        this.data = data;
    }

    public BzzStoreReqMessage(Key key, byte[] data) {
        this.key = key;
        this.data = data;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        key = new Key(paramsList.get(0).getRLPData());
        data = paramsList.get(1).getRLPData();

        if (paramsList.size() > 2) {
            id = ByteUtil.byteArrayToLong(paramsList.get(2).getRLPData());
        }
        if (paramsList.size() > 3) {
            metadata = paramsList.get(2).getRLPData();
        }

        parsed = true;
    }

    private void encode() {
        List<byte[]> elems = new ArrayList<>();
        elems.add(RLP.encodeElement(key.getBytes()));
        elems.add(RLP.encodeElement(data));
//        if (id >= 0 || metadata != null) {
            elems.add(RLP.encodeInt((int) id));
//        }
//        if (metadata != null) {
            elems.add(RLP.encodeList(metadata));
//        }
        this.encoded = RLP.encodeList(elems.toArray(new byte[0][]));
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.STORE_REQUEST;
    }

    public Key getKey() {
        return key;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "BzzStoreReqMessage{" +
                "key=" + key +
                ", data=" + Arrays.toString(data) +
                ", id=" + id +
                ", metadata=" + Arrays.toString(metadata) +
                '}';
    }
}
