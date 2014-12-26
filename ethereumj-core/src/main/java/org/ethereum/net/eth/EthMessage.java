package org.ethereum.net.eth;

import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.message.Message;

public abstract class EthMessage extends Message {

    public EthMessage() {
    }

    public EthMessage(byte[] encoded) {
        super(encoded);
    }

    public EthMessageCodes getCommand() {
        return EthMessageCodes.fromByte(code);
    }
}
