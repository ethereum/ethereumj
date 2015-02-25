package org.ethereum.net.rlpx;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;

public class PongMessage extends Message {

    public static Message create(byte[] token) {

        long expiration = System.currentTimeMillis();

        /* RLP Encode data */
        byte[] rlpToken = RLP.encodeElement(token);
        byte[] rlpExp = RLP.encodeElement(ByteUtil.longToBytes(expiration));

        byte[] type = new byte[]{2};
        byte[] data = RLP.encodeList(rlpToken, rlpExp);

        PongMessage pong = new PongMessage();
        pong.encode(type, data);

        return pong;
    }


    @Override
    public String toString() {
        return "PongMessage: " + super.toString();
    }


}
