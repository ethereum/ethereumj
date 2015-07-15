package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.net.client.PeerClient;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.crypto.SHA3Helper.sha3;

/**
 * @author Mikhail Kalinin
 * @since 14.07.2015
 */
public class MultiPeerStart {

    public static void main(String args[]) throws IOException, URISyntaxException, InterruptedException {
        CLIInterface.call(args);
        Ethereum ethereum = EthereumFactory.createEthereum();

        for(PeerConfig peer : PEERS) {
            ethereum.connect(
                    peer.getAddress(),
                    peer.getPort(),
                    peer.getNodeId()
            );
        }
    }

    private static List<PeerConfig> PEERS;
    static {
        PEERS = Arrays.asList(
                PeerConfig.create("peer-3.ether.camp", 30301, "peer-3.ether.camp"),
                PeerConfig.create("peer-2.ether.camp", 30301, "peer-2.ether.camp"),
                PeerConfig.create("peer-1.ether.camp", 30301, "peer-1.ether.camp")
        );
    }

    private static String createNodeId(String nodeName) {
        byte[] ecPublic = ECKey.fromPrivate(sha3(nodeName.getBytes())).getPubKeyPoint().getEncoded(false);
        byte[] nodeId = new byte[ecPublic.length - 1];
        System.arraycopy(ecPublic, 1, nodeId, 0, nodeId.length);
        return Hex.toHexString(nodeId);
    }

    private static class PeerConfig {
        private String address;
        private int port;
        private String nodeId;

        public static PeerConfig create(String address, int port, String nodeName) {
            String nodeId = createNodeId(nodeName);
            return new PeerConfig(address, port, nodeId);
        }

        private PeerConfig(String address, int port, String nodeId) {
            this.address = address;
            this.port = port;
            this.nodeId = nodeId;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public String getNodeId() {
            return nodeId;
        }
    }
}
