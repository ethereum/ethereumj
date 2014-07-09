package org.ethereum.net.message;

import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class GetPeersMessage extends Message {

    public GetPeersMessage() {
        this.payload = Hex.decode("C110");
    }

    @Override
    public void parseRLP() {
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String getMessageName(){
        return "GetPeers";
    }

    @Override
    public Class getAnswerMessage() {
        return PeersMessage.class;
    }
}

