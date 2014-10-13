package org.ethereum.net.peerdiscovery;

import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This class models a peer in the network
 */
public class PeerData {

	private InetAddress address;
	private int port;
	private String peerId;

	private List<String> capabilities;

	private transient boolean isOnline = false;
	private transient long lastCheckTime = 0;

	public PeerData(InetAddress ip, int port, String peerId) {
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

	public List<String> getCapabilities() {
		return capabilities;
	}

	public byte[] getEncoded() {
		byte[] ip = RLP.encodeElement(this.address.getAddress());
		byte[] port = RLP.encodeInt(this.port);
		byte[] peerId = RLP.encodeElement(Hex.decode(this.peerId));
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
		PeerData peerData = (PeerData) obj;
		return peerData.peerId.equals(this.peerId)
				|| this.getAddress().equals(peerData.getAddress());
	}

	@Override
	public int hashCode() {
		return -1; // override for equals function
	}
}
