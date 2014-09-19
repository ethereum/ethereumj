package org.ethereum.core;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.MockDB;
import org.ethereum.trie.Trie;
import org.ethereum.util.RLP;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class StateTest {

    @Test
    public void testGenesisAccounts() {
        Trie trie = generateGenesis();
        assertEquals("b4fb905967f9b64f7abb0912884ecc49a13689e268d0913460791746e16aeb7c", Hex.toHexString(trie.getRootHash()));
    }

    @Test  // right way to calc tx trie hash
    public void testCalculatePostTxState() {

        /*    txTrieHash=a77691cf47bec9021d3f027fc8ef2d51b758b600a79967154354b8e37108896f */
        String expected = "a77691cf47bec9021d3f027fc8ef2d51b758b600a79967154354b8e37108896f";
        Transaction tx = new Transaction(
                new byte[]{},
                Hex.decode("09184E72A000"),
                Hex.decode("03E8"),
                Hex.decode("0000000000000000000000000000000000000000"),
                Hex.decode("03e8"),
                Hex.decode("60016000546006601160003960066000f261778e600054")
        );
        byte[] cowPrivKey = Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4");
        tx.sign(cowPrivKey);

        byte[] postTxState = Hex.decode("7fa5bd00f6e03b5a5718560f1e25179b227167585a3c3da06a48f554365fb527");
        byte[] cumGas      = Hex.decode("0272");

        TransactionReceipt tr = new TransactionReceipt(tx, postTxState, cumGas);

        Trie trie = new Trie(new MockDB());
        trie.update(RLP.encodeInt(0), tr.getEncoded());
        String txTrieRoot = Hex.toHexString(trie.getRootHash());
        System.out.println(txTrieRoot);
        assertEquals(expected, txTrieRoot);

        /* *** GROSS DATA ***

        BlockData [
          hash=22cf863ab836a6f5c29389d2e77f4792a3b3b52908c98ed14b1cbe91491a3e36
          parentHash=77ef4fdaf389dca53236bcf7f72698e154eab2828f86fbc4fc6cd9225d285c89
          unclesHash=1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347
          coinbase=4c5f4d519dff3c16f0d54b6866e256fbbbc1a600
          stateHash=69c21ff84a5af0b53b11c61110a16d6ad43dad37b3eb29ae8e88c936eb06456a
          txTrieHash=a77691cf47bec9021d3f027fc8ef2d51b758b600a79967154354b8e37108896f
          difficulty=3ff000
          number=1
          minGasPrice=10000000000000
          gasLimit=999023
          gasUsed=626
          timestamp=1401979976 (2014.06.05 15:52:56)
          extraData=null
          nonce=0000000000000000000000000000000000000000000000005d439960040e4505

        TransactionReceipt[
           TransactionData [ hash=1ee6fa3149a5e9c09b54009eb6e108aaa7ecd79483d57eedcf2dff93a1505588  nonce=null,
               gasPrice=09184e72a000, gas=03e8, receiveAddress=0000000000000000000000000000000000000000, value=03e8,
               data=60016000546006601160003960066000f261778e600054, signatureV=27,
               signatureR=2b379f22050e3554c3fa5423d9040bb28dcc7f905300db4e67c03bcf9b27003c,
               signatureS=59f47793e050974e6b5fca2848b19925637b883a012693b54d712f1c4f74def5
          ]
          , postTxState=7fa5bd00f6e03b5a5718560f1e25179b227167585a3c3da06a48f554365fb527
          , cumulativeGas=0272]
        ]

        +++  4c5f4d519dff3c16f0d54b6866e256fbbbc1a600:
        +++  77045e71a7a2c50903d88e564cd72fab11e82051: $[61,77,8e,60,0,54] ([])
         *   cd2a3d9f938e13cd947ec05abc7fe734df8dd826: #1 1606938044258990275541962092341162602522202987522792835300376 (-6260000000001000)
          */
    }

    @Test  // calc state after applying first tx on genesis
    public void test2() {

        // explanation:
        // 0) create genesis
        // 1) apply cost of tx to cd2a3d9f938e13cd947ec05abc7fe734df8dd826
        // 2) create AccountState for 77045e71a7a2c50903d88e564cd72fab11e82051
        // 3) minner gets the gas + coinbase ==> 6260000000000000 + 1500000000000000000
        // 4) calc the root

        Trie trie = generateGenesis();
        String expected = "be627444b72cad709e3c8ed325d71c965024b64d938fa10a01aa46bd9599b705";

        // Get and update sender in world state
        byte[] cowAddress = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
        byte[] rlpEncodedState = trie.get(cowAddress);
        AccountState account_1 = new AccountState(rlpEncodedState);
        account_1.addToBalance(new BigInteger("-6260000000001000"));
        account_1.incrementNonce();
        trie.update(cowAddress, account_1.getEncoded());

        // Add contract to world state
        byte[] codeData = Hex.decode("61778e600054");
        AccountState account_2 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(1000));
        account_2.setCodeHash(HashUtil.sha3(codeData));
        byte[] contractAddress = Hex.decode("77045e71a7a2c50903d88e564cd72fab11e82051"); // generated based on sender + nonce
        trie.update(contractAddress, account_2.getEncoded());

//        this is saved in the db
//        trie.update(HashUtil.sha3(codeData), codeData);

        // Update miner in world state
        byte[] minerAddress = Hex.decode("4c5f4d519dff3c16f0d54b6866e256fbbbc1a600");
        AccountState account_3 = new AccountState(BigInteger.ZERO, new BigInteger("1506260000000000000"));
        trie.update(minerAddress, account_3.getEncoded());
        
        assertEquals(expected,  Hex.toHexString(trie.getRootHash()));


        /* *** GROSS DATA ***

        BlockData [
          hash=22cf863ab836a6f5c29389d2e77f4792a3b3b52908c98ed14b1cbe91491a3e36
          parentHash=77ef4fdaf389dca53236bcf7f72698e154eab2828f86fbc4fc6cd9225d285c89
          unclesHash=1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347
          coinbase=4c5f4d519dff3c16f0d54b6866e256fbbbc1a600
          stateHash=69c21ff84a5af0b53b11c61110a16d6ad43dad37b3eb29ae8e88c936eb06456a
          txTrieHash=a77691cf47bec9021d3f027fc8ef2d51b758b600a79967154354b8e37108896f
          difficulty=3ff000
          number=1
          minGasPrice=10000000000000
          gasLimit=999023
          gasUsed=626
          timestamp=1401979976 (2014.06.05 15:52:56)
          extraData=null
          nonce=0000000000000000000000000000000000000000000000005d439960040e4505

        TransactionReceipt[
           TransactionData [ hash=1ee6fa3149a5e9c09b54009eb6e108aaa7ecd79483d57eedcf2dff93a1505588  nonce=null,
               gasPrice=09184e72a000, gas=03e8, receiveAddress=0000000000000000000000000000000000000000, value=03e8,
               data=60016000546006601160003960066000f261778e600054, signatureV=27,
               signatureR=2b379f22050e3554c3fa5423d9040bb28dcc7f905300db4e67c03bcf9b27003c,
               signatureS=59f47793e050974e6b5fca2848b19925637b883a012693b54d712f1c4f74def5
          ]
          , postTxState=7fa5bd00f6e03b5a5718560f1e25179b227167585a3c3da06a48f554365fb527
          , cumulativeGas=0272]
        ]

        +++  4c5f4d519dff3c16f0d54b6866e256fbbbc1a600:
        +++  77045e71a7a2c50903d88e564cd72fab11e82051: $[61,77,8e,60,0,54] ([])
         *   cd2a3d9f938e13cd947ec05abc7fe734df8dd826: #1 1606938044258990275541962092341162602522202987522792835300376 (-6260000000001000)
          */

        assertEquals(expected, Hex.toHexString(trie.getRootHash()));
    }

    private Trie generateGenesis() {

        Trie trie = new Trie(new MockDB());
        // 2ef47100e0787b915105fd5e3f4ff6752079d5cb # (M)
        AccountState acct5 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("2ef47100e0787b915105fd5e3f4ff6752079d5cb"), acct5.getEncoded());

        // 1a26338f0d905e295fccb71fa9ea849ffa12aaf4 # (A)
        AccountState acct4 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("1a26338f0d905e295fccb71fa9ea849ffa12aaf4"), acct4.getEncoded());

        // e6716f9544a56c530d868e4bfbacb172315bdead # (J)
        AccountState acct2 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("e6716f9544a56c530d868e4bfbacb172315bdead"), acct2.getEncoded());

        // 8a40bfaa73256b60764c1bf40675a99083efb075 # (G)
        AccountState acct1 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("8a40bfaa73256b60764c1bf40675a99083efb075"), acct1.getEncoded());

        // e4157b34ea9615cfbde6b4fda419828124b70c78 # (CH)
        AccountState acct8 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("e4157b34ea9615cfbde6b4fda419828124b70c78"), acct8.getEncoded());

        // 1e12515ce3e0f817a4ddef9ca55788a1d66bd2df # (V)
        AccountState acct3 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("1e12515ce3e0f817a4ddef9ca55788a1d66bd2df"), acct3.getEncoded());

        // 6c386a4b26f73c802f34673f7248bb118f97424a # (HH)
        AccountState acct7 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("6c386a4b26f73c802f34673f7248bb118f97424a"), acct7.getEncoded());

        // cd2a3d9f938e13cd947ec05abc7fe734df8dd826 # (R)
        AccountState acct6 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(2).pow(200));
        trie.update(Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826"), acct6.getEncoded());
        return trie;
    }
}

