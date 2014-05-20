package org.ethereum.core;

import org.ethereum.crypto.ECKey;
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
    byte[] address;

    public Address(){
        privKey = new BigInteger(130, Utils.getRandom()).toString(32).getBytes();
        this.address = ECKey.fromPrivate(privKey).getAddress();
    }

    public Address(byte[] privKey) {
        this.privKey = privKey;
        this.address = ECKey.fromPrivate(privKey).getAddress();
    }

    public Address(byte[] privKey, byte[] address) {
        this.privKey = privKey;
        this.address = address;
    }

    public byte[] getPrivKey() {
        return privKey;
    }

    public byte[] getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return Hex.toHexString(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (!Arrays.equals(this.address, address.address)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }
}
