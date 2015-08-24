package org.ethereum.net.peerdiscovery;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * This class models a peer in the network
 */
public class PeerInfo {

    private InetAddress address;
    private int port;
    private String peerId;

    private List<Capability> capabilities;
    private HelloMessage handshakeHelloMessage;
    private StatusMessage statusMessage;

    private transient boolean isOnline = false;
    private transient long lastCheckTime = 0;

    public PeerInfo(InetAddress ip, int port, String peerId) {
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

    public boolean isOnline() {
        if (getCapabilities().size() < 0)
            return false;
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("PeerInfo: [ ip=").append(getAddress().getHostAddress())
                .append(" port=").append(getPort())
                .append(" peerId=").append(getPeerId()).append("] \n")
                .append(this.handshakeHelloMessage == null ? "" : handshakeHelloMessage + "\n")
                .append(this.statusMessage == null ? "" : statusMessage + "\n");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        PeerInfo peerData = (PeerInfo) obj;
        return peerData.hashCode() == this.hashCode();
    }


    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + port;
        return result;
    }

    public HelloMessage getHandshakeHelloMessage() {
        return handshakeHelloMessage;
    }

    public void setHandshakeHelloMessage(HelloMessage handshakeHelloMessage) {
        this.handshakeHelloMessage = handshakeHelloMessage;
    }

    public StatusMessage getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(StatusMessage statusMessage) {
        this.statusMessage = statusMessage;
    }
}
