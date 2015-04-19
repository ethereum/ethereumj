package org.ethereum.core;

import java.math.BigInteger;

public class PremineRaw {

    byte[] addr;
    BigInteger value;
    Denomination denomination;

    public PremineRaw(byte[] addr, BigInteger value, Denomination denomination) {
        this.addr = addr;
        this.value = value;
        this.denomination = denomination;
    }

    public byte[] getAddr() {
        return addr;
    }

    public BigInteger getValue() {
        return value;
    }

    public Denomination getDenomination() {
        return denomination;
    }
}
