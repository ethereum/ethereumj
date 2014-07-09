package org.ethereum.net.message;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.ethereum.net.Command.HELLO;

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
    public Class getAnswerMessage() {
        return PongMessage.class;
    }
}

