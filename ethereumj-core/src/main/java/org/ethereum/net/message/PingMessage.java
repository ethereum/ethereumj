package org.ethereum.net.message;

import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class PingMessage extends Message {

    public PingMessage() {
        this.payload = Hex.decode("C102");
    }

    @Override
    public void parseRLP() {
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String getMessageName(){
        return "Ping";
    }

    @Override
    public Class<PongMessage> getAnswerMessage() {
        return PongMessage.class;
    }
}

