package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.Command;
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
        
        assertEquals(Command.PING, pingMessage.getCommand());
    }
    
    @Test /* PongMessage */
    public void testPong() {

        PongMessage pongMessage = new PongMessage();
        System.out.println(pongMessage);

        assertEquals(Command.PONG, pongMessage.getCommand());
        assertEquals(null, pongMessage.getAnswerMessage());
    }
}

