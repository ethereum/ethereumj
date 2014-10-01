package org.ethereum.net.client;

import org.ethereum.net.message.HelloMessage;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This class models a peer in the network
 */
public class Peer {

	private InetAddress address;
	private int port;
	private byte[] peerId;

	private List<String> capabilities;
	private HelloMessage handshake;

	private transient boolean isOnline = false;
	private transient long lastCheckTime = 0;

	public Peer(InetAddress ip, int port, byte[] peerId) {
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
		return peerId == null ? "" : Hex.toHexString(peerId);
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
		byte[] ip = RLP.encodeElement(this.address.getAddress());
		byte[] port = RLP.encodeInt(this.port);
		byte[] peerId = RLP.encodeElement(this.peerId);
		byte[][] encodedCaps = new byte[this.capabilities.size()][];
		for (int i = 0; i < this.capabilities.size(); i++) {
			encodedCaps[i] = RLP.encodeString(this.capabilities.get(i));
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
		return Arrays.equals(peerData.peerId, this.peerId)
				|| this.getAddress().equals(peerData.getAddress());
	}

	@Override
	public int hashCode() {
		return -1; // override for equals function
	}
}
