package org.ethereum.core;

import java.math.BigDecimal;
import java.math.BigInteger;

public enum Denomination {

	WEI(BigInteger.ONE),
	SZABO(BigDecimal.valueOf(Math.pow(10, 12)).toBigInteger()),
	FINNY(BigDecimal.valueOf(Math.pow(10, 15)).toBigInteger()),
	ETHER(BigDecimal.valueOf(Math.pow(10, 18)).toBigInteger());
	
	private BigInteger amount;
	
	private Denomination(BigInteger value) {
		this.amount = value;
	}
	
	public BigInteger getDenomination() {
		return amount;
	}
}
