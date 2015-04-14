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
import java.util.Arrays;

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
        System.out.println("Node ID " + Hex.toHexString(nodeId));
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
        byte[] buf = new byte[initiator.getSecrets().getEgressMac().getDigestSize()];
        new SHA3Digest(initiator.getSecrets().getEgressMac()).doFinal(buf, 0);
        new SHA3Digest(initiator.getSecrets().getIngressMac()).doFinal(buf, 0);
        RlpxConnection conn =  new RlpxConnection(initiator.getSecrets(), inp, out);
        HandshakeMessage handshakeMessage = new HandshakeMessage(
                3,
                "computronium1",
                Lists.newArrayList(
                        new Capability("eth", (byte) 60),
                        new Capability("shh", (byte) 2)
                ),
                3333,
                nodeId
        );

        conn.sendProtocolHandshake(handshakeMessage);
        conn.handleNextMessage();
        if (!Arrays.equals(remoteId, conn.getHandshakeMessage().nodeId))
            throw new IOException("returns node ID doesn't match the node ID we dialed to");
        System.out.println(conn.getHandshakeMessage().caps);
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }
}
