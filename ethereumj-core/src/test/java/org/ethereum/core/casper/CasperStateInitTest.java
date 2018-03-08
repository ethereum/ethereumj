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
package org.ethereum.core.casper;

import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.casper.core.genesis.CasperStateInit;
import org.ethereum.db.ByteArrayWrapper;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static junit.framework.TestCase.assertEquals;

@Ignore  // Takes too long to run usually
public class CasperStateInitTest extends CasperBase {

    /**
     * Used same values like in Casper Test Network based on Pyethereum
     */
    @Test
    public void genesisPlusBlock() {
        // Init with Genesis
        final Genesis genesis = Genesis.getInstance(systemProperties);

        CasperStateInit casperStateInit = new CasperStateInit(genesis, repository, blockchain, systemProperties);
        casperStateInit.initDB();
        casper.setInitTxs(casperStateInit.makeInitTxes().getValue());

        // Check after genesis
        assertEquals(new ByteArrayWrapper(Hex.decode("f3f713c5ff3119287ae62861e3fd90d6afc94b57d06151007c409b86bf419d11")),
                new ByteArrayWrapper(blockchain.getBestBlock().getStateRoot()));
        assertEquals(new ByteArrayWrapper(Hex.decode("5d0dfcfbcb941825c7ed52c846dc2021e29374f6954c4eaf6f7352f63ec8cab4")),
                new ByteArrayWrapper(blockchain.getBestBlock().getHash()));

        // Mine 1st block
        Block block1 = bc.createBlock(Hex.decode("3535353535353535353535353535353535353535"));

        // Check after 1st block
        assertEquals(new ByteArrayWrapper(Hex.decode("b1b5d87eeadab3ffc0e4045ee18fc63ccc06b8e9b7195af8f1a4450557c3818d")),
                new ByteArrayWrapper(block1.getStateRoot()));

        assertEquals(new ByteArrayWrapper(Hex.decode("bd832b0cd3291c39ef67691858f35c71dfb3bf21")),
                new ByteArrayWrapper(systemProperties.getCasperAddress()));
    }
}
