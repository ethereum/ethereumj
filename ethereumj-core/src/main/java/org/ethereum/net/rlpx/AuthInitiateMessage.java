package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;

import static org.ethereum.util.ByteUtil.merge;

/**
 * Authentication initiation message, to be wrapped inside {@link Encrypter}.
 *
 * Created by devrandom on 2015-04-07.
 */
public class AuthInitiateMessage {
    ECKey.ECDSASignature signature; // 65 bytes
    byte[] ephemeralPublicHash; // 32 bytes
    ECPoint publicKey; // 64 bytes - uncompressed and no type byte
    byte[] nonce; // 32 bytes
    boolean isTokenUsed; // 1 byte - 0x00 or 0x01

    public AuthInitiateMessage() {
    }

    public static int getLength() {
        return 65+32+64+32+1;
    }

    static AuthInitiateMessage decode(byte[] wire) {
        AuthInitiateMessage message = new AuthInitiateMessage();
        int offset = 0;
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(wire, offset, r, 0, 32);
        offset += 32;
        System.arraycopy(wire, offset, s, 0, 32);
        offset += 32;
        int v = wire[offset] + 27;
        offset += 1;
        message.signature = ECKey.ECDSASignature.fromComponents(r, s, (byte)v);
        message.ephemeralPublicHash = new byte[32];
        System.arraycopy(wire, offset, message.ephemeralPublicHash, 0, 32);
        offset += 32;
        byte[] bytes = new byte[65];
        System.arraycopy(wire, offset, bytes, 1, 64);
        offset += 64;
        bytes[0] = 0x04; // uncompressed
        message.publicKey = ECKey.CURVE.getCurve().decodePoint(bytes);
        message.nonce = new byte[32];
        System.arraycopy(wire, offset, message.nonce, 0, 32);
        offset += message.nonce.length;
        byte tokenUsed = wire[offset];
        offset += 1;
        if (tokenUsed != 0x00 && tokenUsed != 0x01)
            throw new RuntimeException("invalid boolean"); // TODO specific exception
        message.isTokenUsed = (tokenUsed == 0x01);
        return message;
    }

    public byte[] encode() {
        // FIXME does this code generate a constant length for each item?
        byte[] sigBytes = merge(BigIntegers.asUnsignedByteArray(signature.r),
                BigIntegers.asUnsignedByteArray(signature.s), new byte[]{EncryptionHandshake.recIdFromSignatureV(signature.v)});

        byte[] buffer = new byte[getLength()];
        int offset = 0;
        System.arraycopy(sigBytes, 0, buffer, offset, sigBytes.length);
        offset += sigBytes.length;
        System.arraycopy(ephemeralPublicHash, 0, buffer, offset, ephemeralPublicHash.length);
        offset += ephemeralPublicHash.length;
        byte[] publicBytes = publicKey.getEncoded(false);
        System.arraycopy(publicBytes, 1, buffer, offset, publicBytes.length - 1);
        offset += publicBytes.length - 1;
        System.arraycopy(nonce, 0, buffer, offset, nonce.length);
        offset += nonce.length;
        buffer[offset] = (byte)(isTokenUsed ? 0x01 : 0x00);
        offset += 1;
        return buffer;
    }
}
