package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.PingMessage;
import org.ethereum.net.message.PongMessage;
import org.junit.Test;

public class PingPongMessageTest {

    /* PING_MESSAGE & PONG_MESSAGE */

    @Test /* PingMessage */
    public void testPing() {

        PingMessage pingMessage = new PingMessage();
        System.out.println(pingMessage);

        assertEquals(PongMessage.class, pingMessage.getAnswerMessage());
    }
    
    @Test /* PongMessage */
    public void testPong() {

        PongMessage pongMessage = new PongMessage();
        System.out.println(pongMessage);

        assertEquals(null, pongMessage.getAnswerMessage());
    }
}

