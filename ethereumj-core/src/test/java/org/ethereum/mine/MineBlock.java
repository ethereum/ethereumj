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
package org.ethereum.mine;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.core.*;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.PruneManager;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.spongycastle.util.encoders.Hex;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 10.12.2015.
 */
public class MineBlock {

    @Mock
    PruneManager pruneManager;

    @InjectMocks
    @Resource
    BlockchainImpl blockchain = ImportLightTest.createBlockchain(GenesisLoader.loadGenesis(
            getClass().getResourceAsStream("/genesis/genesis-light.json")));

    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

    @Before
    public void setUp() throws Exception {
        // Initialize mocks created above
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void mine1() throws Exception {

        blockchain.setMinerCoinbase(Hex.decode("ee0250c19ad59305b2bdb61f34b45b72fe37154f"));
        Block parent = blockchain.getBestBlock();

        List<Transaction> pendingTx = new ArrayList<>();

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
        byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");
        Transaction tx = new Transaction(new byte[] {0}, new byte[] {1}, ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                receiverAddr, new byte[] {77}, new byte[0]);
        tx.sign(senderKey);
        pendingTx.add(tx);

        Block b = blockchain.createNewBlock(parent, pendingTx, Collections.EMPTY_LIST);

        System.out.println("Mining...");
        Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get();
        System.out.println("Validating...");
        boolean valid = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader());
        Assert.assertTrue(valid);

        System.out.println("Connecting...");
        ImportResult importResult = blockchain.tryToConnect(b);

        Assert.assertTrue(importResult == ImportResult.IMPORTED_BEST);
        System.out.println(Hex.toHexString(blockchain.getRepository().getRoot()));
    }
}
