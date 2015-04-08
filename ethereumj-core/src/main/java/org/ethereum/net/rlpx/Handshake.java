package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;

import java.security.SecureRandom;

import static org.ethereum.util.ByteUtil.merge;

/**
 * Created by devrandom on 2015-04-08.
 */
public class Handshake {
    boolean isInitiator;
    ECKey ephemeralKey;
    ECPoint remotePublicKey;
    ECPoint remoteEphemeralKey;
    byte[] initiatorNonce;
    byte[] responderNonce;

    public Handshake(ECPoint remotePublicKey) {
        this.remotePublicKey = remotePublicKey;
        SecureRandom random = new SecureRandom();
        ephemeralKey = new ECKey(random);
        initiatorNonce = new byte[32];
        random.nextBytes(initiatorNonce);
        isInitiator = true;
    }

    public AuthInitiateMessage createAuthInitiate(byte[] token) {
        if (token == null) {
            token = new byte[32]; // TODO shared secret
        }
        byte[] signed = new byte[32];
        for (int i = 0; i < 32; i++) {
            signed[i] = (byte) (token[i] ^ initiatorNonce[i]);
        }
        ECKey.ECDSASignature signature = ephemeralKey.sign(signed);
        AuthInitiateMessage message = new AuthInitiateMessage();
        message.signature =
                merge(BigIntegers.asUnsignedByteArray(signature.r),
                        BigIntegers.asUnsignedByteArray(signature.s), new byte[]{signature.v});
        // TODO
        return message;
    }
}
