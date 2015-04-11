package org.ethereum.net.rlpx;

import com.google.common.base.Throwables;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.math.ec.ECPoint;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import static org.ethereum.crypto.SHA3Helper.sha3;

/**
 * Created by devrandom on 2015-04-08.
 */
public class Handshake {
    private SecureRandom random = new SecureRandom();
    private boolean isInitiator;
    private ECKey ephemeralKey;
    private ECPoint remotePublicKey;
    private ECPoint remoteEphemeralKey;
    private byte[] initiatorNonce;
    private byte[] responderNonce;
    private Secrets secrets;

    public Handshake(ECPoint remotePublicKey) {
        this.remotePublicKey = remotePublicKey;
        ephemeralKey = new ECKey(random);
        initiatorNonce = new byte[32];
        random.nextBytes(initiatorNonce);
        isInitiator = true;
    }

    public Handshake() {
        ephemeralKey = new ECKey(random);
        responderNonce = new byte[32];
        random.nextBytes(responderNonce);
        isInitiator = false;
    }

    /**
     * Create a handshake auth message
     *
     * @param token previous token if we had a previous session
     * @param key our private key
     */
    public AuthInitiateMessage createAuthInitiate(@Nullable byte[] token, ECKey key) {
        AuthInitiateMessage message = new AuthInitiateMessage();
        boolean isToken;
        if (token == null) {
            isToken = false;
            BigInteger secretScalar = remotePublicKey.multiply(key.getPrivKey()).normalize().getXCoord().toBigInteger();
            token = ByteUtil.bigIntegerToBytes(secretScalar, 32);
        } else {
            isToken = true;
        }
        byte[] signed = new byte[32];
        for (int i = 0; i < 32; i++) {
            signed[i] = (byte) (token[i] ^ initiatorNonce[i]);
        }
        message.signature = ephemeralKey.sign(signed);
        message.isTokenUsed = isToken;
        message.ephemeralPublicHash = sha3(ephemeralKey.getPubKeyPoint().getEncoded(false), 1, 32);
        message.publicKey = key.getPubKeyPoint();
        message.nonce = initiatorNonce;
        return message;
    }

    public byte[] encryptAuthMessage(AuthInitiateMessage message) {
        return ECIESCoder.encrypt(remotePublicKey, message.encode());
    }

    public AuthResponseMessage decryptAuthResponse(byte[] ciphertext, ECKey myKey) {
        try {
            byte[] plaintext = ECIESCoder.decrypt(myKey.getPrivKey(), ciphertext);
            return AuthResponseMessage.decode(plaintext);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } catch (InvalidCipherTextException e) {
            throw Throwables.propagate(e);
        }
    }

    public void handleAuthResponse(AuthResponseMessage response) {
        remoteEphemeralKey = response.ephemeralPublicKey;
        responderNonce = response.nonce;
        agreeSecret();
    }

    private void agreeSecret() {
        BigInteger secretScalar = remoteEphemeralKey.multiply(ephemeralKey.getPrivKey()).normalize().getXCoord().toBigInteger();
        byte[] agreedSecret = ByteUtil.bigIntegerToBytes(secretScalar, 32);
        byte[] sharedSecret = sha3(agreedSecret, sha3(responderNonce, initiatorNonce));
        byte[] aesSecret = sha3(agreedSecret, sharedSecret);
        secrets = new Secrets();
        secrets.aes = aesSecret;
        secrets.mac = sha3(sharedSecret, aesSecret);
        secrets.token = sha3(sharedSecret);
    }

    public AuthResponseMessage handleAuthInitiate(AuthInitiateMessage initiate, ECKey key) {
        initiatorNonce = initiate.nonce;
        remotePublicKey = initiate.publicKey;

        BigInteger secretScalar = remotePublicKey.multiply(key.getPrivKey()).normalize().getXCoord().toBigInteger();
        byte[] token = ByteUtil.bigIntegerToBytes(secretScalar, 32);
        byte[] signed = new byte[32];
        for (int i = 0; i < 32; i++) {
            signed[i] = (byte) (token[i] ^ initiatorNonce[i]);
        }

        remoteEphemeralKey = ECKey.recoverFromSignature(recIdFromSignatureV(initiate.signature.v),
                initiate.signature, signed, false).getPubKeyPoint();
        agreeSecret();
        AuthResponseMessage response = new AuthResponseMessage();
        response.isTokenUsed = initiate.isTokenUsed;
        response.ephemeralPublicKey = ephemeralKey.getPubKeyPoint();
        response.nonce = responderNonce;
        return response;
    }

    static public byte recIdFromSignatureV(int v) {
        if (v >= 31) {
            // compressed
            v -= 4;
        }
        return (byte)(v - 27);
    }

    public Secrets getSecrets() {
        return secrets;
    }

    public ECPoint getRemotePublicKey() {
        return remotePublicKey;
    }

    public static class Secrets {
        byte[] aes;
        byte[] mac;
        byte[] token;

        public byte[] getAes() {
            return aes;
        }

        public byte[] getMac() {
            return mac;
        }

        public byte[] getToken() {
            return token;
        }
    }
}
