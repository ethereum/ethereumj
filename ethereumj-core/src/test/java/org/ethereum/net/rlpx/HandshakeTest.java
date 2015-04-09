package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Created by android on 4/8/15.
 */
public class HandshakeTest {
    private ECKey myKey;
    private ECKey remoteKey;
    private Handshake initiator;

    @Before
    public void setUp() {
        remoteKey = new ECKey().decompress();
        myKey = new ECKey().decompress();
        initiator = new Handshake(remoteKey.getPubKeyPoint());
    }

    @Test
    public void testCreateAuthInitiate() throws Exception {
        AuthInitiateMessage message = initiator.createAuthInitiate(new byte[32], myKey);
        int expectedLength = 65+32+64+32+1;
        byte[] buffer = new byte[expectedLength];
        int offset = message.encode(buffer, 0);
        assertEquals(expectedLength, offset);
    }

    @Test
    public void testAgreement() throws Exception {
        Handshake responder = new Handshake();
        AuthInitiateMessage initiate = initiator.createAuthInitiate(null, myKey);
        AuthResponseMessage response = responder.handleAuthInitiate(initiate);
        initiator.handleAuthResponse(response);
        assertArrayEquals(initiator.getSecrets().aes, responder.getSecrets().aes);
        assertArrayEquals(initiator.getSecrets().mac, responder.getSecrets().mac);
        assertArrayEquals(initiator.getSecrets().token, responder.getSecrets().token);
    }
}