package org.ethereum.core;

import java.math.BigInteger;

public enum Denomination {

    WEI(newBigInt(0)),
    SZABO(newBigInt(12)),
    FINNY(newBigInt(15)),
    ETHER(newBigInt(18));

    private BigInteger amount;

    private Denomination(BigInteger value) {
        this.amount = value;
    }

    public BigInteger value() {
        return amount;
    }

    public long longValue() {
        return value().longValue();
    }

    private static BigInteger newBigInt(int value) {
        return BigInteger.valueOf(10).pow(value);
    }

    public static String toFriendlyString(BigInteger value) {
        if (value.compareTo(ETHER.value()) == 1 || value.compareTo(ETHER.value()) == 0) {
            return Float.toString(value.divide(ETHER.value()).floatValue()) +  " ETHER";
        }
        else if(value.compareTo(FINNY.value()) == 1 || value.compareTo(FINNY.value()) == 0) {
            return Float.toString(value.divide(FINNY.value()).floatValue()) +  " FINNY";
        }
        else if(value.compareTo(SZABO.value()) == 1 || value.compareTo(SZABO.value()) == 0) {
            return Float.toString(value.divide(SZABO.value()).floatValue()) +  " SZABO";
        }
        else
            return Float.toString(value.divide(WEI.value()).floatValue()) +  " WEI";
    }
}
