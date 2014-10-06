package org.ethereum.net.message;

public abstract class P2pMessage extends Message {

	public P2pMessage() {}

    public P2pMessage(byte[] encoded) {
        super(encoded);
    }
}
