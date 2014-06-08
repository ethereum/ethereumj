package org.ethereum.core;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class MinerTest {

	private Block blockWithoutNonce;
	
	@Before
	public void createNewBlock() {
		byte[] parentHash = Hex.decode("0a312c2b0a8f125c60a3976b6e508e740e095eb59943988d9bbfb8aa43922e31");
		byte[] unclesHash = Hex.decode("1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347");
		byte[] coinbase = Hex.decode("e559de5527492bcb42ec68d07df0742a98ec3f1e");
		byte[] difficulty = Hex.decode("3ee248");
		long number = 32;
		long minGasPrice = 10000000000000L;
		long gasLimit = 969216;
		long gasUsed = 0;
		long timestamp = 1401421088;
		blockWithoutNonce = new Block(parentHash, unclesHash, coinbase,
				difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,
				null, null, null, null);
		blockWithoutNonce.setStateRoot(Hex.decode("50188ab86bdf164ac90eb2835a04a8930aae5393c3a2ef1166fb95028f9456b8"));
	}
	
	@Test
	public void testMine() {
		boolean miningTestEnabled = false;
		
		if(miningTestEnabled) {
			String expectedNone = "0000000000000000000000000000000000000000000000001f52ebb192c4ea97";
			System.out.println("Searching for nonce of following block: \n" + blockWithoutNonce.toString());
			Miner miner = new Miner();
			Block blockWithNonce = miner.mine(blockWithoutNonce, blockWithoutNonce.getDifficulty());
			assertEquals(expectedNone, Hex.toHexString(blockWithNonce.getNonce()));
		}
	}

}
