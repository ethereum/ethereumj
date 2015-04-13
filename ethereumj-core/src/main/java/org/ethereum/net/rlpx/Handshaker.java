package org.ethereum.net.rlpx;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.client.Capability;
import org.spongycastle.crypto.digests.SHA3Digest;
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
    private final byte[] nodeId;

    public static void main(String[] args) throws IOException {
        new Handshaker().doHandshake(args[0], Integer.parseInt(args[1]), args[2]);
    }

    Handshaker() {
        myKey = new ECKey().decompress();
        byte[] nodeIdWithFormat = myKey.getPubKey();
        nodeId = new byte[nodeIdWithFormat.length - 1];
        System.arraycopy(nodeIdWithFormat, 1, nodeId, 0, nodeId.length);
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

        initiator.handleAuthResponse(myKey, initiatePacket, responsePacket);
        System.out.println(Hex.toHexString(initiator.getSecrets().aes));
        byte[] buf = new byte[32];
        new SHA3Digest(initiator.getSecrets().getEgressMac()).doFinal(buf, 0);
        System.out.println(Hex.toHexString(buf));
        new SHA3Digest(initiator.getSecrets().getIngressMac()).doFinal(buf, 0);
        System.out.println(Hex.toHexString(buf));
        RlpxConnection conn =  new RlpxConnection(initiator.getSecrets(), inp, out);
        HandshakeMessage handshakeMessage = new HandshakeMessage(
                123,
                "abcd",
                Lists.newArrayList(
                        new Capability("zz", (byte) 1),
                        new Capability("yy", (byte) 3)
                ),
                3333,
                nodeId
        );

        conn.sendProtocolHandshake(handshakeMessage);
        delay(1000);
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }
}
