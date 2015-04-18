package org.ethereum.net.rlpx;

import org.ethereum.net.p2p.P2pMessage;
import org.spongycastle.util.encoders.Hex;

import java.io.*;

/**
 * Created by devrandom on 2015-04-12.
 */
public class RlpxConnection {
    private final EncryptionHandshake.Secrets secrets;
    private final FrameCodec codec;
    private final DataInputStream inp;
    private final OutputStream out;
    private HandshakeMessage handshakeMessage;

    public RlpxConnection(EncryptionHandshake.Secrets secrets, InputStream inp, OutputStream out) {
        this.secrets = secrets;
        this.inp = new DataInputStream(inp);
        this.out = out;
        this.codec = new FrameCodec(secrets);
    }

    public void sendProtocolHandshake(HandshakeMessage message) throws IOException {
        byte[] payload = message.encode();
        codec.writeFrame(new FrameCodec.Frame(HandshakeMessage.HANDSHAKE_MESSAGE_TYPE, payload), out);
    }

    public void handleNextMessage() throws IOException {
        FrameCodec.Frame frame = codec.readFrame(inp);
        if (handshakeMessage == null) {
            if (frame.type != HandshakeMessage.HANDSHAKE_MESSAGE_TYPE)
                throw new IOException("expected handshake or disconnect");
            // TODO handle disconnect
            byte[] wire = new byte[frame.size];
            frame.payload.read(wire);
            System.out.println("packet " + Hex.toHexString(wire));
            handshakeMessage = HandshakeMessage.parse(wire);
        } else {
            System.out.println("packet type " + frame.type);
            byte[] wire = new byte[frame.size];
            frame.payload.read(wire);
            System.out.println("packet " + Hex.toHexString(wire));
        }
    }

    public HandshakeMessage getHandshakeMessage() {
        return handshakeMessage;
    }

    public void writeMessage(P2pMessage message) throws IOException {
        byte[] payload = message.getEncoded();
        codec.writeFrame(new FrameCodec.Frame(message.getCommand().asByte(), payload), out);
    }
}
