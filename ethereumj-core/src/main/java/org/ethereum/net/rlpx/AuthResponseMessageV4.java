package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

/**
 * Auth Response message defined by EIP-8
 *
 * @author mkalinin
 * @since 17.02.2016
 */
public class AuthResponseMessageV4 {

    ECPoint ephemeralPublicKey; // 64 bytes - uncompressed and no type byte
    byte[] nonce; // 32 bytes
    int version = 4; // 4 bytes

    static AuthResponseMessageV4 decode(byte[] wire) {

        AuthResponseMessageV4 message = new AuthResponseMessageV4();

        RLPList params = (RLPList) RLP.decode2OneItem(wire, 0);

        byte[] pubKeyBytes = params.get(0).getRLPData();

        byte[] bytes = new byte[65];
        System.arraycopy(pubKeyBytes, 0, bytes, 1, 64);
        bytes[0] = 0x04; // uncompressed
        message.ephemeralPublicKey = ECKey.CURVE.getCurve().decodePoint(bytes);

        message.nonce = params.get(1).getRLPData();

        byte[] versionBytes = params.get(2).getRLPData();
        message.version = ByteUtil.byteArrayToInt(versionBytes);

        return message;
    }

    public byte[] encode() {

        byte[] publicKey = new byte[64];
        System.arraycopy(ephemeralPublicKey.getEncoded(false), 1, publicKey, 0, publicKey.length);

        byte[] publicBytes = RLP.encode(publicKey);
        byte[] nonceBytes = RLP.encode(nonce);
        byte[] versionBytes = RLP.encodeInt(version);

        return RLP.encodeList(publicBytes, nonceBytes, versionBytes);
    }

    @Override
    public String toString() {
        return "AuthResponseMessage{" +
                "\n  ephemeralPublicKey=" + ephemeralPublicKey +
                "\n  nonce=" + Hex.toHexString(nonce) +
                "\n  version=" + version +
                '}';
    }
}
