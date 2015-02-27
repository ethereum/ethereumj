package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.RLP;

import static org.ethereum.util.ByteUtil.longToBytes;
import static org.ethereum.util.ByteUtil.stripLeadingZeroes;

public class PingMessage extends Message {

    public static Message create(String ip, int port, ECKey privKey){

        long expiration = 3 + System.currentTimeMillis() / 1000;

        /* RLP Encode data */
        byte[] rlpIp    = RLP.encodeElement(ip.getBytes());

        byte[] tmpPort = longToBytes(port);
        byte[] rlpPort  = RLP.encodeElement(stripLeadingZeroes(tmpPort));

        byte[] tmpExp  = longToBytes(expiration);
        byte[] rlpExp   = RLP.encodeElement(stripLeadingZeroes(tmpExp));

        byte[] type = new byte[]{1};
        byte[] data = RLP.encodeList(rlpIp, rlpPort, rlpExp);

        PingMessage ping = new PingMessage();
        ping.encode(type, data, privKey);

        return ping;
    }

    @Override
    public String toString() {
        return "PingMessage: " + super.toString();
    }
}
