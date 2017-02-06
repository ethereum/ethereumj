package org.ethereum.net.par.message;

import org.ethereum.net.message.Message;

public abstract class ParMessage extends Message {

    public ParMessage() {
    }

    public ParMessage(byte[] encoded) {
        super(encoded);
    }

    abstract public ParMessageCodes getCommand();
}
