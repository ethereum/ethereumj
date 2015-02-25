package org.ethereum.net.rlpx;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;

import java.nio.charset.Charset;

public class Node {

    byte[] id;
    String ip;
    int    port;

    public Node(byte[] id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getRLP(){

        byte[] rlpId    = RLP.encodeElement(id);
        byte[] rlpIp    = RLP.encodeElement(ip.getBytes(Charset.forName("UTF-8")));
        byte[] rlpPort  = RLP.encodeElement(ByteUtil.longToBytes(port));

        byte[] data = RLP.encodeList(rlpId, rlpIp, rlpPort);
        return data;
    }

}
