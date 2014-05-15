package org.ethereum.core;

import org.spongycastle.util.encoders.Hex;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;

public class BlockTest {
	
	// https://ethereum.etherpad.mozilla.org/12
	private String CPP_PoC5_GENESIS_STATE_ROOT_HEX_HASH = "2f4399b08efe68945c1cf90ffe85bbe3ce978959da753f9e649f034015b8817d";
	private String CPP_PoC5_GENESIS_HEX_RLP_ENCODED = "f8cbf8c7a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a02f4399b08efe68945c1cf90ffe85bbe3ce978959da753f9e649f034015b8817da00000000000000000000000000000000000000000000000000000000000000000834000008080830f4240808080a004994f67dc55b09e814ab7ffc8df3686b4afb2bb53e60eae97ef043fe03fb829c0c0";
	private String CPP_PoC5_GENESIS_HEX_HASH = "69a7356a245f9dc5b865475ada5ee4e89b18f93c06503a9db3b3630e88e9fb4e";

    @Test /* Creating genesis hash not ready yet */
    public void test1() throws IOException {

/*
        def serialize(self):
        txlist = [x.serialize() for x in self.transactions]
        header = [encode_int(self.number),
                self.prevhash,
                sha3(rlp.encode(self.uncles)),
                self.coinbase.decode('hex'),
                self.state.root,
                sha3(rlp.encode(txlist)),
                encode_int(self.difficulty),
                encode_int(self.timestamp),
                self.extradata,
                encode_int(self.nonce)]
        return rlp.encode([header, txlist, self.uncles])
*/

/*
        ( 0(256) - parentHash
        SHA3(RLP(emptyList)) - hashes of transactions
        0(160) - coinbase
        0(256) - stateRoot
        SHA3(RLP(emptyList)) - hashes of uncles
        2**22 - difficulty
        0 - timestamp
        () -
        42 - nonce  )
 () - uncles
 () - transactions


 	block.appendList(9) << h256() << sha3EmptyList << h160() << stateRoot << sha3EmptyList << c_genesisDifficulty << (uint)0 << string() << sha3(bytes(1, 42));
	block.appendRaw(RLPEmptyList);
	block.appendRaw(RLPEmptyList);

 */

    /* 1 */    byte[] prevHash =
                {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        prevHash = RLP.encodeElement(prevHash);

   /* 2 */    byte[] uncleList = RLP.encodeElement(HashUtil.sha3(RLP.encodeList(new byte[]{})));

   /* 3 */    byte[] coinbase =
                {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                 0x00, 0x00, 0x00, 0x00};
        coinbase = RLP.encodeElement(coinbase);

   /* 4 */    byte[] rootState =  {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
              rootState = RLP.encodeElement(rootState);

   /* 5 */      byte[] transactionsRoot = RLP.encodeElement(HashUtil.sha3(RLP.encodeList(new byte[]{})));

  /* 6 */  BigInteger difficulty = new BigInteger("2");
           difficulty = difficulty.pow(22);
           byte[] diffBytes = RLP.encodeElement(difficulty.toByteArray());

  /* 7 */  byte[] longTS = {0x00, 0x00, 0x00, 0x00};
           longTS = RLP.encodeElement(longTS);

  /* 8 */  byte[] extradata = {};
           extradata = RLP.encodeElement(extradata);

  /* 9 */  byte[] nonce = {42};
           nonce = RLP.encodeElement(HashUtil.sha3(nonce));

        byte[] header = RLP.encodeList( prevHash,
                uncleList,
                coinbase,
                rootState,
                transactionsRoot,
                diffBytes,
                longTS,
                extradata,
                nonce);

//	block.appendList(9) << h256() << sha3EmptyList << h160() << stateRoot << sha3EmptyList << c_genesisDifficulty << (uint)0 << string() << sha3(bytes(1, 42));

        byte[] txList     = RLP.encodeList(new byte[]{});
        byte[] unclesList = RLP.encodeList(new byte[]{});

        byte[] genesis = RLP.encodeList(header, txList, unclesList);
        System.out.println(Hex.toHexString(genesis));

        byte[] hash = HashUtil.sha3(genesis);
        assertEquals(CPP_PoC5_GENESIS_HEX_HASH, Hex.toHexString(hash));
    }

    @Test /* got from go guy */
    public void testGenesisFromRLP(){
    	// from RLP encoding
    	byte[] genesisBytes = Hex.decode(CPP_PoC5_GENESIS_HEX_RLP_ENCODED);
    	Block genesis = new Block(genesisBytes);
    	assertEquals(CPP_PoC5_GENESIS_HEX_HASH, Hex.toHexString(genesis.getHash()));
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
    	Block genesis = new Genesis();
        assertEquals(CPP_PoC5_GENESIS_HEX_RLP_ENCODED, Hex.toHexString(genesis.getEncoded()));
        assertEquals(CPP_PoC5_GENESIS_HEX_HASH, Hex.toHexString(genesis.getHash()));
    }
    
    @Test /* create BlockData from part of  real RLP BLOCKS message */
    public void test3(){

        String blocksMsg = "F8C8F8C4A07B2536237CBF114A043B0F9B27C76F84AC160EA5B87B53E42C7E76148964D450A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347943854AAF203BA5F8D49B1EC221329C7AEBCF050D3A07A3BE0EE10ECE4B03097BF74AABAC628AA0FAE617377D30AB1B97376EE31F41AA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347833FBFE884533F1CE880A0000000000000000000000000000000000000000000000000F3DEEA84969B6E95C0C0";

        byte[] payload = Hex.decode(blocksMsg);
        Block blockData = new Block(payload);
        System.out.println(blockData.toString());
    }

    @Test /* create BlockData from part of  real RLP BLOCKS message POC-5 */
    public void test4(){

        String blocksMsg = "F8D1A0085F6A51A63D1FBA43D6E5FE166A47BED64A8B93A99012537D50F3279D4CEA52A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D4934794D8758B101609A9F2A881A017BA86CBE6B7F0581DA068472689EA736CFC6B18FCAE9BA7454BADF9C65333A0317DFEFAE1D4AFFF6F90A000000000000000000000000000000000000000000000000000000000000000008401EDF1A18222778609184E72A0008080845373B0B180A0000000000000000000000000000000000000000000000000D1C0D8BC6D744943C0C0";

        byte[] payload = Hex.decode(blocksMsg);
        Block blockData = new Block(payload);
        System.out.println(blockData.toString());
    }
}

/*
[[ab6b9a5613970faa771b12d449b2e9bb925ab7a369f0a4b86b286e9d540099cf, 1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347, 3854aaf203ba5f8d49b1ec221329c7aebcf050d3, 990dc3b5acbee04124361d958fe51acb582593613fc290683940a0769549d3ed, 9bfe4817d274ea3eb8672e9fe848c3885b53bbbd1d7c26e6039f90fb96b942b0, 3ff000, 533f16b7, null, 00000000000000000000000000000000000000000000000077377adff6c227db, ]
        [
        [null, null, 0000000000000000000000000000000000000000, 09184e72a000, 2710, 606956330c0d630000003359366000530a0d630000003359602060005301356000533557604060005301600054630000000c58, 33606957, 1c, 7f6eb94576346488c6253197bde6a7e59ddc36f2773672c849402aa9c402c3c4, 6d254e662bf7450dd8d835160cbb053463fed0b53f2cdd7f3ea8731919c8e8cc, ]
        [01, null, 0000000000000000000000000000000000000000, 09184e72a000, 2710, 36630000002e59606956330c0d63000000155933ff33560d63000000275960003356576000335700630000005358600035560d630000003a590033560d63000000485960003356573360003557600035335700, 7f4e616d65526567000000000000000000000000000000000000000000000000003057307f4e616d65526567000000000000000000000000000000000000000000000000005733606957, 1b, 4af15a0ec494aeac5b243c8a2690833faa74c0f73db1f439d521c49c381513e9, 5802e64939be5a1f9d4d614038fbd5479538c48795614ef9c551477ecbdb49d2, ]
        [02, null, ccdeac59d35627b7de09332e819d5159e7bb7250, 09184e72a000, 2710, 00000000000000000000000000000000000000000000000000000000000000000000000000000000000000002d0aceee7e5ab874e22ccf8d1a649f59106d74e8, 1b, d05887574456c6de8f7a0d172342c2cbdd4cf7afe15d9dbb8b75b748ba6791c9, 1e87172a861f6c37b5a9e3a5d0d7393152a7fbe41530e5bb8ac8f35433e5931b, ]]*/
