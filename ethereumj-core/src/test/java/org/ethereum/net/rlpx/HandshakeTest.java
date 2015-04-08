package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by android on 4/8/15.
 */
public class HandshakeTest {
    private ECKey myKey;
    private ECKey remoteKey;
    private Handshake handshake;

    @Before
    public void setUp() {
        remoteKey = new ECKey();
        myKey = new ECKey();
        handshake = new Handshake(remoteKey.getPubKeyPoint());
    }

    @Test
    public void testCreateAuthInitiate() throws Exception {
        AuthInitiateMessage message = handshake.createAuthInitiate(new byte[32], myKey);
        int expectedLength = 65+32+64+32+1;
        byte[] buffer = new byte[expectedLength];
        int offset = message.encode(buffer, 0);
        assertEquals(expectedLength, offset);
    }
}