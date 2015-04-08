package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;

import java.security.SecureRandom;

import static org.ethereum.util.ByteUtil.merge;

/**
 * Authentication initiation message, to be wrapped inside {@link EncryptedMessage}.
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
        nonce = new byte[32];
        new SecureRandom().nextBytes(nonce);
    }

    public int getLength() {
        return 65+32+64+32+1;
    }

    public int encode(byte[] buffer, int offset) {
        // FIXME does this code generate a constant length for each item?
        byte[] sigBytes = merge(BigIntegers.asUnsignedByteArray(signature.r),
                BigIntegers.asUnsignedByteArray(signature.s), new byte[]{signature.v});

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
        return offset;
    }
}
