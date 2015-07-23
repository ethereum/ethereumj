package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * Authentication response message, to be wrapped inside
 *
 * Created by devrandom on 2015-04-07.
 */
public class AuthResponseMessage {
    ECPoint ephemeralPublicKey; // 64 bytes - uncompressed and no type byte
    byte[] nonce; // 32 bytes
    boolean isTokenUsed; // 1 byte - 0x00 or 0x01

    static AuthResponseMessage decode(byte[] wire) {
        int offset = 0;
        AuthResponseMessage message = new AuthResponseMessage();
        byte[] bytes = new byte[65];
        System.arraycopy(wire, offset, bytes, 1, 64);
        offset += 64;
        bytes[0] = 0x04; // uncompressed
        message.ephemeralPublicKey = ECKey.CURVE.getCurve().decodePoint(bytes);
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

    public static int getLength() {
        return 64+32+1;
    }

    public byte[] encode() {
        byte[] buffer = new byte[getLength()];
        int offset = 0;
        byte[] publicBytes = ephemeralPublicKey.getEncoded(false);
        System.arraycopy(publicBytes, 1, buffer, offset, publicBytes.length - 1);
        offset += publicBytes.length - 1;
        System.arraycopy(nonce, 0, buffer, offset, nonce.length);
        offset += nonce.length;
        buffer[offset] = (byte)(isTokenUsed ? 0x01 : 0x00);
        offset += 1;
        return buffer;
    }

    @Override
    public String toString() {
        return "AuthResponseMessage{" +
                "\n  ephemeralPublicKey=" + ephemeralPublicKey +
                "\n  nonce=" + Hex.toHexString(nonce) +
                "\n  isTokenUsed=" + isTokenUsed +
                '}';
    }
}
