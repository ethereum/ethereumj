package test.ethereum.net;

import org.ethereum.net.p2p.P2pMessageCodes;
import org.ethereum.net.p2p.PingMessage;
import org.ethereum.net.p2p.PongMessage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PingPongMessageTest {

    /* PING_MESSAGE & PONG_MESSAGE */

    @Test /* PingMessage */
    public void testPing() {

        PingMessage pingMessage = new PingMessage();
        System.out.println(pingMessage);

        assertEquals(PongMessage.class, pingMessage.getAnswerMessage());

        assertEquals(P2pMessageCodes.PING, pingMessage.getCommand());
    }

    @Test /* PongMessage */
    public void testPong() {

        PongMessage pongMessage = new PongMessage();
        System.out.println(pongMessage);

        assertEquals(P2pMessageCodes.PONG, pongMessage.getCommand());
        assertEquals(null, pongMessage.getAnswerMessage());
    }
}

