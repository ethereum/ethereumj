/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.util.ByteUtil.merge;
import static org.spongycastle.util.BigIntegers.asUnsignedByteArray;

/**
 * Auth Initiate message defined by EIP-8
 *
 * @author mkalinin
 * @since 17.02.2016
 */
public class AuthInitiateMessageV4 {

    ECKey.ECDSASignature signature; // 65 bytes
    ECPoint publicKey; // 64 bytes - uncompressed and no type byte
    byte[] nonce; // 32 bytes
    int version = 4; // 4 bytes

    public AuthInitiateMessageV4() {
    }

    static AuthInitiateMessageV4 decode(byte[] wire) {
        AuthInitiateMessageV4 message = new AuthInitiateMessageV4();

        RLPList params = (RLPList) RLP.decode2OneItem(wire, 0);

        byte[] signatureBytes = params.get(0).getRLPData();
        int offset = 0;
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(signatureBytes, offset, r, 0, 32);
        offset += 32;
        System.arraycopy(signatureBytes, offset, s, 0, 32);
        offset += 32;
        int v = signatureBytes[offset] + 27;
        message.signature = ECKey.ECDSASignature.fromComponents(r, s, (byte)v);

        byte[] publicKeyBytes = params.get(1).getRLPData();
        byte[] bytes = new byte[65];
        System.arraycopy(publicKeyBytes, 0, bytes, 1, 64);
        bytes[0] = 0x04; // uncompressed
        message.publicKey = ECKey.CURVE.getCurve().decodePoint(bytes);

        message.nonce = params.get(2).getRLPData();

        byte[] versionBytes = params.get(3).getRLPData();
        message.version = ByteUtil.byteArrayToInt(versionBytes);

        return message;
    }

    public byte[] encode() {

        byte[] rsigPad = new byte[32];
        byte[] rsig = asUnsignedByteArray(signature.r);
        System.arraycopy(rsig, 0, rsigPad, rsigPad.length - rsig.length, rsig.length);

        byte[] ssigPad = new byte[32];
        byte[] ssig = asUnsignedByteArray(signature.s);
        System.arraycopy(ssig, 0, ssigPad, ssigPad.length - ssig.length, ssig.length);

        byte[] publicKey = new byte[64];
        System.arraycopy(this.publicKey.getEncoded(false), 1, publicKey, 0, publicKey.length);

        byte[] sigBytes = RLP.encode(merge(rsigPad, ssigPad, new byte[]{EncryptionHandshake.recIdFromSignatureV(signature.v)}));
        byte[] publicBytes = RLP.encode(publicKey);
        byte[] nonceBytes = RLP.encode(nonce);
        byte[] versionBytes = RLP.encodeInt(version);

        return RLP.encodeList(sigBytes, publicBytes, nonceBytes, versionBytes);
    }

    @Override
    public String toString() {

        byte[] sigBytes = merge(asUnsignedByteArray(signature.r),
                asUnsignedByteArray(signature.s), new byte[]{EncryptionHandshake.recIdFromSignatureV(signature.v)});

        return "AuthInitiateMessage{" +
                "\n  sigBytes=" + Hex.toHexString(sigBytes) +
                "\n  publicKey=" + Hex.toHexString(publicKey.getEncoded(false)) +
                "\n  nonce=" + Hex.toHexString(nonce) +
                "\n  version=" + version +
                "\n}";
    }
}
