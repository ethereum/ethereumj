package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

/**
 * The Miner performs the proof-of-work needed for a valid block
 * 
 * The mining proof-of-work (PoW) exists as a cryptographically secure nonce
 * that proves beyond reasonable doubt that a particular amount of computation 
 * has been expended in the determination of some token value n. 
 * It is utilised to enforce the blockchain security by giving meaning 
 * and credence to the notion of difficulty (and, by extension, total difficulty). 
 * 
 * However, since mining new blocks comes with an attached reward, 
 * the proof-of-work not only functions as a method of securing confidence 
 * that the blockchain will remain canonical into the future, but also as 
 * a wealth distribution mechanism.
 * 
 * See Yellow Paper: http://www.gavwood.com/Paper.pdf (chapter 11.5 Mining Proof-of-Work)
 */
public class Miner {

	/**
	 * Produces a block with a nonce that results in a hash that complies with the given difficulty
	 * 
	 * For the PoC series, we use a simplified proof-of-work. 
	 * This is not ASIC resistant and is meant merely as a placeholder. 
	 * It utilizes the bare SHA3 hash function to secure the block chain by requiring 
	 * the SHA3 hash of the concatenation of the nonce and the header’s SHA3 hash to be 
	 * sufficiently low. It is formally defined as PoW:
	 * 
	 * 		PoW(H, n) ≡ BE(SHA3(SHA3(RLP(H!n)) ◦ n))
	 *
	 * 	where:
	 * 		RLP(H!n) is the RLP encoding of the block header H, not including the
	 *			final nonce component;
	 *		SHA3 is the SHA3 hash function accepting an arbitrary length series of
	 *			bytes and evaluating to a series of 32 bytes (i.e. 256-bit);
	 *		n is the nonce, a series of 32 bytes;
	 *		o is the series concatenation operator;
	 *		BE(X) evaluates to the value equal to X when interpreted as a
	 *			big-endian-encoded integer.
	 * 
	 * @param block without a valid nonce
	 * @param the mining difficulty
	 * @return block with a valid nonce
	 */
	public Block mine(Block newBlock, byte[] difficulty) {

		BigInteger max = BigInteger.valueOf(2).pow(256);
		BigInteger target = max.divide(new BigInteger(1, difficulty));
		BigInteger counter = BigInteger.ZERO;
		while(counter.compareTo(max) < 0) {
			if(counter.mod(BigInteger.valueOf(10000)).longValue() == 0)
				System.out.println(counter.longValue());
			byte[] encodedWithoutNonce = newBlock.getEncodedWithoutNonce();
			byte[] concat = Arrays.concatenate(HashUtil.sha3(encodedWithoutNonce), BigIntegers.asUnsignedByteArray(counter));
			BigInteger result = new BigInteger(1, HashUtil.sha3(concat));
			if(result.compareTo(target) < 0) {
				newBlock.setNonce(counter);
				return newBlock;
			}
			counter = counter.add(BigInteger.ONE);
		}
		return null; // couldn't find a valid nonce
	}
}
