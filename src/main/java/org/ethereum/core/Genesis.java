package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class Genesis extends Block {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static byte[] zeroHash256 = new byte[32];
	private static byte[] zeroHash160 = new byte[20];
	private static byte[] sha3EmptyList = HashUtil.sha3(RLP.encodeList());

	public static byte[] PARENT_HASH = zeroHash256;
	public static byte[] UNCLES_HASH = sha3EmptyList;
	public static byte[] COINBASE = zeroHash160;
    public static byte[] TX_TRIE_ROOT = new byte[0];
    public static byte[] DIFFICULTY = BigInteger.valueOf(2).pow(22).toByteArray();
    public static long NUMBER = 0;
    public static long MIN_GAS_PRICE = 0;
    public static long GAS_LIMIT = 1000000;
    public static long GAS_USED = 0;
    public static long TIMESTAMP = 0;
    public static byte[] EXTRA_DATA = new byte[0];
    public static byte[] NONCE = HashUtil.sha3(new byte[]{42});
			
	public Genesis() {
		super(PARENT_HASH, UNCLES_HASH, COINBASE, TX_TRIE_ROOT, DIFFICULTY,
				NUMBER, MIN_GAS_PRICE, GAS_LIMIT, GAS_USED, TIMESTAMP,
				EXTRA_DATA, NONCE, null, null);
		// Premine state
		AccountState acct = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
		// # (M)
		this.updateState(Hex.decode("2ef47100e0787b915105fd5e3f4ff6752079d5cb"), acct.getEncoded());
		// # (A)
		this.updateState(Hex.decode("1a26338f0d905e295fccb71fa9ea849ffa12aaf4"), acct.getEncoded());
		// # (J)
		this.updateState(Hex.decode("e6716f9544a56c530d868e4bfbacb172315bdead"), acct.getEncoded());
		// # (G)
		this.updateState(Hex.decode("8a40bfaa73256b60764c1bf40675a99083efb075"), acct.getEncoded());
		// # (CH)
		this.updateState(Hex.decode("e4157b34ea9615cfbde6b4fda419828124b70c78"), acct.getEncoded());		
		// # (V)
		this.updateState(Hex.decode("1e12515ce3e0f817a4ddef9ca55788a1d66bd2df"), acct.getEncoded());
		// # (HH)
		this.updateState(Hex.decode("6c386a4b26f73c802f34673f7248bb118f97424a"), acct.getEncoded());
		// # (R)
		this.updateState(Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826"), acct.getEncoded());
		System.out.println(Hex.toHexString(this.getStateRoot()));
		logger.info("Genesis-hash: " + Hex.toHexString(this.getHash()));
	}
}
