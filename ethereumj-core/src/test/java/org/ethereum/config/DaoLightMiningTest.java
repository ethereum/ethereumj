package org.ethereum.config;

import org.ethereum.config.blockchain.DaoHFConfig;
import org.ethereum.config.blockchain.DaoNoHFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.core.Block;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Test;

import java.math.BigInteger;

import static org.ethereum.util.ByteUtil.toHexString;
import static org.junit.Assert.*;

/**
 * Created by Stan Reshetnyk on 29.12.16.
 */
public class DaoLightMiningTest {

    // configure
    final int FORK_BLOCK = 20;
    final int FORK_BLOCK_AFFECTED = 10; // hardcoded in DAO config


    @Test
    public void testDaoExtraData() {
        final StandaloneBlockchain sb = createBlockchain(true);

        for (int i = 0; i < FORK_BLOCK + 30; i++) {
            Block b = sb.createBlock();
//            System.out.println("Created block " + b.getNumber() + " " + toHexString(b.getExtraData()));
        }

        final Block preForkBlock = sb.getBlockchain().getBlockByNumber(FORK_BLOCK - 1);
        final Block forkBlock = sb.getBlockchain().getBlockByNumber(FORK_BLOCK);
        final Block lastForkBlock = sb.getBlockchain().getBlockByNumber(FORK_BLOCK + FORK_BLOCK_AFFECTED - 1);
        final Block normalBlock = sb.getBlockchain().getBlockByNumber(FORK_BLOCK + FORK_BLOCK_AFFECTED);

        assertEquals("", toHexString(preForkBlock.getExtraData()));
        assertEquals("64616f2d686172642d666f726b", toHexString(forkBlock.getExtraData()));
        assertEquals("64616f2d686172642d666f726b", toHexString(lastForkBlock.getExtraData()));
        assertEquals("", toHexString(normalBlock.getExtraData()));
    }

    @Test
    public void testNoDaoExtraData() {
        final StandaloneBlockchain sb = createBlockchain(false);

        for (int i = 0; i < FORK_BLOCK + 30; i++) {
            Block b = sb.createBlock();
            assertEquals("", toHexString(b.getExtraData()));
        }
    }

    private StandaloneBlockchain createBlockchain(boolean proFork) {
        final BaseNetConfig netConfig = new BaseNetConfig();
        final FrontierConfig c1 = new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        });
        netConfig.add(0, c1);
        netConfig.add(FORK_BLOCK, proFork ? new DaoHFConfig(c1, FORK_BLOCK) : new DaoNoHFConfig(c1, FORK_BLOCK));
        System.setProperty("mine.extraDataHex", "");

        SystemProperties.getDefault().setBlockchainConfig(netConfig);

        // create blockchain
        return new StandaloneBlockchain()
                .withAutoblock(true);
    }
}
