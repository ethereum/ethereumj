/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;


import org.ethereum.crypto.HashUtil;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.db.ByteArrayWrapper;

import org.junit.Ignore;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;


public class StateTest {

    private static final String GENESIS_STATE_ROOT = "7e204dc9cfb7acdf062ff0b8052f7fcb0b7e6593754773967932ce458d134af3";

    private static final Logger logger = LoggerFactory.getLogger("test");


    @Ignore //TODO #POC9
    @Test
    public void testGenesisAccounts() {
        Trie trie = generateGenesisState();
        assertEquals(GENESIS_STATE_ROOT, Hex.toHexString(trie.getRootHash()));
    }


    @Ignore
    @Test  // calc state after applying first tx on genesis
    public void test2() {

        // explanation:
        // 0) create genesis
        // 1) apply cost of tx to cd2a3d9f938e13cd947ec05abc7fe734df8dd826
        // 2) create AccountState for 77045e71a7a2c50903d88e564cd72fab11e82051
        // 3) minner gets the gas + coinbase ==> 6260000000000000 + 1500000000000000000
        // 4) calc the root

        Trie<byte[]> trie = generateGenesisState();
        String expected = "c12b4d771fbcc0d56ec106f8d465d24b9d4c36d60275bbafa7d69694d6708660";

        // Get and update sender in world state
        byte[] cowAddress = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
        byte[] rlpEncodedState = trie.get(cowAddress);
        AccountState account_1 = new AccountState(rlpEncodedState)
                .withBalanceIncrement(new BigInteger("-6260000000001000"))
                .withIncrementedNonce();
        trie.put(cowAddress, account_1.getEncoded());

        // Add contract to world state
        byte[] codeData = Hex.decode("61778e600054");
        AccountState account_2 = new AccountState(BigInteger.ZERO, BigInteger.valueOf(1000))
                .withCodeHash(HashUtil.sha3(codeData));
        byte[] contractAddress = Hex.decode("77045e71a7a2c50903d88e564cd72fab11e82051"); // generated based on sender + nonce
        trie.put(contractAddress, account_2.getEncoded());

//        this is saved in the db
//        trie.update(HashUtil.keccak(codeData), codeData);

        // Update miner in world state
        byte[] minerAddress = Hex.decode("4c5f4d519dff3c16f0d54b6866e256fbbbc1a600");
        AccountState account_3 = new AccountState(BigInteger.ZERO, new BigInteger("1506260000000000000"));
        trie.put(minerAddress, account_3.getEncoded());

        assertEquals(expected, Hex.toHexString(trie.getRootHash()));


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

    private Trie generateGenesisState() {

        Trie<byte[]> trie = new TrieImpl();
        Genesis genesis = (Genesis)Genesis.getInstance();
        for (ByteArrayWrapper key : genesis.getPremine().keySet()) {
            trie.put(key.getData(), genesis.getPremine().get(key).accountState.getEncoded());
        }

        return trie;
    }
}

