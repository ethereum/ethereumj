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
    public static byte[] STATE_ROOT = // TODO: Get stateRoot from actual state
    		Hex.decode("12582945fc5ad12c3e7b67c4fc37a68fc0d52d995bb7f7291ff41a2739a7ca16");
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
		super(PARENT_HASH, UNCLES_HASH, COINBASE, STATE_ROOT,
				TX_TRIE_ROOT, DIFFICULTY, NUMBER, MIN_GAS_PRICE, GAS_LIMIT, GAS_USED,
				TIMESTAMP, EXTRA_DATA, NONCE, null, null);
		logger.info("Genesis-hash: " + Hex.toHexString(this.getHash()));
	}
}
