package org.ethereum.net.rlpx;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;

import static org.ethereum.util.ByteUtil.byteArrayToInt;
import static org.ethereum.util.ByteUtil.intToBytes;


public class Node {

    byte[] id;
    String host;
    int port;

    public Node(byte[] id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public Node(byte[] rlp) {

        RLPList nodeRLP = RLP.decode2(rlp);
        nodeRLP = (RLPList) nodeRLP.get(0);

        byte[] hostB = nodeRLP.get(0).getRLPData();
        byte[] portB = nodeRLP.get(1).getRLPData();
        byte[] idB = nodeRLP.get(2).getRLPData();

        String host = new String(hostB, Charset.forName("UTF-8"));
        int port = byteArrayToInt(portB);

        this.host = host;
        this.port = port;
        this.id = idB;
    }


    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getRLP() {

        byte[] rlphost = RLP.encodeElement(host.getBytes(Charset.forName("UTF-8")));
        byte[] rlpPort = RLP.encodeElement(intToBytes(port));
        byte[] rlpId = RLP.encodeElement(id);

        byte[] data = RLP.encodeList(rlphost, rlpPort, rlpId);
        return data;
    }

    @Override
    public String toString() {
        return "Node{" +
                " host='" + host + '\'' +
                ", port=" + port +
                ", id=" + Hex.toHexString(id) +
                '}';
    }
}
