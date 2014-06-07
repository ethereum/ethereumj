package org.ethereum.core;

import java.math.BigInteger;

public enum Denomination {

	WEI(newBigInt(0)),
	ADA(newBigInt(3)),
	BABBAGE(newBigInt(6)),
	SHANNON(newBigInt(9)),
	SZABO(newBigInt(12)),
	FINNY(newBigInt(15)),
	ETHER(newBigInt(18));
	
	private BigInteger amount;
	
	private Denomination(BigInteger value) {
		this.amount = value;
	}

	public BigInteger getDenomination() {
		return amount;
	}
    public long longValue() {return getDenomination().longValue();}

	private static BigInteger newBigInt(int value) {
		return BigInteger.valueOf(10).pow(value);
	}
}
