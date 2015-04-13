package org.ethereum.net.rlpx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by devrandom on 2015-04-12.
 */
public class RlpxConnection {
    private final EncryptionHandshake.Secrets secrets;
    private final FrameCodec codec;
    private HandshakeMessage handshakeMessage;

    public RlpxConnection(EncryptionHandshake.Secrets secrets, InputStream inp, OutputStream out) {
        this.secrets = secrets;
        this.codec = new FrameCodec(secrets, inp, out);
    }

    public RlpxConnection(EncryptionHandshake.Secrets secrets, FrameCodec codec) {
        this.secrets = secrets;
        this.codec = codec;
    }

    public void sendProtocolHandshake(HandshakeMessage message) throws IOException {
        byte[] payload = message.encode();
        codec.writeFrame(new FrameCodec.Frame(HandshakeMessage.HANDSHAKE_MESSAGE_TYPE, payload));
    }

    public void handleNextMessage() throws IOException {
        FrameCodec.Frame frame = codec.readFrame();
        if (handshakeMessage == null) {
            if (frame.type != HandshakeMessage.HANDSHAKE_MESSAGE_TYPE)
                throw new IOException("expected handshake or disconnect");
            // TODO handle disconnect
            byte[] wire = new byte[frame.size];
            frame.payload.read(wire);
            handshakeMessage = HandshakeMessage.parse(wire);
        }
    }

    public HandshakeMessage getHandshakeMessage() {
        return handshakeMessage;
    }
}
