package org.ethereum.net.client;

import org.ethereum.net.message.HelloMessage;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 13/04/14 17:36
 */
public class Peer {

	private byte[] ip;
	private short port;
	private byte[] peerId;
    private List<String> capabilities;
    private HelloMessage handshake;

	private transient boolean isOnline = false;
	private transient long    lastCheckTime = 0;

    public Peer(byte[] ip, int port, byte[] peerId) {
        this.ip = ip;
        this.port = (short) (port & 0xFFFF);
        this.peerId = peerId;
        this.capabilities = new ArrayList<>();
    }

    public InetAddress getInetAddress() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new Error("malformed ip");
        }
        return addr;
    }

    public byte[] getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public byte[] getPeerId() {
        return peerId;
    }

    public boolean isOnline() {
        if (getCapabilities().size() < 0) return false;
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

    public List<String> getCapabilities() {

        if (handshake != null)
            return handshake.getCapabilities();
        else
            return new ArrayList<String>();
    }

    public HelloMessage getHandshake() {
        return handshake;
    }

    public void setHandshake(HelloMessage handshake) {
        this.handshake = handshake;
    }
    
    public byte[] getEncoded() {
    	byte[] ip				= RLP.encodeElement(this.ip);
    	byte[] port				= RLP.encodeShort(this.port);
    	byte[] peerId			= RLP.encodeElement(this.peerId);
    	byte[][] encodedCaps 	= new byte[this.capabilities.size()][];
    	for (int i = 0; i < this.capabilities.size(); i++) {
			encodedCaps[i] = RLP.encodeString(this.capabilities.get(i));
		}
        byte[] capabilities		= RLP.encodeList(encodedCaps);
        return RLP.encodeList(ip, port, peerId, capabilities);
    }

    @Override
    public String toString() {
		return "[ip=" + getInetAddress().getHostAddress() + 
				" port=" + getPort() +
				" peerId=" + (getPeerId() == null ? "" : Hex.toHexString(getPeerId()))
				+ "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Peer peerData = (Peer) obj;
        return this.getInetAddress().equals(peerData.getInetAddress());
    }

    @Override
    public int hashCode() {
        return getInetAddress().hashCode();
    }
}
