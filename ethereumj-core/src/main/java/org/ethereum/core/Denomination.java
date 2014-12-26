package org.ethereum.core;

import java.math.BigInteger;

public enum Denomination {

    WEI(newBigInt(0)),
    ADA(newBigInt(3)),
    BABBAGE(newBigInt(6)),
    SHANNON(newBigInt(9)),
    SZABO(newBigInt(12)),
    FINNY(newBigInt(15)),
    ETHER(newBigInt(18)),
    EINSTEIN(newBigInt(21)),
    DOUGLAS(newBigInt(42));                  
             
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
        if(value.compareTo(DOUGLAS.value()) == 1 || value.compareTo(DOUGLAS.value()) == 0) {
            return Float.toString(value.divide(DOUGLAS.value()).floatValue()) +  " DOUGLAS";
        }
        else if(value.compareTo(EINSTEIN.value()) == 1 || value.compareTo(EINSTEIN.value()) == 0) {
            return Float.toString(value.divide(EINSTEIN.value()).floatValue()) +  " EINSTEIN";
        }
        else if(value.compareTo(ETHER.value()) == 1 || value.compareTo(ETHER.value()) == 0) {
            return Float.toString(value.divide(ETHER.value()).floatValue()) +  " ETHER";
        }
        else if(value.compareTo(FINNY.value()) == 1 || value.compareTo(FINNY.value()) == 0) {
            return Float.toString(value.divide(FINNY.value()).floatValue()) +  " FINNY";
        }
        else if(value.compareTo(SZABO.value()) == 1 || value.compareTo(SZABO.value()) == 0) {
            return Float.toString(value.divide(SZABO.value()).floatValue()) +  " SZABO";
        }
        else if(value.compareTo(SHANNON.value()) == 1 || value.compareTo(SHANNON.value()) == 0) {
            return Float.toString(value.divide(SHANNON.value()).floatValue()) +  " SHANNON";
        }
        else if(value.compareTo(BABBAGE.value()) == 1 || value.compareTo(BABBAGE.value()) == 0) {
            return Float.toString(value.divide(BABBAGE.value()).floatValue()) +  " BABBAGE";
        }
        else if(value.compareTo(ADA.value()) == 1 || value.compareTo(ADA.value()) == 0) {
            return Float.toString(value.divide(ADA.value()).floatValue()) +  " ADA";
        }
        else
            return Float.toString(value.divide(WEI.value()).floatValue()) +  " WEI";
    }
}
