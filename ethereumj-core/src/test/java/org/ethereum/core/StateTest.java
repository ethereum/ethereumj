package org.ethereum.core;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.ethereum.trie.MockDB;
import org.ethereum.trie.Trie;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class StateTest {

	@Test
	public void testGenesisAccounts() {
		Trie trie = new Trie(new MockDB());

		// 2ef47100e0787b915105fd5e3f4ff6752079d5cb # (M)
		AccountState acct5 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("2ef47100e0787b915105fd5e3f4ff6752079d5cb"), acct5.getEncoded());
	
		// 1a26338f0d905e295fccb71fa9ea849ffa12aaf4 # (A)
		AccountState acct4 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("1a26338f0d905e295fccb71fa9ea849ffa12aaf4"), acct4.getEncoded());
		
		// e6716f9544a56c530d868e4bfbacb172315bdead # (J)
		AccountState acct2 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("e6716f9544a56c530d868e4bfbacb172315bdead"), acct2.getEncoded());
		
		// 8a40bfaa73256b60764c1bf40675a99083efb075 # (G)
		AccountState acct1 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("8a40bfaa73256b60764c1bf40675a99083efb075"), acct1.getEncoded());
		
		// e4157b34ea9615cfbde6b4fda419828124b70c78 # (CH)
		AccountState acct8 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("e4157b34ea9615cfbde6b4fda419828124b70c78"), acct8.getEncoded());
		
		// 1e12515ce3e0f817a4ddef9ca55788a1d66bd2df # (V)
		AccountState acct3 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("1e12515ce3e0f817a4ddef9ca55788a1d66bd2df"), acct3.getEncoded());
		
		// 6c386a4b26f73c802f34673f7248bb118f97424a # (HH)
		AccountState acct7 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("6c386a4b26f73c802f34673f7248bb118f97424a"), acct7.getEncoded());
		
		// cd2a3d9f938e13cd947ec05abc7fe734df8dd826 # (R)
		AccountState acct6 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		trie.update(Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826"), acct6.getEncoded());
		
		assertEquals("23b503734ff34ddb7bd5e478f1645680ec778ab3f90007cb1c854653693e5adc", Hex.toHexString(trie.getRootHash()));
	}

}
