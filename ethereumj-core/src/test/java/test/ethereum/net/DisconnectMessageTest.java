package test.ethereum.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.System;

import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.p2p.DisconnectMessage;

import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class DisconnectMessageTest {

    /* DISCONNECT_MESSAGE */

    @Test /* DisconnectMessage 1 - Requested */
    public void test_1() {

        String disconnectMessageRaw = "C20100";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
        System.out.println(disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.REQUESTED);
    }

    @Test /* DisconnectMessage 2 - TCP Error */
    public void test_2() {

        String disconnectMessageRaw = "C20101";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
        System.out.println(disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.TCP_ERROR);
    }

    @Test /* DisconnectMessage 2 - from constructor */
    public void test_3() {

        DisconnectMessage disconnectMessage = new DisconnectMessage(ReasonCode.INCOMPATIBLE_NETWORK);
        System.out.println(disconnectMessage);

        String expected = "c20107";
        assertEquals(expected, Hex.toHexString(disconnectMessage.getEncoded()));

        assertEquals(ReasonCode.INCOMPATIBLE_NETWORK, disconnectMessage.getReason());
    }

    @Test //handling boundary-high
    public void test_4() {

        String disconnectMessageRaw = "C28080";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
        System.out.println(disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.REQUESTED); //high numbers are zeroed
    }

    @Test //handling boundary-low
    public void test_5() {

        String disconnectMessageRaw = "C20000";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
        System.out.println(disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.REQUESTED);
    }

    @Test //handling boundary-low minus 1 (error)
    public void test_6() {

        String disconnectMessageRaw = "C19999";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        try{
          DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
          disconnectMessage.toString(); //throws exception
          assertTrue("Valid raw encoding for disconnectMessage", false);
        } catch (RuntimeException e) {
          assertTrue("Invalid raw encoding for disconnectMessage", true);
        }
    }

    @Test //handling boundary-high plus 1 (error)
    public void test_7() {

        String disconnectMessageRaw = "C28081";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        try{
          DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
          disconnectMessage.toString(); //throws exception
          assertTrue("Valid raw encoding for disconnectMessage", false);
        } catch (RuntimeException e) {
          assertTrue("Invalid raw encoding for disconnectMessage", true);
        }
    }
}

