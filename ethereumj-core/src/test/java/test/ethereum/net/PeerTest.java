package test.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.Peer;

import org.spongycastle.util.encoders.Hex;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;
import org.junit.Test;

public class PeerTest {

    /* PEER */

    @Test
    public void testPeer() {
        
        //Init
        InetAddress address = InetAddress.getLoopbackAddress();
        List<Capability> capabilities = new ArrayList<>(); 
        int port = 1010;
        String peerId = "1010";
        Peer peerCopy = new Peer(address, port, peerId );

        //Peer
        Peer peer = new Peer(address, port, peerId );

        //getAddress
        assertEquals( "127.0.0.1" , peer.getAddress().getHostAddress() );

        //getPort
        assertEquals( port , peer.getPort() );

        //getPeerId
        assertEquals( peerId , peer.getPeerId() );

        //getCapabilities
        assertEquals( capabilities , peer.getCapabilities() );
                                       
        //getEncoded
        assertEquals("CC847F0000018203F2821010C0", Hex.toHexString(  peer.getEncoded() ).toUpperCase() );
        
        //toString
        assertEquals("[ip=" + address.getHostAddress() + " port=" + Integer.toString( port ) + " peerId=" + peerId + "]", peer.toString() );

        //equals
        assertEquals(true, peer.equals( peerCopy ) );
        assertEquals(false, peer.equals( null ) );

        //hashCode
        assertEquals(-1, peer.hashCode());
    }
}

