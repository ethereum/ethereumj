package test.ethereum.net;

import org.ethereum.net.p2p.GetPeersMessage;
import org.ethereum.net.p2p.P2pMessageCodes;

import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class GetPeersMessageTest {

    /* GETPEERS_MESSAGE */

    @Test
    public void testGetPeers() {

        //Init
        GetPeersMessage getPeersMessage = new GetPeersMessage();

        //toString
        assertEquals("[GET_PEERS]", getPeersMessage.toString());

        //getEncoded
        assertEquals("C104", Hex.toHexString(getPeersMessage.getEncoded()).toUpperCase());

        //getAnswerMessage
        assertEquals(null, getPeersMessage.getAnswerMessage());

        //getCommand
        assertEquals(P2pMessageCodes.GET_PEERS, getPeersMessage.getCommand());
    }
}

