package org.ethereum.net.rlpx;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.client.Capability;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.PingMessage;
import org.ethereum.net.rlpx.EncryptionHandshake.Secrets;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by devrandom on 2015-04-09.
 */
public class Handshaker {
    private final ECKey myKey;
    private final byte[] nodeId;
    private Secrets secrets;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI uri = new URI(args[0]);
        if (!uri.getScheme().equals("enode"))
            throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT");

        new Handshaker().doHandshake(uri.getHost(), uri.getPort(), uri.getUserInfo());
    }

    public Handshaker() {
        myKey = new ECKey().decompress();
        nodeId = myKey.getNodeId();
        System.out.println("Node ID " + Hex.toHexString(nodeId));
    }

    /**
     * Sample output:
     * <pre>
     Node ID b7fb52ddb1f269fef971781b9568ad65d30ac3b6055ebd6a0a762e6b67a7c92bd7c1fdf3c7c722d65ae70bfe6a9a58443297485aa29e3acd9bdf2ee0df4f5c45
     packet f86b0399476574682f76302e392e372f6c696e75782f676f312e342e32ccc5836574683cc5837368680280b840f1c041a7737e8e06536d9defb92cb3db6ecfeb1b1208edfca6953c0c683a31ff0a478a832bebb6629e4f5c13136478842cc87a007729f3f1376f4462eb424ded
     [eth:60, shh:2]
     packet type 16
     packet f8453c7b80a0fd4af92a79c7fc2fd8bf0d342f2e832e1d4f485c85b9152d2039e03bc604fdcaa0fd4af92a79c7fc2fd8bf0d342f2e832e1d4f485c85b9152d2039e03bc604fdca
     packet type 24
     packet c102
     packet type 3
     packet c0
     packet type 1
     packet c180
     </pre>
     */
    public void doHandshake(String host, int port, String remoteIdHex) throws IOException {
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
        conn.writeMessage(new PingMessage());
        conn.writeMessage(new DisconnectMessage(ReasonCode.PEER_QUITING));
        conn.handleNextMessage();

        while (true) {
            try {
                conn.handleNextMessage();
            } catch (EOFException e) {
                break;
            }
        }


        this.secrets = initiator.getSecrets();
    }


    public Secrets getSecrets() {
        return secrets;
    }


    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }
}
