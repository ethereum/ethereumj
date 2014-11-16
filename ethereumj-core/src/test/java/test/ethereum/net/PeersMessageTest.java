package test.ethereum.net;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ethereum.net.p2p.GetPeersMessage;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.Peer;
import org.ethereum.net.p2p.PeersMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

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

        PeersMessage peersMessage= new PeersMessage(payload);
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


}