package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;

import static org.ethereum.util.ByteUtil.longToBytes;
import static org.ethereum.util.ByteUtil.stripLeadingZeroes;

public class PingMessage extends Message {

    String host;
    int port;
    long expires;

    public static PingMessage create(String host, int port, ECKey privKey) {

        long expiration = 3 + System.currentTimeMillis() / 1000;

        /* RLP Encode data */
        byte[] rlpIp = RLP.encodeElement(host.getBytes());

        byte[] tmpPort = longToBytes(port);
        byte[] rlpPort = RLP.encodeElement(stripLeadingZeroes(tmpPort));

        byte[] tmpExp = longToBytes(expiration);
        byte[] rlpExp = RLP.encodeElement(stripLeadingZeroes(tmpExp));

        byte[] type = new byte[]{1};
        byte[] data = RLP.encodeList(rlpIp, rlpPort, rlpExp);

        PingMessage ping = new PingMessage();
        ping.encode(type, data, privKey);

        ping.expires = expiration;
        ping.host = host;
        ping.port = port;

        return ping;
    }

    @Override
    public void parse(byte[] data) {

        RLPList list = RLP.decode2(data);
        list = (RLPList) list.get(0);

        byte[] ipB = list.get(0).getRLPData();
        this.host = new String(ipB, Charset.forName("UTF-8"));

        this.port = ByteUtil.byteArrayToInt(list.get(1).getRLPData());

        RLPItem expires = (RLPItem) list.get(2);
        this.expires = ByteUtil.byteArrayToLong(expires.getRLPData());
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getExpires() {
        return expires;
    }

    @Override
    public String toString() {

        long currTime = System.currentTimeMillis() / 1000;

        String out = String.format("[PingMessage] \n host: %s port: %d \n expires in %d seconds \n %s\n",
                host, port, (expires - currTime), super.toString());

        return out;
    }
}
