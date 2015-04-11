package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by devrandom on 2015-04-09.
 */
public class Handshaker {
    private final ECKey myKey;

    public static void main(String[] args) throws IOException {
        new Handshaker().doHandshake(args[0], Integer.parseInt(args[1]), args[2]);
    }

    Handshaker() {
        myKey = new ECKey().decompress();
    }

    private void doHandshake(String host, int port, String remoteIdHex) throws IOException {
        byte[] remoteId = Hex.decode(remoteIdHex);
        byte[] remotePublicBytes = new byte[remoteId.length + 1];
        System.arraycopy(remoteId, 0, remotePublicBytes, 1, remoteId.length);
        remotePublicBytes[0] = 0x04; // uncompressed
        ECPoint remotePublic = ECKey.fromPublicOnly(remotePublicBytes).getPubKeyPoint();
        Socket sock = new Socket(host, port);
        InputStream inp = sock.getInputStream();
        OutputStream out = sock.getOutputStream();
        EncryptionHandshake initiator = new EncryptionHandshake(remotePublic);
        AuthInitiateMessage initiateMessage = initiator.createAuthInitiate(null, myKey);
        byte[] initiatePacket = initiator.encryptAuthMessage(initiateMessage);

        out.write(initiatePacket);
        byte[] responsePacket = new byte[AuthResponseMessage.getLength() + ECIESCoder.getOverhead()];
        int n = inp.read(responsePacket);
        if (n < responsePacket.length)
            throw new IOException("could not read, got " + n);

        AuthResponseMessage responseMessage = initiator.decryptAuthResponse(responsePacket, myKey);
        initiator.handleAuthResponse(initiateMessage, responseMessage);
        System.out.println(Hex.toHexString(initiator.getSecrets().aes));
    }
}
