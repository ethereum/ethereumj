package org.ethereum.net.rlpx;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.util.Arrays;

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
        byte[] idB;

        if (nodeRLP.size() > 3) {
            idB = nodeRLP.get(3).getRLPData();
        } else {
            idB = nodeRLP.get(2).getRLPData();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(hostB[0] & 0xFF);
        sb.append(".");
        sb.append(hostB[1] & 0xFF);
        sb.append(".");
        sb.append(hostB[2] & 0xFF);
        sb.append(".");
        sb.append(hostB[3] & 0xFF);

//        String host = new String(hostB, Charset.forName("UTF-8"));
        String host = sb.toString();
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

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o instanceof Node) {
            return Arrays.equals(((Node) o).getId(), this.getId());
        }

        return false;
    }
}
