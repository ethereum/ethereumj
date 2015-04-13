package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by android on 4/8/15.
 */
public class EncryptionHandshakeTest {
    private ECKey myKey;
    private ECKey remoteKey;
    private EncryptionHandshake initiator;

    @Before
    public void setUp() {
        remoteKey = new ECKey().decompress();
        myKey = new ECKey().decompress();
        initiator = new EncryptionHandshake(remoteKey.getPubKeyPoint());
    }

    @Test
    public void testCreateAuthInitiate() throws Exception {
        AuthInitiateMessage message = initiator.createAuthInitiate(new byte[32], myKey);
        int expectedLength = 65+32+64+32+1;
        byte[] buffer = message.encode();
        assertEquals(expectedLength, buffer.length);
    }

    @Test
    public void testAgreement() throws Exception {
        EncryptionHandshake responder = new EncryptionHandshake();
        AuthInitiateMessage initiate = initiator.createAuthInitiate(null, myKey);
        byte[] initiatePacket = initiator.encryptAuthMessage(initiate);
        byte[] responsePacket = responder.handleAuthInitiate(initiatePacket, remoteKey);
        initiator.handleAuthResponse(myKey, initiatePacket, responsePacket);
        assertArrayEquals(initiator.getSecrets().aes, responder.getSecrets().aes);
        assertArrayEquals(initiator.getSecrets().mac, responder.getSecrets().mac);
        assertArrayEquals(initiator.getSecrets().token, responder.getSecrets().token);
    }
}