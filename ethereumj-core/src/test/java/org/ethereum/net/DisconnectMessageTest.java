package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.ReasonCode;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

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
}

