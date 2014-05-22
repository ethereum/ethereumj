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
    		Hex.decode("12582945fc5ad12c3e7b67c4fc37a68fc0d52d995bb7f7291ff41a2739a7ca16");
    private static byte[] txTrieRoot = new byte[0];
    private static byte[] difficulty = BigInteger.valueOf(2).pow(22).toByteArray();
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
