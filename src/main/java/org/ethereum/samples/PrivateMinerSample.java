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
package org.ethereum.samples;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.mine.Ethash;
import org.ethereum.mine.MinerListener;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;

/**
 * The sample creates a small private net with two peers: one is the miner, another is a regular peer
 * which is directly connected to the miner peer and starts submitting transactions which are then
 * included to blocks by the miner.
 *
 * Another concept demonstrated by this sample is the ability to run two independently configured
 * EthereumJ peers in a single JVM. For this two Spring ApplicationContext's are created which
 * are mostly differed by the configuration supplied
 *
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PrivateMinerSample {

    /**
     * Spring configuration class for the Miner peer
     */
    private static class MinerConfig {

        private final String config =
                // no need for discovery in that small network
                "peer.discovery.enabled = false \n" +
                "peer.listen.port = 30335 \n" +
                 // need to have different nodeId's for the peers
                "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                // our private net ID
                "peer.networkId = 555 \n" +
                // we have no peers to sync with
                "sync.enabled = false \n" +
                // genesis with a lower initial difficulty and some predefined known funded accounts
                "genesis = sample-genesis.json \n" +
                // two peers need to have separate database dirs
                "database.dir = sampleDB-1 \n" +
                // when more than 1 miner exist on the network extraData helps to identify the block creator
                "mine.extraDataHex = cccccccccccccccccccc \n" +
                "mine.cpuMineThreads = 2 \n" +
                "cache.flush.blocks = 1";

        @Bean
        public MinerNode node() {
            return new MinerNode();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    /**
     * Miner bean, which just start a miner upon creation and prints miner events
     */
    static class MinerNode extends BasicSample implements MinerListener{
        public MinerNode() {
            // peers need different loggers
            super("sampleMiner");
        }

        // overriding run() method since we don't need to wait for any discovery,
        // networking or sync events
        @Override
        public void run() {
            if (config.isMineFullDataset()) {
                logger.info("Generating Full Dataset (may take up to 10 min if not cached)...");
                // calling this just for indication of the dataset generation
                // basically this is not required
                Ethash ethash = Ethash.getForBlock(config, ethereum.getBlockchain().getBestBlock().getNumber());
                ethash.getFullDataset();
                logger.info("Full dataset generated (loaded).");
            }
            ethereum.getBlockMiner().addListener(this);
            ethereum.getBlockMiner().startMining();
        }

        @Override
        public void miningStarted() {
            logger.info("Miner started");
        }

        @Override
        public void miningStopped() {
            logger.info("Miner stopped");
        }

        @Override
        public void blockMiningStarted(Block block) {
            logger.info("Start mining block: " + block.getShortDescr());
        }

        @Override
        public void blockMined(Block block) {
            logger.info("Block mined! : \n" + block);
        }

        @Override
        public void blockMiningCanceled(Block block) {
            logger.info("Cancel mining block: " + block.getShortDescr());
        }
    }

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig {
        private final String config =
                // no discovery: we are connecting directly to the miner peer
                "peer.discovery.enabled = false \n" +
                "peer.listen.port = 30336 \n" +
                "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                "peer.networkId = 555 \n" +
                // actively connecting to the miner
                "peer.active = [" +
                "    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335' }" +
                "] \n" +
                "sync.enabled = true \n" +
                // all peers in the same network need to use the same genesis block
                "genesis = sample-genesis.json \n" +
                // two peers need to have separate database dirs
                "database.dir = sampleDB-2 \n";

        @Bean
        public RegularNode node() {
            return new RegularNode();
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    /**
     * The second node in the network which connects to the miner
     * waits for the sync and starts submitting transactions.
     * Those transactions should be included into mined blocks and the peer
     * should receive those blocks back
     */
    static class RegularNode extends BasicSample {
        public RegularNode() {
            // peers need different loggers
            super("sampleNode");
        }

        @Override
        public void onSyncDone() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        generateTransactions();
                    } catch (Exception e) {
                        logger.error("Error generating tx: ", e);
                    }
                }
            }).start();
        }

        /**
         * Generate one simple value transfer transaction each 7 seconds.
         * Thus blocks will include one, several and none transactions
         */
        private void generateTransactions() throws Exception{
            logger.info("Start generating transactions...");

            // the sender which some coins from the genesis
            ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
            byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");

            for (int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
                {
                    Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
                            ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                            receiverAddr, new byte[]{77}, new byte[0], ethereum.getChainIdForNextBlock());
                    tx.sign(senderKey);
                    logger.info("<== Submitting tx: " + tx);
                    ethereum.submitTransaction(tx);
                }
                Thread.sleep(7000);
            }
        }
    }

    /**
     *  Creating two EthereumJ instances with different config classes
     */
    public static void main(String[] args) throws Exception {
        if (Runtime.getRuntime().maxMemory() < (1250L << 20)) {
            MinerNode.sLogger.error("Not enough JVM heap (" + (Runtime.getRuntime().maxMemory() >> 20) + "Mb) to generate DAG for mining (DAG requires min 1G). For this sample it is recommended to set -Xmx2G JVM option");
            return;
        }

        BasicSample.sLogger.info("Starting EthtereumJ miner instance!");
        EthereumFactory.createEthereum(MinerConfig.class);

        BasicSample.sLogger.info("Starting EthtereumJ regular instance!");
        EthereumFactory.createEthereum(RegularConfig.class);
    }
}
