package org.ethereum.net.dht;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class PeerId {
    byte[] data;

    public PeerId(byte[] data) {
        this.data = data;
    }

    public PeerId() {
        HashUtil.randomPeerId();
    }

    public byte nextBit(String startPatern) {

        if (this.toBinaryString().startsWith(startPatern + "1"))
            return 1;
        else
            return 0;
    }

    public byte[] calcDistance(PeerId toPeerId) {

        BigInteger aPeer = new BigInteger(data);
        BigInteger bPeer = new BigInteger(toPeerId.data);

        BigInteger distance = aPeer.xor(bPeer);
        return BigIntegers.asUnsignedByteArray(distance);
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PeerId{" +
                "data=" + Hex.toHexString(data) +
                '}';
    }

    public String toBinaryString() {

        BigInteger bi = new BigInteger(1, data);
        String out = String.format("%512s", bi.toString(2));
        out = out.replace(' ', '0');

        return out;
    }

}
