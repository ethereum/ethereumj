package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.net.message.StaticMessages;
import org.spongycastle.util.encoders.Hex;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.junit.Test;

import static org.junit.Assert.*;

public class BlockTest {


	// https://ethereum.etherpad.mozilla.org/12
	private String CPP_PoC5_GENESIS_HEX_RLP_ENCODED = "f8abf8a7a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a011cc4aaa3b2f97cd6c858fcc0903b9b34b071e1798c91645f0e05e267028cb4a80834000008080830f4240808080a004994f67dc55b09e814ab7ffc8df3686b4afb2bb53e60eae97ef043fe03fb829c0c0";
	private String CPP_PoC5_GENESIS_HEX_HASH = "56fff6ab5ef6f1ef8dafb7b4571b89a9ae1ab870e54197c59ea10ba6f2c7eb60";

	String block_1 = "f9072df8d3a077ef4fdaf389dca53236bcf7f72698e154eab2828f86fbc4fc6c"
			+ "d9225d285c89a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0"
			+ "a142fd40d493479476f5eabe4b342ee56b8ceba6ab2a770c3e2198e7a0faa0ca"
			+ "43105f667dceb168eb4e0cdc98ef28a9da5c381edef70d843207601719a06785"
			+ "f3860460b2aa29122698e83a5151b270e82532c1663e89e3df8c5445b8ca833f"
			+ "f000018609184e72a000830f3e6f8227d2845387c58f80a00000000000000000"
			+ "0000000000000000000000000000000094148d7738f78c04f90654f8c6f8a080"
			+ "8609184e72a00082271094000000000000000000000000000000000000000080"
			+ "b83a33604557602a5160106000396000f200604556330e0f602a59366000530a"
			+ "0f602a596020600053013560005335576040600053016000546009581ca033a6"
			+ "bfa5eb2f4b63f1b98bed9a987d096d32e56deecb050367c84955508f5365a015"
			+ "034e7574ec073f0c448aac1d9f844387610dfef5342834b6825fbc35df5913a0"
			+ "ee258e73d41ada73d8d6071ba7d236fbbe24fcfb9627fbd4310e24ffd87b961a"
			+ "8203e9f90194f9016d018609184e72a000822710940000000000000000000000"
			+ "00000000000000000080b901067f4e616d655265670000000000000000000000"
			+ "00000000000000000000000000003057307f4e616d6552656700000000000000"
			+ "000000000000000000000000000000000000577f436f6e666967000000000000"
			+ "000000000000000000000000000000000000000073ccdeac59d35627b7de0933"
			+ "2e819d5159e7bb72505773ccdeac59d35627b7de09332e819d5159e7bb72507f"
			+ "436f6e6669670000000000000000000000000000000000000000000000000000"
			+ "57336045576041516100c56000396000f20036602259604556330e0f600f5933"
			+ "ff33560f601e5960003356576000335700604158600035560f602b590033560f"
			+ "603659600033565733600035576000353357001ca0f3c527e484ea5546189979"
			+ "c767b69aa9f1ad5a6f4b6077d4bccf5142723a67c9a069a4a29a2a315102fcd0"
			+ "822d39ad696a6d7988c993bb2b911cc2a78bb8902d91a01ebe4782ea3ed224cc"
			+ "bb777f5de9ee7b5bbb282ac08f7fa0ef95d3d1c1c6d1a1820ef7f8ccf8a60286"
			+ "09184e72a00082271094ccdeac59d35627b7de09332e819d5159e7bb725080b8"
			+ "4000000000000000000000000000000000000000000000000000000000000000"
			+ "000000000000000000000000002d0aceee7e5ab874e22ccf8d1a649f59106d74"
			+ "e81ba095ad45bf574c080e4d72da2cfd3dbe06cc814c1c662b5f74561f13e1e7"
			+ "5058f2a057745a3db5482bccb5db462922b074f4b79244c4b1fa811ed094d728"
			+ "e7b6da92a08599ea5d6cb6b9ad3311f0d82a3337125e05f4a82b9b0556cb3776"
			+ "a6e1a02f8782132df8abf885038609184e72a000822710942d0aceee7e5ab874"
			+ "e22ccf8d1a649f59106d74e880a0476176000000000000000000000000000000"
			+ "00000000000000000000000000001ca09b5fdabd54ebc284249d2d2df6d43875"
			+ "cb86c52bd2bac196d4f064c8ade054f2a07b33f5c8b277a408ec38d2457441d2"
			+ "af32e55681c8ecb28eef3d2a152e8db5a9a0227a67fceb1bf4ddd31a7047e24b"
			+ "e93c947ab3b539471555bb3509ed6e393c8e82178df90277f90250048609184e"
			+ "72a0008246dd94000000000000000000000000000000000000000080b901e961"
			+ "010033577f476176436f696e0000000000000000000000000000000000000000"
			+ "000000000060005460006000600760006000732d0aceee7e5ab874e22ccf8d1a"
			+ "649f59106d74e860645c03f150436000576000600157620f424060025761017d"
			+ "5161006c6000396000f2006020360e0f61013f59602060006000374360205460"
			+ "0056600054602056602054437f6e000000000000000000000000000000000000"
			+ "00000000000000000000000000560e0f0f61008059437f6e0000000000000000"
			+ "0000000000000000000000000000000000000000000000576000602054610400"
			+ "60005304600053036000547f6400000000000000000000000000000000000000"
			+ "0000000000000000000000005660016000030460406000200a0f61013e596001"
			+ "60205301602054600a6020530b0f6100f45961040060005304600053017f6400"
			+ "0000000000000000000000000000000000000000000000000000000000005760"
			+ "20537f6900000000000000000000000000000000000000000000000000000000"
			+ "000000576000537f640000000000000000000000000000000000000000000000"
			+ "000000000000000057006040360e0f0f61014a59003356604054600035566060"
			+ "546020356080546080536040530a0f6101695900608053604053033357608053"
			+ "60605301600035571ba0190fc7ab634dc497fe1656fde523a4c26926d51a93db"
			+ "2ba37af8e83c3741225da066ae0ec1217b0ca698a5369d4881e1c4cbde56af99"
			+ "31ebf9281580a23b659c08a051f947cb2315d0259f55848c630caa10cd91d6e4"
			+ "4ff8bad7758c65b25e2191308227d2c0";
	
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
    public void testGenesisFromRLP(){
    	// from RLP encoding
    	byte[] genesisBytes = Hex.decode(CPP_PoC5_GENESIS_HEX_RLP_ENCODED);
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
        assertEquals(CPP_PoC5_GENESIS_HEX_RLP_ENCODED, Hex.toHexString(genesis.getEncoded()));
        
        // Not really a good test because this compares Genesis.getHash() to itself
        assertEquals(CPP_PoC5_GENESIS_HEX_HASH, Hex.toHexString(genesis.getHash()));
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
        byte[] payload = Hex.decode(block_1);
        Block block = new Block(payload);
        System.out.println(block.toString());
    }
    
    @Test
    public void testCalcDifficulty() {
    	byte[] diffBytes = Genesis.getInstance().calcDifficulty();
      	BigInteger difficulty = new BigInteger(1, diffBytes);
    	System.out.println("Genesis difficulty = " + difficulty.toString());
    	assertEquals(new BigInteger(1, Genesis.DIFFICULTY), difficulty);
    	
    	Block block1 = new Block(Hex.decode(block_1));
        	diffBytes = block1.calcDifficulty();
    	difficulty = new BigInteger(1, diffBytes);
    	System.out.println("Block#1 difficulty = " + difficulty.toString());
    	assertEquals(new BigInteger(""), difficulty);
    }
    
    @Test
    public void testCalcGasLimit() {
    	long gasLimit = Genesis.getInstance().calcGasLimit();
    	System.out.println("Genesis gasLimit = " + gasLimit);
    	assertEquals(Genesis.GAS_LIMIT, gasLimit);
    	
    	// Test with block
    	Block block1 = new Block(Hex.decode(block_1));
    	gasLimit = block1.calcGasLimit();
    	System.out.println("Block#1 gasLimit = " + gasLimit);
    	assertEquals(999023, gasLimit);
    }
}