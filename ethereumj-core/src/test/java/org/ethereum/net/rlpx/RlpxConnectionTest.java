/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.rlpx;

import com.google.common.collect.Lists;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.client.Capability;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.security.SecureRandom;

import static org.junit.Assert.*;

/**
 * Created by devrandom on 2015-04-11.
 */
public class RlpxConnectionTest {
    private FrameCodec iCodec;
    private FrameCodec rCodec;
    private EncryptionHandshake initiator;
    private EncryptionHandshake responder;
    private HandshakeMessage iMessage;
    private PipedInputStream to;
    private PipedOutputStream toOut;
    private PipedInputStream from;
    private PipedOutputStream fromOut;

    @Before
    public void setUp() throws Exception {
        ECKey remoteKey = new ECKey();
        ECKey myKey = new ECKey();
        initiator = new EncryptionHandshake(remoteKey.getPubKeyPoint());
        responder = new EncryptionHandshake();
        AuthInitiateMessage initiate = initiator.createAuthInitiate(null, myKey);
        byte[] initiatePacket = initiator.encryptAuthMessage(initiate);
        byte[] responsePacket = responder.handleAuthInitiate(initiatePacket, remoteKey);
        initiator.handleAuthResponse(myKey, initiatePacket, responsePacket);
        to = new PipedInputStream(1024*1024);
        toOut = new PipedOutputStream(to);
        from = new PipedInputStream(1024*1024);
        fromOut = new PipedOutputStream(from);
        iCodec = new FrameCodec(initiator.getSecrets());
        rCodec = new FrameCodec(responder.getSecrets());
        byte[] nodeId = {1, 2, 3, 4};
        iMessage = new HandshakeMessage(
                123,
                "abcd",
                Lists.newArrayList(
                        new Capability("zz", (byte) 1),
                        new Capability("yy", (byte) 3)
                ),
                3333,
                nodeId
        );
    }

    @Test
    public void testFrame() throws Exception {
        byte[] payload = new byte[123];
        new SecureRandom().nextBytes(payload);
        FrameCodec.Frame frame = new FrameCodec.Frame(12345, 123, new ByteArrayInputStream(payload));
        iCodec.writeFrame(frame, toOut);
        FrameCodec.Frame frame1 = rCodec.readFrames(new DataInputStream(to)).get(0);
        byte[] payload1 = new byte[frame1.size];
        assertEquals(frame.size, frame1.size);
        frame1.payload.read(payload1);
        assertArrayEquals(payload, payload1);
        assertEquals(frame.type, frame1.type);
    }

    @Test
    public void testMessageEncoding() throws IOException {
        byte[] wire = iMessage.encode();
        HandshakeMessage message1 = HandshakeMessage.parse(wire);
        assertEquals(123, message1.version);
        assertEquals("abcd", message1.name);
        assertEquals(3333, message1.listenPort);
        assertArrayEquals(message1.nodeId, message1.nodeId);
        assertEquals(iMessage.caps, message1.caps);
    }

    @Test
    public void testHandshake() throws IOException {
        RlpxConnection iConn =  new RlpxConnection(initiator.getSecrets(), from, toOut);
        RlpxConnection rConn =  new RlpxConnection(responder.getSecrets(), to, fromOut);
        iConn.sendProtocolHandshake(iMessage);
        rConn.handleNextMessage();
        HandshakeMessage receivedMessage = rConn.getHandshakeMessage();
        assertNotNull(receivedMessage);
        assertArrayEquals(iMessage.nodeId, receivedMessage.nodeId);
    }
}
