package org.ethereum.net.rlpx;

import org.spongycastle.math.ec.ECPoint;

/**
 * Authentication initiation message, to be wrapped inside {@link EncryptedMessage}.
 *
 * Created by devrandom on 2015-04-07.
 */
public class AuthInitiateMessage {
    byte[] signature; // 65 bytes
    byte[] ephemeralPublicHash; // 32 bytes
    ECPoint publicKey; // 64 bytes - uncompressed and no type byte
    byte[] nonce; // 32 bytes
    boolean isTokenUsed; // 1 byte - 0x00 or 0x01

    public int getLength() {
        return 65+32+64+32+1;
    }

    public void encode(byte[] buffer, int offset) {
        System.arraycopy(signature, 0, buffer, offset, signature.length);
        offset += signature.length;
        System.arraycopy(ephemeralPublicHash, 0, buffer, offset, ephemeralPublicHash.length);
        offset += ephemeralPublicHash.length;
        byte[] publicBytes = publicKey.getEncoded(false);
        System.arraycopy(publicBytes, 1, buffer, offset, publicBytes.length - 1);
        offset += publicBytes.length;
        System.arraycopy(nonce, 0, buffer, offset, nonce.length);
        offset += nonce.length;
        buffer[offset] = (byte)(isTokenUsed ? 0x01 : 0x00);
        offset += 1;
    }
}
