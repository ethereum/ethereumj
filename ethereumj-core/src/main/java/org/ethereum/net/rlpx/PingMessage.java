package org.ethereum.net.rlpx;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;

public class PingMessage extends Message {

    public static Message create(String ip, int port){

        long expiration = System.currentTimeMillis();

        /* RLP Encode data */
        byte[] rlpIp    = RLP.encodeElement(ip.getBytes());
        byte[] rlpPort  = RLP.encodeElement(ByteUtil.longToBytes(port));
        byte[] rlpExp   = RLP.encodeElement(ByteUtil.longToBytes(expiration));

        byte[] type = new byte[]{1};
        byte[] data = RLP.encodeList(rlpIp, rlpPort, rlpExp);

        PingMessage ping = new PingMessage();
        ping.encode(type, data);

        return ping;
    }

    @Override
    public String toString() {
        return "PingMessage: " + super.toString();
    }
}
