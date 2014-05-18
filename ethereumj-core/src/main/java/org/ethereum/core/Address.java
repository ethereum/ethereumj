package org.ethereum.core;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 19:10
 */
public class Address {

    byte[] privKey;
    byte[] pubKey;

    public Address(){
        privKey = new BigInteger(130, Utils.getRandom()).toString(32).getBytes();
        this.pubKey = ECKey.fromPrivate(privKey).getAddress();
    }

    public Address(byte[] privKey) {
        this.privKey = privKey;
        this.pubKey = ECKey.fromPrivate(privKey).getAddress();
    }

    public Address(byte[] privKey, byte[] pubKey) {
        this.privKey = privKey;
        this.pubKey = pubKey;
    }

    public byte[] getPrivKey() {
        return privKey;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    @Override
    public String toString() {
        return Hex.toHexString(pubKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (!Arrays.equals(pubKey, address.pubKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pubKey);
    }
}
