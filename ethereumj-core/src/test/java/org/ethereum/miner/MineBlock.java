package org.ethereum.miner;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.*;
import org.ethereum.core.genesis.GenesisLoader;
import org.ethereum.crypto.ECKey;
import org.ethereum.mine.Ethash;
import org.ethereum.util.ByteUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 10.12.2015.
 */
public class MineBlock {

    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }


    @Test
    public void mine1() throws Exception {
        BlockchainImpl blockchain = ImportLightTest.createBlockchain(GenesisLoader.loadGenesis(
                getClass().getResourceAsStream("/genesis/genesis-light.json")));
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
