package org.ethereum.net.message;

public abstract class ShhMessage extends Message {

    public ShhMessage() {
    }

    public ShhMessage(byte[] encoded) {
        super(encoded);
    }
}
