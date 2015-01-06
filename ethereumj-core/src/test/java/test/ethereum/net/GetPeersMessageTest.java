package test.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.spongycastle.util.encoders.Hex;
import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.GetPeersMessage;
import org.junit.Test;

public class GetPeersMessageTest {

    /* GETPEERS_MESSAGE */

    @Test
    public void testGetPeers() {
        
        //Init
        GetPeersMessage getPeersMessage = new GetPeersMessage();

        //System.out.println(getPeersMessage.getEncoded());

        //toString
        assertEquals("[GET_PEERS]", getPeersMessage.toString());

        //getEncoded
        assertEquals("C104", Hex.toHexString(  getPeersMessage.getEncoded() ).toUpperCase() );

        //getAnswerMessage
        assertEquals(null, getPeersMessage.getAnswerMessage());
        
        //getCommand
        assertEquals(P2pMessageCodes.GET_PEERS, getPeersMessage.getCommand());
        
    }
}

