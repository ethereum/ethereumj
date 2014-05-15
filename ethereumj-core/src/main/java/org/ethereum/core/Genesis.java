package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

public class Genesis extends Block {

	private static byte[] zeroHash256 = new byte[32];
	private static byte[] zeroHash160 = new byte[20];
	private static byte[] sha3EmptyList = HashUtil.sha3(RLP.encodeList());

	private static byte[] parentHash = zeroHash256;
    private static byte[] unclesHash = sha3EmptyList;
    private static byte[] coinbase = zeroHash160;
    private static byte[] stateRoot = // TODO: Get stateRoot from actual state
    		Hex.decode("2f4399b08efe68945c1cf90ffe85bbe3ce978959da753f9e649f034015b8817d");
    private static byte[] txTrieRoot = zeroHash256;
    private static byte[] difficulty = BigInteger.valueOf((long) Math.pow(2, 22)).toByteArray();
    private static long number = 0;
    private static long minGasPrice = 0;
    private static long gasLimit = 1000000;
    private static long gasUsed = 0;
    private static long timestamp = 0;
    private static byte[] extraData = new byte[0];
    private static byte[] nonce = HashUtil.sha3(new byte[]{42});
			
	public Genesis() {
		super(parentHash, unclesHash, coinbase, stateRoot,
				txTrieRoot, difficulty, number, minGasPrice, gasLimit, gasUsed,
				timestamp, extraData, nonce, null, null);
	}
}
