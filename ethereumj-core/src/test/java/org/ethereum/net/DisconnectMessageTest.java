package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import org.ethereum.net.message.DisconnectMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class DisconnectMessageTest {

    /* DISCONNECT_MESSAGE */

    @Test /* DisconnectMessage 1 */
    public void test_3() {

        String disconnectMessageRaw = "C20100";
        byte[] payload = Hex.decode(disconnectMessageRaw);
        RLPList rlpList = RLP.decode2(payload);

        DisconnectMessage disconnectMessage = new DisconnectMessage(rlpList);
        System.out.println(disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.REQUESTED);
    }

    @Test /* DisconnectMessage 2 */
    public void test_4() {

        String disconnectMessageRaw = "C20101";
        byte[] payload = Hex.decode(disconnectMessageRaw);
        RLPList rlpList = RLP.decode2(payload);

        DisconnectMessage disconnectMessage = new DisconnectMessage(rlpList);
        System.out.println(disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.TCP_ERROR);
    }
}

