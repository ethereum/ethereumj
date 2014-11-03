package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.manager.WorldManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlockTest {

        private static final Logger logger = LoggerFactory.getLogger("test");


    @BeforeClass
    public static void setUp() {
        if (WorldManager.getInstance().getRepository().isClosed())
            WorldManager.getInstance().reset();
    }

    @AfterClass
    public static void tearDown() {
        WorldManager.getInstance().close();
    }

	// https://ethereum.etherpad.mozilla.org/12
	private String PoC7_GENESIS_HEX_RLP_ENCODED = "f9012ff9012aa00000000000000000000000000000000000000000000000000000000000000000a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470940000000000000000000000000000000000000000a0c67c70f5d7d3049337d1dcc0503a249881120019a8e7322774dbfe57b463718ca056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b84000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000830200008080830f4240808080a004994f67dc55b09e814ab7ffc8df3686b4afb2bb53e60eae97ef043fe03fb829c0c0";
	private String PoC7_GENESIS_HEX_HASH = "19039f385f269fae48d497fba579d7079c761cec5643ec1096e98bdda4ffcf00";

	String block_2 = "f8b5f8b1a0cf4b25b08b39350304fe12a16e4216c01a426f8f3dbf0d392b5b45"
				   + "8ffb6a399da01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a1"
				   + "42fd40d493479476f5eabe4b342ee56b8ceba6ab2a770c3e2198e7a08a22d58b"
				   + "a5c65b2bf660a961b075a43f564374d38bfe6cc69823eea574d1d16e80833fe0"
				   + "04028609184e72a000830f3aab80845387c60380a00000000000000000000000"
				   + "0000000000000000000000000033172b6669131179c0c0";
	
    String block_17 = "f9016df8d3a0aa142573b355c6f2e8306471c869b0d12d0638cea3f57d39991a"
    		+ "b1b03ffa40daa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40"
    		+ "d4934794e559de5527492bcb42ec68d07df0742a98ec3f1ea031c973c20e7a15c319a9ff"
    		+ "9b0aab5bdc121320767fee71fb2b771ce1c93cf812a01b224ec310c2bfb40fd0e6a668ee"
    		+ "7c06a5a4a4bfb99620d0fea8f7b43315dd59833f3130118609184e72a000830f01ec8201"
    		+ "f4845387f36980a00000000000000000000000000000000000000000000000000532c3ae"
    		+ "9b3503f6f895f893f86d018609184e72a0008201f494f625565ac58ec5dadfce1b8f9fb1"
    		+ "dd30db48613b8862cf5246d0c80000801ca05caa26abb350e0521a25b8df229806f3777d"
    		+ "9e262996493846a590c7011697dba07bb7680a256ede4034212b7a1ae6c7caea73190cb0"
    		+ "7dedb91a07b72f34074e76a00cd22d78d556175604407dc6109797f5c8d990d05f1b352e"
    		+ "10c71b3dd74bc70f8201f4c0";
    
    String block_32 = "f8b5f8b1a00a312c2b0a8f125c60a3976b6e508e740e095eb59943988d9bbfb8"
    		+ "aa43922e31a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4"
    		+ "934794e559de5527492bcb42ec68d07df0742a98ec3f1ea050188ab86bdf164ac90eb283"
    		+ "5a04a8930aae5393c3a2ef1166fb95028f9456b880833ee248208609184e72a000830eca"
    		+ "0080845387fd2080a00000000000000000000000000000000000000000000000001f52eb"
    		+ "b192c4ea97c0c0";
	
    @Test /* got from go guy */
    public void testGenesisFromRLP() {
    	// from RLP encoding
    	byte[] genesisBytes = Hex.decode(PoC7_GENESIS_HEX_RLP_ENCODED);
    	Block genesisFromRLP = new Block(genesisBytes);
    	Block genesis = Genesis.getInstance();
    	assertEquals(Hex.toHexString(genesis.getHash()), Hex.toHexString(genesisFromRLP.getHash()));
    	assertEquals(Hex.toHexString(genesis.getParentHash()), Hex.toHexString(genesisFromRLP.getParentHash()));
    	assertEquals(Hex.toHexString(genesis.getStateRoot()), Hex.toHexString(genesisFromRLP.getStateRoot()));
    }
    
    @Test
    public void testGenesisFromNew() {
        /*	From: https://ethereum.etherpad.mozilla.org/11		
          	Genesis block is: 
             		(
             		    todo: update it
             			B32(0, 0, ...), 
        				B32(sha3(B())), 
        				B20(0, 0, ...), 
        				B32(stateRoot), 
        				B32(0, 0, ...), 
		    			P(2^22), 
        				P(0), 
        				P(0), 
        				P(1000000), 
        				P(0), 
        				P(0)
        				B()
        				B32(sha3(B(42)))
        			)
         */
    	Block genesis = Genesis.getInstance();
        logger.info("genesis hash: [{}]", Hex.toHexString(genesis.getHash()));
        logger.info("genesis rlp: [{}]", Hex.toHexString(genesis.getEncoded()));
        assertEquals(PoC7_GENESIS_HEX_HASH, Hex.toHexString(genesis.getHash()));
    	assertEquals(PoC7_GENESIS_HEX_RLP_ENCODED, Hex.toHexString(genesis.getEncoded()));
    }
    
    @Test /* block without transactions - block#32 in PoC5 cpp-chain */
    public void testEmptyBlock() {
        byte[] payload = Hex.decode(block_32);
        Block blockData = new Block(payload);
        System.out.println(blockData.toString());
    }

    @Test /* block with single balance transfer transaction - block#17 in PoC5 cpp-chain */
    public void testSingleBalanceTransfer() {
        byte[] payload = Hex.decode(block_17);
        Block blockData = new Block(payload);
        System.out.println(blockData.toString());
    }

    @Test /* large block with 5 transactions -block#1 in PoC5 cpp-chain */
    public void testBlockWithContractCreation() {
        byte[] payload = Hex.decode(PoC7_GENESIS_HEX_RLP_ENCODED);
        Block block = new Block(payload);
        System.out.println(block.toString());
    }
    
    @Test
    public void testCalcDifficulty() {

        Block genesis = Genesis.getInstance();
        BigInteger difficulty = new BigInteger(1, genesis.calcDifficulty());
        System.out.println("Genesis difficulty = " + difficulty.toString());
        assertEquals(new BigInteger(1, Genesis.DIFFICULTY), difficulty);

        // Storing genesis because the parent needs to be in the DB for calculation.
        WorldManager.getInstance().getBlockchain().add(genesis);

        Block block1 = new Block(Hex.decode(PoC7_GENESIS_HEX_RLP_ENCODED));
        BigInteger calcDifficulty = new BigInteger(1, block1.calcDifficulty());
        BigInteger actualDifficulty = new BigInteger(1, block1.getDifficulty());
        System.out.println("Block#1 actual difficulty = " + actualDifficulty.toString());
        System.out.println("Block#1 calculated difficulty = " + calcDifficulty.toString());
        assertEquals(actualDifficulty, calcDifficulty);
    }
    
    @Test
    public void testCalcGasLimit() {
    	WorldManager.getInstance();
    	Block genesis = Genesis.getInstance();
    	long gasLimit = genesis.calcGasLimit();
    	System.out.println("Genesis gasLimit = " + gasLimit);
    	assertEquals(Genesis.GAS_LIMIT, gasLimit);
    	
    	// Storing genesis because the parent needs to be in the DB for calculation.
		WorldManager.getInstance().getBlockchain().add(genesis);
    	
    	// Test with block
    	Block block1 = new Block(Hex.decode(PoC7_GENESIS_HEX_RLP_ENCODED));
    	long calcGasLimit = block1.calcGasLimit();
    	long actualGasLimit = block1.getGasLimit();
    	System.out.println("Block#1 actual gasLimit = " + actualGasLimit);
    	System.out.println("Block#1 calculated gasLimit = " + calcGasLimit);
    	assertEquals(actualGasLimit, calcGasLimit);
    }

    @Test
    @Ignore
    public void testUncleValidGenerationGap() {
    	// TODO
    	fail("Not yet implemented");
    }
    
    @Test
    @Ignore
    public void testUncleInvalidGenerationGap() {
    	// TODO
    	fail("Not yet implemented");
    }
    
    @Test
    @Ignore
    public void testUncleInvalidParentGenerationGap() {
    	// TODO
    	fail("Not yet implemented");
    }
}