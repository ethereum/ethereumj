package org.ethereum.net.rlpx;

import com.google.common.collect.Lists;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.client.Capability;
import org.ethereum.util.DecodeResult;
import org.ethereum.util.RLP;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.util.encoders.Hex;

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

    @Before
    public void setUp() throws IOException {
        ECKey remoteKey = new ECKey().decompress();
        ECKey myKey = new ECKey().decompress();
        initiator = new EncryptionHandshake(remoteKey.getPubKeyPoint());
        responder = new EncryptionHandshake();
        AuthInitiateMessage initiate = initiator.createAuthInitiate(null, myKey);
        AuthResponseMessage response = responder.handleAuthInitiate(initiate, remoteKey);
        initiator.handleAuthResponse(initiate, response);
        PipedInputStream to = new PipedInputStream(1024*1024);
        PipedOutputStream toOut = new PipedOutputStream(to);
        PipedInputStream from = new PipedInputStream(1024*1024);
        PipedOutputStream fromOut = new PipedOutputStream(from);
        iCodec = new FrameCodec(initiator.getSecrets(), to, fromOut);
        rCodec = new FrameCodec(responder.getSecrets(), from, toOut);
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
        iCodec.writeFrame(frame);
        FrameCodec.Frame frame1 = rCodec.readFrame();
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
        RlpxConnection iConn =  new RlpxConnection(initiator.getSecrets(), iCodec);
        RlpxConnection rConn =  new RlpxConnection(responder.getSecrets(), rCodec);
        iConn.sendProtocolHandshake(iMessage);
        rConn.handleNextMessage();
        HandshakeMessage receivedMessage = rConn.getHandshakeMessage();
        assertNotNull(receivedMessage);
        assertArrayEquals(iMessage.nodeId, receivedMessage.nodeId);
    }

    @Test
    public void blah() {
        Object[] stuff = new Object[]{1L, 3, "asdf"};
        byte[] res = RLP.encode(stuff);
        DecodeResult result = RLP.decode(res, 0);
        System.out.println(result.getDecoded());

        SHA3Digest d = new SHA3Digest();
        d.update((byte)11);
        d.update((byte) 22);
        byte[] buf = new byte[d.getDigestSize()];
        new SHA3Digest(d).doFinal(buf, 0);
        System.out.println(Hex.toHexString(buf));
        d.update((byte) 33);
        d.doFinal(buf, 0);
        System.out.println(Hex.toHexString(buf));

        d = new SHA3Digest();
        d.update((byte) 11);
        d.update((byte) 22);
        d.doFinal(buf, 0);
        System.out.println(Hex.toHexString(buf));
        d = new SHA3Digest();
        d.update((byte) 11);
        d.update((byte) 22);
        d.update((byte) 33);
        d.doFinal(buf, 0);
        System.out.print(Hex.toHexString(buf));
        System.out.println(".");
    }
}