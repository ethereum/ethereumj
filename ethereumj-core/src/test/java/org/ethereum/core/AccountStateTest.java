package org.ethereum.core;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class AccountStateTest {

	@Test
	public void testGetEncoded() {
		String expected = "de809a01000000000000000000000000000000000000000000000000008080";
		AccountState acct = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		assertEquals(expected, Hex.toHexString(acct.getEncoded()));
	}

}
