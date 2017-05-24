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

import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.StringUtils;
import org.ethereum.net.p2p.Peer;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.swarm.Key;
import org.ethereum.net.swarm.Util;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Class similar for {@link Node} used in the swarm
 *
 * Created by Admin on 25.06.2015.
 */
public class PeerAddress {
    private byte[] ip;
    private int port;
    private byte[] id;

    private Key addrKeyCached = null;

    private PeerAddress() {
    }

    public PeerAddress(Node discoverNode) {
        try {
            port = discoverNode.getPort();
            id = discoverNode.getId();
            ip = InetAddress.getByName(discoverNode.getHost()).getAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public PeerAddress(byte[] ip, int port, byte[] id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    public Node toNode() {
        try {
            return new Node(id, InetAddress.getByAddress(ip).getHostAddress(), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the SHA3 hash if this node ID
     */
    public Key getAddrKey() {
        if (addrKeyCached == null) {
            addrKeyCached = new Key(sha3(id));
        }
        return addrKeyCached;
    }

    public byte[] getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public byte[] getId() {
        return id;
    }

    static PeerAddress parse(RLPList l) {
        PeerAddress ret = new PeerAddress();
        ret.ip = l.get(0).getRLPData();
        ret.port = ByteUtil.byteArrayToInt(l.get(1).getRLPData());
        ret.id = l.get(2).getRLPData();
        return ret;
    }

    byte[] encodeRlp() {
        return RLP.encodeList(RLP.encodeElement(ip),
                RLP.encodeInt(port),
                RLP.encodeElement(id));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerAddress that = (PeerAddress) o;

        if (port != that.port) return false;
        return Arrays.equals(ip, that.ip);

    }

    @Override
    public int hashCode() {
        int result = ip != null ? Arrays.hashCode(ip) : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "PeerAddress{" +
                "ip=" + Util.ipBytesToString(ip) +
                ", port=" + port +
                ", id=" + Hex.toHexString(id) +
                '}';
    }
}
