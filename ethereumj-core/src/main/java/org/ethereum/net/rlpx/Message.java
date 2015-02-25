package org.ethereum.net.rlpx;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.merge;

public class Message {

    byte[] mdc;
    byte[] signature;
    byte[] type;
    byte[] data;

    public static Message decode(byte[] wire) {

        if (wire.length < 98) throw new Error("Bad message");

        byte[] mdc = new byte[32];
        System.arraycopy(wire, 0, mdc, 0, 32);

        byte[] signature = new byte[65];
        System.arraycopy(wire, 32, signature, 0, 65);

        byte[] type = new byte[1];
        type[0] = wire[97];

        byte[] data = new byte[wire.length - 98];
        System.arraycopy(wire, 98, data, 0, data.length);

        byte[] mdcCheck = sha3(wire, 32, wire.length - 32);

        int check = FastByteComparisons.compareTo(mdc, 0, mdc.length, mdcCheck, 0, mdcCheck.length);

        if (check != 0) throw new Error("MDC check failed");

        Message msg;
        if (type[0] == 1) msg = new PingMessage();
        else if (type[0] == 2) msg = new PongMessage();
        else if (type[0] == 3) msg = new FindNodeMessage();
        else if (type[0] == 4) msg = new NeighborsMessage();
        else throw new Error("Unknown RLPx message");

        msg.mdc = mdc;
        msg.signature = signature;
        msg.type = type;
        msg.data = data;

        return msg;
    }


    public Message encode(byte[] type, byte[] data) {

        /* [1] Calc sha3 - prepare for sig */
        byte[] payload = new byte[type.length + data.length];
        payload[0] = type[0];
        System.arraycopy(data, 0, payload, 1, data.length);
        byte[] forSig = sha3(payload);

        /* [2] Crate signature*/
        ECKey privKey = ECKey.fromPrivate(Hex.decode("3ecb44df2159c26e0f995712d4f39b6f6e499b40749b1cf1246c37f9516cb6a4"));
        ECKey.ECDSASignature signature = privKey.sign(forSig);

        byte[] sigBytes =
                merge(new byte[]{signature.v}, BigIntegers.asUnsignedByteArray(signature.r),
                        BigIntegers.asUnsignedByteArray(signature.s));

        // [3] calculate MDC
        byte[] forSha = merge(sigBytes, type, data);
        byte[] mdc = sha3(forSha);

        // wrap all the data in to the packet
        this.mdc = mdc;
        this.signature = sigBytes;
        this.type = type;
        this.data = data;

        return this;
    }

    public byte[] getPacket() {
        byte[] packet = merge(mdc, signature, type, data);
        return packet;
    }

    public byte[] getMdc() {
        return mdc;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "{" +
                "mdc=" + Hex.toHexString(mdc) +
                ", signature=" + Hex.toHexString(signature) +
                ", type=" + Hex.toHexString(type) +
                ", data=" + Hex.toHexString(data) +
                '}';
    }
}
