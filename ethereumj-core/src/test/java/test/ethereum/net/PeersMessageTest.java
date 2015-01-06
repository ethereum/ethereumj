package test.ethereum.net;

import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.GetPeersMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.Peer;
import org.ethereum.net.p2p.PeersMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class PeersMessageTest {

    /* GET_PEERS */
    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test /* GetPeersMessage */
    public void testGetPeers() {

        GetPeersMessage getPeersMessage = new GetPeersMessage();
        logger.info(getPeersMessage.toString());

        assertEquals(P2pMessageCodes.GET_PEERS, getPeersMessage.getCommand());
    }

    /* PEERS */

    @Test /* PeersMessage 1 from RLP */
    public void testPeers_1() {

        String peersMessageRaw = "f84b05f848846894d84870b84036659c3656c488437cceb11abeb9b9fc69b8055144a7e7db3584d03e606083f90e17a1d3021d674579407cdaaafdfeef485872ab719db9f2b6283f498bb90a71";
        byte[] payload = Hex.decode(peersMessageRaw);

        PeersMessage peersMessage = new PeersMessage(payload);
        logger.info(peersMessage.toString());

        assertEquals(1, peersMessage.getPeers().size());

        Iterator<Peer> it = peersMessage.getPeers().iterator();
        Peer peer = it.next();

        assertEquals(P2pMessageCodes.PEERS, peersMessage.getCommand());
        assertEquals("/104.148.216.72", peer.getAddress().toString());
        assertEquals(112, peer.getPort());
        assertEquals("36659c3656c488437cceb11abeb9b9fc69b8055144a7e7db3584d03e606083f90e17a1d3021d674579407cdaaafdfeef485872ab719db9f2b6283f498bb90a71",
                peer.getPeerId());
    }

    @Test /* PeersMessage 1 from constructor */
    public void testPeers_2() {
        //Init
        InetAddress address = InetAddress.getLoopbackAddress();
        List<Capability> capabilities = new ArrayList<>();
        int port = 112;
        String peerId = "36659c3656c488437cceb11abeb9b9fc69b8055144a7e7db3584d03e606083f90e" +
                "17a1d3021d674579407cdaaafdfeef485872ab719db9f2b6283f498bb90a71";

        Set<Peer> peers = new HashSet<>();
        peers.add(new Peer(address, port, peerId));

        PeersMessage peersMessage = new PeersMessage(peers);
        logger.info(peersMessage.toString());

        assertEquals(1, peersMessage.getPeers().size());

        Iterator<Peer> it = peersMessage.getPeers().iterator();
        Peer peer = it.next();

        assertEquals(P2pMessageCodes.PEERS, peersMessage.getCommand());
        assertEquals("127.0.0.1", peer.getAddress().getHostAddress());
        assertEquals(112, peer.getPort());
        assertEquals("36659c3656c488437cceb11abeb9b9fc69b8055144a7e7db3584d03e6" +
                "06083f90e17a1d3021d674579407cdaaafdfeef485872ab719db9f2b6283f498bb90a71", peer.getPeerId());
    }

    @Test /* failing test */
    public void testPeers_3() {
        //Init
        InetAddress address = InetAddress.getLoopbackAddress();
        List<Capability> capabilities = Arrays.asList(
                new Capability(null, (byte) 0),
                null //null here can cause NullPointerException when using toString
        ); //encoding null capabilities
        int port = -1; //invalid port
        String peerId = ""; //invalid peerid

        Set<Peer> peers = new HashSet<>();
        peers.add(new Peer(address, port, peerId));

        PeersMessage peersMessage = new PeersMessage(peers);
        logger.info(peersMessage.toString());

        assertEquals(1, peersMessage.getPeers().size());

        Iterator<Peer> it = peersMessage.getPeers().iterator();
        Peer peer = it.next();

        assertEquals(P2pMessageCodes.PEERS, peersMessage.getCommand());
        assertEquals("127.0.0.1", peer.getAddress().getHostAddress());
        assertEquals(-1, peer.getPort());
        assertEquals("", peer.getPeerId());
    }
}
