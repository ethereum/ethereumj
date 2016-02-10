package org.ethereum.samples;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.mine.Ethash;
import org.ethereum.mine.MinerListener;
import org.springframework.context.annotation.Bean;

/**
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PrivateNetSample {
    static class MinerNode extends BasicSample implements MinerListener{
        public MinerNode() {
            super("sampleMiner");
        }

        @Override
        public void run() {
            if (config.isMineFullDataset()) {
                logger.info("Generating Full Dataset (may take up to 10 min if not cached)...");
                // calling this just for indication of the dataset generation
                // basically this is not required
                Ethash ethash = Ethash.getForBlock(ethereum.getBlockchain().getBestBlock().getNumber());
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

    static class RegularNode extends BasicSample {
        public RegularNode() {
            super("sampleNode");
        }

        @Override
        public void onSyncDone() {
            super.onSyncDone();
        }
    }

    private static class MinerConfig {

        private final String config =
                "peer.discovery.enabled = false \n" +
                "peer.listen.port = 30335 \n" +
                "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                "peer.networkId = 555 \n" +
                "sync.enabled = true \n" +
                "genesis = sample-genesis.json \n" +
                "database.dir = sampleDB-1 \n" +
                "mine.extraDataHex = cccccccccccccccccccc \n" +
                "mine.cpuMineThreads = 2";

        @Bean
        public MinerNode node() {
            return new MinerNode();
        }

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    private static class RegularConfig {
        private final String config =
                "peer.discovery.enabled = false \n" +
                "peer.listen.port = 30336 \n" +
                "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                "peer.networkId = 555 \n" +
                "peer.active = [" +
                "    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335' }" +
                "] \n" +
                "sync.enabled = true \n" +
                "genesis = sample-genesis.json \n" +
                "database.dir = sampleDB-2 \n";

        @Bean
        public RegularNode node() {
            return new RegularNode();
        }

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    public static void main(String[] args) throws Exception {
        BasicSample.sLogger.info("Starting EthtereumJ miner instance!");
        EthereumFactory.createEthereum(MinerConfig.class);

//        BasicSample.sLogger.info("Starting EthtereumJ regular instance!");
//        EthereumFactory.createEthereum(RegularConfig.class);
    }
}
