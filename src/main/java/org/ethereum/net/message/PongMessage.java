package org.ethereum.net.message;

import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class PongMessage extends Message {

    public PongMessage() {
        this.payload = Hex.decode("C103");
    }

    @Override
    public void parseRLP() {
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getMessageName(){
        return "Pong";
    }

    @Override
    public Class getAnswerMessage() {
        return null;
    }
}

