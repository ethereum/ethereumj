package org.ethereum.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

public class MinerTest {

	// Example block#32 from Poc5 chain - rlpEncoded without nonce
	private String rlpWithoutNonce = "f894f890a00a312c2b0a8f125c60a3976b6e508e740e095eb59943988d9bbfb8"
			+ "aa43922e31a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794e559de5527492bcb42ec68d07df0742a98ec3f1ea050188ab86bdf164ac90eb2835a04a8930aae5393c3a2ef1166fb95028f9456b880833ee248208609184e72a000830eca0080845387fd2080c0c0";
	
	@Test
	public void testMine() {
		boolean miningTestEnabled = false;
		
		if(miningTestEnabled) {
			Block block = createBlock();
			assertEquals(rlpWithoutNonce, Hex.toHexString(block.getEncodedWithoutNonce()));
			System.out.println("Searching for nonce of following block: \n" + block.toString());
			
			Miner miner = new Miner();
			boolean mined = miner.mine(block, block.getDifficulty());
			assertTrue(mined);
			boolean valid = block.validateNonce();
			assertTrue(valid);
			
			// expectedHash is the actual hash from block#32 in PoC5 chain based on nonce below
			String expectedHash = "ce7201f6cc5eb1a6f35c7dda8acda111647a0f8a5bf0518e46579b03e29fe14b";
			assertEquals(expectedHash, Hex.toHexString(block.getHash()));

			// expectedNonce is the actual nonce from block#32 in Poc5 chain
			String expectedNonce = "0000000000000000000000000000000000000000000000001f52ebb192c4ea97"; // from Poc5 chain
			// Actual is "000000000000000000000000000000000000000000000000000000000098cc15" 
			// but that might also be a valid nonce in compliance with PoW(H!n, n) < (2^256 / difficulty)
			assertEquals(expectedNonce, Hex.toHexString(block.getNonce()));
		}
	}

	/**
	 * Produces a block equal to block#32 on PoC5 testnet (protocol 19)
	 * Where nonce was '0000000000000000000000000000000000000000000000001f52ebb192c4ea97'
	 * and resulting hash 'ce7201f6cc5eb1a6f35c7dda8acda111647a0f8a5bf0518e46579b03e29fe14b'
	 */
	private Block createBlock() {
		byte[] parentHash = Hex.decode("0a312c2b0a8f125c60a3976b6e508e740e095eb59943988d9bbfb8aa43922e31");
		byte[] unclesHash = Hex.decode("1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347");
		byte[] coinbase = Hex.decode("e559de5527492bcb42ec68d07df0742a98ec3f1e");
		byte[] difficulty = Hex.decode("3ee248");
		byte[] nonce = null;
		long number = 32;
		long minGasPrice = 10000000000000L;
		long gasLimit = 969216;
		long gasUsed = 0;
		long timestamp = 1401421088;
		Block newBlock = new Block(parentHash, unclesHash, coinbase,
				difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,
				null, nonce, null, null);
		// Setting stateRoot manually, because don't have state available.
		newBlock.setStateRoot(Hex.decode("50188ab86bdf164ac90eb2835a04a8930aae5393c3a2ef1166fb95028f9456b8"));
		return newBlock;
	}
	
	/**
	 * This test shows the difference between iterating over, 
	 * and comparing byte[] vs BigInteger value.
	 * 
	 * Results indicate that the former has ~15x better performance.
	 * Therefore this is used in the Miner.mine() method.
	 */
	@Test
	public void testIncrementPerformance() {
		boolean testEnabled = true;
		
		if(testEnabled) {
			byte[] counter1 = new byte[4];
			byte[] max = ByteBuffer.allocate(4).putInt(Integer.MAX_VALUE).array();
			long start1 = System.currentTimeMillis();
			while(ByteUtil.increment(counter1)) {
				if(FastByteComparisons.compareTo(counter1, 0, 4, max, 0, 4) == 0) {
					break;
				}
			}
			System.out.println(System.currentTimeMillis() - start1 + "ms to reach: " + Hex.toHexString(counter1));
			
			BigInteger counter2 = BigInteger.ZERO;
			long start2 = System.currentTimeMillis();
			while(true) {
				if(counter2.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 0) {
					break;
				}
				counter2 = counter2.add(BigInteger.ONE);
				
			}
			System.out.println(System.currentTimeMillis() - start2 + "ms to reach: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(4, counter2)));
		}
		
	}
}
