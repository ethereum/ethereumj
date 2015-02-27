package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;

import java.util.List;

public class NeighborsMessage extends Message {

    public static Message create(List<Node> nodes, ECKey privKey) {

        long expiration = System.currentTimeMillis();

        byte[][] nodeRLPs = null;

        if (nodes != null) {
            nodeRLPs =  new byte[nodes.size()][];
            int i = 0;
            for (Node node : nodes) {
                nodeRLPs[i] = node.getRLP();
                ++i;
            }
        }

        byte[] rlpListNodes = RLP.encodeList(nodeRLPs);
        byte[] rlpExp = RLP.encodeElement(ByteUtil.longToBytes(expiration));

        byte[] type = new byte[]{4};
        byte[] data = RLP.encodeList(rlpListNodes, rlpExp);

        NeighborsMessage neighborsMessage = new NeighborsMessage();
        neighborsMessage.encode(type, data, privKey);

        return neighborsMessage;
    }

    @Override
    public String toString() {
        return "NeighborsMessage: " + super.toString();
    }
}
