package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.SHA3Helper;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;

import javax.annotation.Nullable;
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

    /**
     * Create a handshake auth message
     *
     * @param token previous token if we had a previous session
     * @param key our private key
     */
    public AuthInitiateMessage createAuthInitiate(@Nullable byte[] token, ECKey key) {
        boolean isToken;
        if (token == null) {
            isToken = false;
            token = new byte[32]; // TODO shared secret
        } else {
            isToken = true;
        }
        byte[] signed = new byte[32];
        for (int i = 0; i < 32; i++) {
            signed[i] = (byte) (token[i] ^ initiatorNonce[i]);
        }
        AuthInitiateMessage message = new AuthInitiateMessage();
        message.signature = ephemeralKey.sign(signed);
        message.isTokenUsed = isToken;
        message.ephemeralPublicHash = SHA3Helper.sha3(ephemeralKey.getPubKeyPoint().getEncoded(false), 1, 32);
        message.publicKey = key.getPubKeyPoint();
        return message;
    }
}
