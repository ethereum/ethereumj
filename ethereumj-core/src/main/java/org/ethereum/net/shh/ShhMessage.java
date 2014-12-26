package org.ethereum.net.shh;

import org.ethereum.net.message.Message;

public abstract class ShhMessage extends Message {

    public ShhMessage() {
    }

    public ShhMessage(byte[] encoded) {
        super(encoded);
    }

    public ShhMessageCodes getCommand() {
        return ShhMessageCodes.fromByte(code);
    }
}
