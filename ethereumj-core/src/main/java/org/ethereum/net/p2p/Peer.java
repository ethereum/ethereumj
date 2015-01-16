package org.ethereum.net.p2p;

import org.ethereum.net.client.Capability;
import org.ethereum.util.RLP;

import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * This class models a peer in the network
 */
public class Peer {

    private final InetAddress address;
    private final int port;
    private final String peerId;
    private final List<Capability> capabilities;

    public Peer(InetAddress ip, int port, String peerId) {
        this.address = ip;
        this.port = port;
        this.peerId = peerId;
        this.capabilities = new ArrayList<>();
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getPeerId() {
        return peerId == null ? "" : peerId;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public byte[] getEncoded() {
        byte[] ip = RLP.encodeElement(this.address.getAddress());
        byte[] port = RLP.encodeInt(this.port);
        byte[] peerId = RLP.encodeElement(Hex.decode(this.peerId));
        byte[][] encodedCaps = new byte[this.capabilities.size()][];
        for (int i = 0; i < this.capabilities.size() * 2; i++) {
            encodedCaps[i] = RLP.encodeString(this.capabilities.get(i).getName());
            encodedCaps[i] = RLP.encodeByte(this.capabilities.get(i).getVersion());
        }
        byte[] capabilities = RLP.encodeList(encodedCaps);
        return RLP.encodeList(ip, port, peerId, capabilities);
    }

    @Override
    public String toString() {
        return "[ip=" + getAddress().getHostAddress() +
                " port=" + getPort()
                + " peerId=" + getPeerId() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Peer peerData = (Peer) obj;
        return peerData.peerId.equals(this.peerId)
                || this.getAddress().equals(peerData.getAddress());
    }

    @Override
    public int hashCode() {
        return -1; // override for equals function
    }
}
