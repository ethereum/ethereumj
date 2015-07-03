package org.ethereum.net.swarm.bzz;

import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.StringUtils;
import org.ethereum.net.p2p.Peer;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.swarm.Util;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by Admin on 25.06.2015.
 */
public class PeerAddress {
    byte[] ip;
    int port;
    byte[] id;

    transient Peer node;

    public PeerAddress(PeerInfo peerInfo) {
        ip = peerInfo.getAddress().getAddress();
        port = peerInfo.getPort();
        id = Hex.decode(peerInfo.getPeerId());
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

    public PeerAddress() {
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

    public PeerInfo toPeerInfo() {
        try {
            return new PeerInfo(InetAddress.getByAddress(ip), port, Hex.toHexString(id));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public Peer toPeer() {
        PeerInfo peerInfo = toPeerInfo();
        return new Peer(peerInfo.getAddress(), port, peerInfo.getPeerId());
    }

    public Node toNode() {
        try {
            return new Node(id, InetAddress.getByAddress(ip).getHostAddress(), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static PeerAddress parse(RLPList l) {
        PeerAddress ret = new PeerAddress();
        ret.ip = l.get(0).getRLPData();
        ret.port = ByteUtil.byteArrayToInt(l.get(1).getRLPData());
        ret.id = l.get(2).getRLPData();
        return ret;
    }

    public byte[] encodeRlp() {
        return RLP.encodeList(RLP.encodeElement(ip),
                RLP.encodeElement(Util.uInt16ToBytes(port)),
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
        String sip = "";
        for (int i = 0; i < ip.length; i++) {
            sip += (i == 0 ? "" : ".") + (int)ip[i];
        }
        return "PeerAddress{" +
                "ip=" + sip +
                ", port=" + port +
                ", id=" + Hex.toHexString(id) +
                '}';
    }
}
