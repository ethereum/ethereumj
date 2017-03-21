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

import org.ethereum.net.swarm.Key;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used for several purposes
 * - the main is to ask for a {@link org.ethereum.net.swarm.Chunk} with the specified hash
 * - ask to send back {#PEERS} message with the known nodes nearest to the specified hash
 * - initial request after handshake with zero hash. On this request the nearest known
 *   neighbours are sent back with the {#PEERS} message.
 */
public class BzzRetrieveReqMessage extends BzzMessage {

    private Key key;

    // optional
    private long maxSize = -1;
    private long maxPeers = -1;
    private long timeout = -1;

    public BzzRetrieveReqMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzRetrieveReqMessage(Key key) {
        this.key = key;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        key = new Key(paramsList.get(0).getRLPData());

        if (paramsList.size() > 1) {
            id = ByteUtil.byteArrayToLong(paramsList.get(1).getRLPData());
        }
        if (paramsList.size() > 2) {
            maxSize = ByteUtil.byteArrayToLong(paramsList.get(2).getRLPData());
        }
        if (paramsList.size() > 3) {
            maxPeers = ByteUtil.byteArrayToLong(paramsList.get(3).getRLPData());
        }
        if (paramsList.size() > 4) {
            timeout = ByteUtil.byteArrayToLong(paramsList.get(3).getRLPData());
        }

        parsed = true;
    }

    private void encode() {
        List<byte[]> elems = new ArrayList<>();
        elems.add(RLP.encodeElement(key.getBytes()));
        elems.add(RLP.encodeInt((int) id));
        elems.add(RLP.encodeInt((int) maxSize));
        elems.add(RLP.encodeInt((int) maxPeers));
        elems.add(RLP.encodeInt((int) timeout));
        this.encoded = RLP.encodeList(elems.toArray(new byte[0][]));

    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public Key getKey() {
        return key;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getMaxPeers() {
        return maxPeers;
    }

    public long getTimeout() {
        return timeout;
    }


    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.RETRIEVE_REQUEST;
    }

    @Override
    public String toString() {
        return "BzzRetrieveReqMessage{" +
                "key=" + key +
                ", id=" + id +
                ", maxSize=" + maxSize +
                ", maxPeers=" + maxPeers +
                '}';
    }
}
