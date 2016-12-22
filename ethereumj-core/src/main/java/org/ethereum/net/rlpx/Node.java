package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.mapdb.Serializers;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.mapdb.Serializer;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.byteArrayToInt;

public class Node implements Serializable {
    private static final long serialVersionUID = -4267600517925770636L;

    public static final Serializer<Node> MapDBSerializer = new Serializer<Node>() {
        @Override
        public void serialize(DataOutput out, Node value) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            Serializers.BYTE_ARRAY_WRAPPER.serialize(out, new ByteArrayWrapper(baos.toByteArray()));
        }

        @Override
        public Node deserialize(DataInput in, int available) throws IOException {
            ByteArrayWrapper bytes = Serializers.BYTE_ARRAY_WRAPPER.deserialize(in, available);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            try {
                return (Node) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                ois.close();
            }
        }
    };

    byte[] id;
    String host;
    int port;
    // discovery endpoint doesn't have real nodeId for example
    private boolean isFakeNodeId = false;

    public static Node createWithoutId(String addressOrEnode) {
        try {
            URI uri = new URI(addressOrEnode);
            if (uri.getScheme().equals("enode")) {
                return new Node(addressOrEnode);
            }
        } catch (URISyntaxException e) {
            // continue
        }

        final ECKey generatedNodeKey = ECKey.fromPrivate(sha3(addressOrEnode.getBytes()));
        final String generatedNodeId = Hex.toHexString(generatedNodeKey.getNodeId());
        final Node node = new Node("enode://" + generatedNodeId + "@" + addressOrEnode);
        node.isFakeNodeId = true;
        return node;
    }

    public Node(String enodeURL) {
        try {
            URI uri = new URI(enodeURL);
            if (!uri.getScheme().equals("enode")) {
                throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT");
            }
            this.id = Hex.decode(uri.getUserInfo());
            this.host = uri.getHost();
            this.port = uri.getPort();
        } catch (URISyntaxException e) {
            throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT", e);
        }
    }

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

    /**
     * @return true if this node is endpoint for discovery loaded from config
     */
    public boolean isDiscoveryNode() {
        return isFakeNodeId;
    }


    public byte[] getId() {
        return id;
    }

    public String getHexId() {
        return Hex.toHexString(id);
    }

    public String getHexIdShort() {
        return Utils.getNodeIdShort(getHexId());
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
        byte[] ip;
        try {
            ip = InetAddress.getByName(host).getAddress();
        } catch (UnknownHostException e) {
            ip = new byte[4];  // fall back to invalid 0.0.0.0 address
        }

        byte[] rlphost = RLP.encodeElement(ip);
        byte[] rlpTCPPort = RLP.encodeInt(port);
        byte[] rlpUDPPort = RLP.encodeInt(port);
        byte[] rlpId = RLP.encodeElement(id);

        byte[] data = RLP.encodeList(rlphost, rlpUDPPort, rlpTCPPort, rlpId);
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
