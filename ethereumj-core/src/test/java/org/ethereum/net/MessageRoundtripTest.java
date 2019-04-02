package org.ethereum.net;

import org.ethereum.net.p2p.P2pMessage;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author alexbraz
 * @since 29/03/2019
 */
public class MessageRoundtripTest {

    @Test
    public void testMessageNotAnswered(){
        P2pMessage message = mock(P2pMessage.class);
        MessageRoundtrip messageRoundtrip = new MessageRoundtrip(message);
        assertFalse(messageRoundtrip.isAnswered());
    }

    @Test
    public void testMessageRetryTimes(){
        P2pMessage message = mock(P2pMessage.class);
        MessageRoundtrip messageRoundtrip = new MessageRoundtrip(message);
        messageRoundtrip.incRetryTimes();
        assertEquals(1L, messageRoundtrip.retryTimes);
    }

    @Test
    public void testMessageAnswered(){
        P2pMessage message = mock(P2pMessage.class);
        MessageRoundtrip messageRoundtrip = new MessageRoundtrip(message);
        messageRoundtrip.answer();
        assertTrue(messageRoundtrip.isAnswered());
    }

    @Test
    public void testHasToRetry(){
            P2pMessage message = mock(P2pMessage.class);
            MessageRoundtrip messageRoundtrip = new MessageRoundtrip(message);
            messageRoundtrip.saveTime();
            assertFalse(messageRoundtrip.hasToRetry());
    }

}
