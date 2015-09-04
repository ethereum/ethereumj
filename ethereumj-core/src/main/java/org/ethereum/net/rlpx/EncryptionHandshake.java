package org.ethereum.net.rlpx;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.math.ec.ECPoint;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import static org.ethereum.crypto.SHA3Helper.sha3;

/**
 * Created by devrandom on 2015-04-08.
 */
public class EncryptionHandshake {
    public static final int NONCE_SIZE = 32;
    public static final int MAC_SIZE = 256;
    public static final int SECRET_SIZE = 32;
    private SecureRandom random = new SecureRandom();
    private boolean isInitiator;
    private ECKey ephemeralKey;
    private ECPoint remotePublicKey;
    private ECPoint remoteEphemeralKey;
    private byte[] initiatorNonce;
    private byte[] responderNonce;
    private Secrets secrets;

    public EncryptionHandshake(ECPoint remotePublicKey) {
        this.remotePublicKey = remotePublicKey;
        ephemeralKey = new ECKey(random);
        initiatorNonce = new byte[NONCE_SIZE];
        random.nextBytes(initiatorNonce);
        isInitiator = true;
    }

    public EncryptionHandshake() {
        ephemeralKey = new ECKey(random);
        responderNonce = new byte[NONCE_SIZE];
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
            token = ByteUtil.bigIntegerToBytes(secretScalar, NONCE_SIZE);
        } else {
            isToken = true;
        }

        byte[] nonce = initiatorNonce;
        byte[] signed = xor(token, nonce);
        message.signature = ephemeralKey.sign(signed);
        message.isTokenUsed = isToken;
        message.ephemeralPublicHash = sha3(ephemeralKey.getPubKeyPoint().getEncoded(false), 1, 64);
        message.publicKey = key.getPubKeyPoint();
        message.nonce = initiatorNonce;
        return message;
    }

    private static byte[] xor(byte[] b1, byte[] b2) {
        Preconditions.checkArgument(b1.length == b2.length);
        byte[] out = new byte[b1.length];
        for (int i = 0; i < b1.length; i++) {
            out[i] = (byte) (b1[i] ^ b2[i]);
        }
        return out;
    }

    public byte[] encryptAuthMessage(AuthInitiateMessage message) {
        return ECIESCoder.encrypt(remotePublicKey, message.encode());
    }

    public byte[] encryptAuthReponse(AuthResponseMessage message) {
        return ECIESCoder.encrypt(remotePublicKey, message.encode());
    }

    public AuthResponseMessage decryptAuthResponse(byte[] ciphertext, ECKey myKey) {
        try {
            byte[] plaintext = ECIESCoder.decrypt(myKey.getPrivKey(), ciphertext);
            return AuthResponseMessage.decode(plaintext);
        } catch (IOException | InvalidCipherTextException e) {
            throw Throwables.propagate(e);
        }
    }

    public AuthInitiateMessage decryptAuthInitiate(byte[] ciphertext, ECKey myKey) throws InvalidCipherTextException {
        try {
            byte[] plaintext = ECIESCoder.decrypt(myKey.getPrivKey(), ciphertext);
            return AuthInitiateMessage.decode(plaintext);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public AuthResponseMessage handleAuthResponse(ECKey myKey, byte[] initiatePacket, byte[] responsePacket) {
        AuthResponseMessage response = decryptAuthResponse(responsePacket, myKey);
        remoteEphemeralKey = response.ephemeralPublicKey;
        responderNonce = response.nonce;
        agreeSecret(initiatePacket, responsePacket);
        return response;
    }

    void agreeSecret(byte[] initiatePacket, byte[] responsePacket) {
        BigInteger secretScalar = remoteEphemeralKey.multiply(ephemeralKey.getPrivKey()).normalize().getXCoord().toBigInteger();
        byte[] agreedSecret = ByteUtil.bigIntegerToBytes(secretScalar, SECRET_SIZE);
        byte[] sharedSecret = sha3(agreedSecret, sha3(responderNonce, initiatorNonce));
        byte[] aesSecret = sha3(agreedSecret, sharedSecret);
        secrets = new Secrets();
        secrets.aes = aesSecret;
        secrets.mac = sha3(agreedSecret, aesSecret);
        secrets.token = sha3(sharedSecret);
//        System.out.println("mac " + Hex.toHexString(secrets.mac));
//        System.out.println("aes " + Hex.toHexString(secrets.aes));
//        System.out.println("shared " + Hex.toHexString(sharedSecret));
//        System.out.println("ecdhe " + Hex.toHexString(agreedSecret));

        SHA3Digest mac1 = new SHA3Digest(MAC_SIZE);
        mac1.update(xor(secrets.mac, responderNonce), 0, secrets.mac.length);
        byte[] buf = new byte[32];
        new SHA3Digest(mac1).doFinal(buf, 0);
        mac1.update(initiatePacket, 0, initiatePacket.length);
        new SHA3Digest(mac1).doFinal(buf, 0);
        SHA3Digest mac2 = new SHA3Digest(MAC_SIZE);
        mac2.update(xor(secrets.mac, initiatorNonce), 0, secrets.mac.length);
        new SHA3Digest(mac2).doFinal(buf, 0);
        mac2.update(responsePacket, 0, responsePacket.length);
        new SHA3Digest(mac2).doFinal(buf, 0);
        if (isInitiator) {
            secrets.egressMac = mac1;
            secrets.ingressMac = mac2;
        } else {
            secrets.egressMac = mac2;
            secrets.ingressMac = mac1;
        }
    }

    public byte[] handleAuthInitiate(byte[] initiatePacket, ECKey key) throws InvalidCipherTextException {
        AuthResponseMessage response = makeAuthInitiate(initiatePacket, key);
        byte[] responsePacket = encryptAuthReponse(response);
        agreeSecret(initiatePacket, responsePacket);
        return responsePacket;
    }

    AuthResponseMessage makeAuthInitiate(byte[] initiatePacket, ECKey key) throws InvalidCipherTextException {
        AuthInitiateMessage initiate = decryptAuthInitiate(initiatePacket, key);
        return makeAuthInitiate(initiate, key);
    }

    AuthResponseMessage makeAuthInitiate(AuthInitiateMessage initiate, ECKey key) {
        initiatorNonce = initiate.nonce;
        remotePublicKey = initiate.publicKey;
        BigInteger secretScalar = remotePublicKey.multiply(key.getPrivKey()).normalize().getXCoord().toBigInteger();
        byte[] token = ByteUtil.bigIntegerToBytes(secretScalar, NONCE_SIZE);
        byte[] signed = xor(token, initiatorNonce);

        ECKey ephemeral = ECKey.recoverFromSignature(recIdFromSignatureV(initiate.signature.v),
                initiate.signature, signed, false);
        if (ephemeral == null) {
            throw new RuntimeException("failed to recover signatue from message");
        }
        remoteEphemeralKey = ephemeral.getPubKeyPoint();
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
        SHA3Digest egressMac;
        SHA3Digest ingressMac;

        public byte[] getAes() {
            return aes;
        }

        public byte[] getMac() {
            return mac;
        }

        public byte[] getToken() {
            return token;
        }

        public SHA3Digest getIngressMac() {
            return ingressMac;
        }

        public SHA3Digest getEgressMac() {
            return egressMac;
        }
    }

    public boolean isInitiator() {
        return isInitiator;
    }
}
