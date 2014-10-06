package org.ethereum.net.message;

public abstract class EthMessage extends Message {
	
	public EthMessage() {}

    public EthMessage(byte[] encoded) {
        super(encoded);
    }
}
