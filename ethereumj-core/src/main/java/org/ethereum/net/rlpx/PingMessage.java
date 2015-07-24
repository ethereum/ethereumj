package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
//import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;

import static org.ethereum.util.ByteUtil.longToBytes;
import static org.ethereum.util.ByteUtil.stripLeadingZeroes;

public class PingMessage extends Message {

    String host;
    int port;
    long expires;

    public static PingMessage create(String host, int port, ECKey privKey) {

        long expiration = 60 + System.currentTimeMillis() / 1000;

        /* RLP Encode data */
        byte[] rlpIp = RLP.encodeElement(host.getBytes());

        byte[] tmpPort = longToBytes(port);
        byte[] rlpPort = RLP.encodeElement(stripLeadingZeroes(tmpPort));

        byte[] rlpIpTo = RLP.encodeElement(host.getBytes());

        byte[] tmpPortTo = longToBytes(port);
        byte[] rlpPortTo = RLP.encodeElement(stripLeadingZeroes(tmpPortTo));

        byte[] tmpExp = longToBytes(expiration);
        byte[] rlpExp = RLP.encodeElement(stripLeadingZeroes(tmpExp));

        byte[] type = new byte[]{1};
        byte[] version = new byte[]{4};
        byte[] rlpVer = RLP.encodeElement(version);
        byte[] rlpFromList = RLP.encodeList(rlpIp, rlpPort, rlpPort);
        byte[] rlpToList = RLP.encodeList(rlpIpTo, rlpPortTo, rlpPortTo);
        byte[] data = RLP.encodeList(rlpVer, rlpFromList, rlpToList, rlpExp);

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
        RLPList dataList = (RLPList) list.get(0);
        RLPList fromList = (RLPList) dataList.get(2);

        byte[] ipB = fromList.get(0).getRLPData();
        this.host = new String(ipB, Charset.forName("UTF-8"));

        this.port = ByteUtil.byteArrayToInt(fromList.get(1).getRLPData());

        RLPItem expires = (RLPItem) dataList.get(3);
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
