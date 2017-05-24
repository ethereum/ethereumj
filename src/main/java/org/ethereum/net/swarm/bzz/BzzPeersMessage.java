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
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

/**
 * The message is the immediate response on the {#RETRIEVE_REQUEST} with the nearest known nodes
 * of the requested hash.
 * Contains a list of nearest Nodes (addresses) to the requested hash.
 */
public class BzzPeersMessage extends BzzMessage {

    private List<PeerAddress> peers;
    long timeout;
    // optional
    private Key key;

    public BzzPeersMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzPeersMessage(List<PeerAddress> peers, long timeout, Key key, long id) {
        this.peers = peers;
        this.timeout = timeout;
        this.key = key;
        this.id = id;
    }

    public BzzPeersMessage(List<PeerAddress> peers) {
        this.peers = peers;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        peers = new ArrayList<>();
        RLPList addrs = (RLPList) paramsList.get(0);
        for (RLPElement a : addrs) {
            peers.add(PeerAddress.parse((RLPList) a));
        }
        timeout = ByteUtil.byteArrayToLong(paramsList.get(1).getRLPData());;
        if (paramsList.size() > 2) {
            key = new Key(paramsList.get(2).getRLPData());
        }
        if (paramsList.size() > 3) {
            id = ByteUtil.byteArrayToLong(paramsList.get(3).getRLPData());;
        }

        parsed = true;
    }

    private void encode() {
        byte[][] bPeers = new byte[this.peers.size()][];
        for (int i = 0; i < this.peers.size(); i++) {
            PeerAddress peer = this.peers.get(i);
            bPeers[i] = peer.encodeRlp();
        }
        byte[] bPeersList = RLP.encodeList(bPeers);
        byte[] bTimeout = RLP.encodeInt((int) timeout);

        if (key == null) {
            this.encoded = RLP.encodeList(bPeersList, bTimeout);
        } else {
            this.encoded = RLP.encodeList(bPeersList,
                    bTimeout,
                    RLP.encodeElement(key.getBytes()),
                    RLP.encodeInt((int) id));
        }
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<PeerAddress> getPeers() {
        return peers;
    }

    public Key getKey() {
        return key;
    }

    public long getId() {
        return id;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.PEERS;
    }

    @Override
    public String toString() {
        return "BzzPeersMessage{" +
                "peers=" + peers +
                ", key=" + key +
                ", id=" + id +
                '}';
    }
}
