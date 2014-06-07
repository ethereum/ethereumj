package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class Genesis extends Block {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AccountState acct = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
	private String[] premine = new String[] {
			"2ef47100e0787b915105fd5e3f4ff6752079d5cb", 	// # (M)
			"1a26338f0d905e295fccb71fa9ea849ffa12aaf4",		// # (A)
			"e6716f9544a56c530d868e4bfbacb172315bdead",		// # (J)
			"8a40bfaa73256b60764c1bf40675a99083efb075",		// # (G)
			"e4157b34ea9615cfbde6b4fda419828124b70c78",		// # (CH)
			"1e12515ce3e0f817a4ddef9ca55788a1d66bd2df",		// # (V)
			"6c386a4b26f73c802f34673f7248bb118f97424a",		// # (HH)
			"cd2a3d9f938e13cd947ec05abc7fe734df8dd826" };	// # (R)
	
	private static byte[] zeroHash256 = new byte[32];
	private static byte[] zeroHash160 = new byte[20];
	private static byte[] sha3EmptyList = HashUtil.sha3(RLP.encodeList());

	public static byte[] PARENT_HASH = zeroHash256;
	public static byte[] UNCLES_HASH = sha3EmptyList;
	public static byte[] COINBASE = zeroHash160;
    public static byte[] DIFFICULTY = BigInteger.valueOf(2).pow(22).toByteArray();
    public static long NUMBER = 0;
    public static long MIN_GAS_PRICE = 0;
    public static long GAS_LIMIT = 1000000;
    public static long GAS_USED = 0;
    public static long TIMESTAMP = 0;
    public static byte[] EXTRA_DATA = new byte[0];
    public static byte[] NONCE = HashUtil.sha3(new byte[]{42});
    
    private static Block instance;
    
	private Genesis() {
		super(PARENT_HASH, UNCLES_HASH, COINBASE, DIFFICULTY,
				NUMBER, MIN_GAS_PRICE, GAS_LIMIT, GAS_USED, TIMESTAMP,
				EXTRA_DATA, NONCE, null, null);

		// Premine state
		for (String address : premine) {
			this.updateState(Hex.decode(address), acct.getEncoded());
		}
		logger.info("Genesis-hash: " + Hex.toHexString(this.getHash()));
		logger.info("Genesis-stateRoot: " + Hex.toHexString(this.getStateRoot()));

        WorldManager.instance.chainDB.put(getParentHash(), getEncoded());
    }
	
	public static Block getInstance() {
		if (instance == null) {
			instance = new Genesis();
		}
		return instance;
	}
}
