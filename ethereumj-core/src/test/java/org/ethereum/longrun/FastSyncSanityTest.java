package org.ethereum.longrun;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.mutable.MutableObject;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Bloom;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionInfo;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Serializers;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.SourceCodec;
import org.ethereum.db.DbFlushManager;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.sync.SyncPool;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Value;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static org.ethereum.core.BlockchainImpl.calcReceiptsTrie;

/**
 * Fast Sync with sanity check
 *
 * Runs fast sync with defined config
 * - checks State Trie is not broken
 * - checks whether all blocks are in blockstore, validates parent connection and bodies
 * - checks and validate transaction receipts
 *
 * Run with '-Dlogback.configurationFile=longrun/logback.xml' for proper logging
 * Also following flags are available:
 *     -Dreset.db.onFirstRun=true
 *     -Doverride.config.res=longrun/conf/live.conf
 */
@Ignore
public class FastSyncSanityTest {

    private Ethereum regularNode;
    private static AtomicBoolean firstRun = new AtomicBoolean(true);
    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");
    private static final MutableObject<String> configPath = new MutableObject<>("longrun/conf/ropsten.conf");
    private static final MutableObject<Boolean> resetDBOnFirstRun = new MutableObject<>(null);

    public FastSyncSanityTest() throws Exception {

        String resetDb = System.getProperty("reset.db.onFirstRun");
        String overrideConfigPath = System.getProperty("override.config.res");
        if (Boolean.parseBoolean(resetDb)) {
            resetDBOnFirstRun.setValue(true);
        } else if (resetDb != null && resetDb.equalsIgnoreCase("false")) {
            resetDBOnFirstRun.setValue(false);
        }
        if (overrideConfigPath != null) configPath.setValue(overrideConfigPath);

        statTimer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (fatalErrors.get() > 0) {
                        statTimer.shutdownNow();
                    }
                } catch (Throwable t) {
                    FastSyncSanityTest.testLogger.error("Unhandled exception", t);
                }
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    private static class BasicSample implements Runnable {
        static final Logger sLogger = LoggerFactory.getLogger("sample");

        private String loggerName;
        public Logger logger;

        @Autowired
        protected Ethereum ethereum;

        @Autowired
        protected SystemProperties config;

        @Autowired
        protected SyncPool syncPool;

        @Autowired
        protected CommonConfig commonConfig;

        @Autowired
        protected DbFlushManager dbFlushManager;

        // Spring config class which add this sample class as a bean to the components collections
        // and make it possible for autowiring other components
        private static class Config {
            @Bean
            public FastSyncSanityTest.BasicSample basicSample() {
                return new FastSyncSanityTest.BasicSample();
            }
        }

        public static void main(String[] args) throws Exception {
            sLogger.info("Starting EthereumJ!");

            // Based on Config class the BasicSample would be created by Spring
            // and its springInit() method would be called as an entry point
            EthereumFactory.createEthereum(Config.class);
        }

        public BasicSample() {
            this("sample");
        }

        /**
         * logger name can be passed if more than one EthereumJ instance is created
         * in a single JVM to distinguish logging output from different instances
         */
        public BasicSample(String loggerName) {
            this.loggerName = loggerName;
        }

        /**
         * The method is called after all EthereumJ instances are created
         */
        @PostConstruct
        private void springInit() {
            logger = LoggerFactory.getLogger(loggerName);
            // adding the main EthereumJ callback to be notified on different kind of events
            ethereum.addListener(listener);

            logger.info("Sample component created. Listening for ethereum events...");

            // starting lifecycle tracking method run()
            new Thread(this, "SampleWorkThread").start();
        }

        /**
         * The method tracks step-by-step the instance lifecycle from node discovery till sync completion.
         * At the end the method onSyncDone() is called which might be overridden by a sample subclass
         * to start making other things with the Ethereum network
         */
        public void run() {
            try {
                logger.info("Sample worker thread started.");

                if (!config.peerDiscovery()) {
                    logger.info("Peer discovery disabled. We should actively connect to another peers or wait for incoming connections");
                }

                waitForSync();

                onSyncDone();

            } catch (Exception e) {
                logger.error("Error occurred in Sample: ", e);
            }
        }


        /**
         * Waits until the whole blockchain sync is complete
         */
        private void waitForSync() throws Exception {
            logger.info("Waiting for the whole blockchain sync (will take up to an hour on fast sync for the whole chain)...");
            while(true) {
                sleep(10000);

                if (synced) {
                    logger.info("[v] Sync complete! The best block: " + bestBlock.getShortDescr());
                    syncComplete = true;

                    // Stop syncing
                    config.setSyncEnabled(false);
                    config.setDiscoveryEnabled(false);
                    ethereum.getChannelManager().close();
                    syncPool.close();

                    // Full sanity check
                    fullSanityCheck(ethereum, commonConfig);
                    return;
                }
            }
        }

        /**
         * Is called when the whole blockchain sync is complete
         */
        public void onSyncDone() throws Exception {
            logger.info("Monitoring new blocks in real-time...");
        }

        protected Map<Node, StatusMessage> ethNodes = new Hashtable<>();
        protected List<Node> syncPeers = new Vector<>();

        protected Block bestBlock = null;

        boolean synced = false;
        boolean syncComplete = false;

        /**
         * The main EthereumJ callback.
         */
        EthereumListener listener = new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                synced = true;
            }

            @Override
            public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {
                ethNodes.put(channel.getNode(), statusMessage);
            }

            @Override
            public void onPeerAddedToSyncPool(Channel peer) {
                syncPeers.add(peer.getNode());
            }

            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                bestBlock = block;

                if (syncComplete) {
                    logger.info("New block: " + block.getShortDescr());
                }
            }
        };
    }

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig {

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
            props.overrideParams(ConfigFactory.parseResources(configPath.getValue()));
            if (firstRun.get() && resetDBOnFirstRun.getValue() != null) {
                props.setDatabaseReset(resetDBOnFirstRun.getValue());
            }
            return props;
        }
    }

    /**
     * This node doing nothing special, but by default as any other node will resend txs and new blocks
     */
    static class RegularNode extends BasicSample {
        public RegularNode() {
            // peers need different loggers
            super("sampleNode");
        }
    }

    private final static AtomicInteger fatalErrors = new AtomicInteger(0);

    private final static long MAX_RUN_MINUTES = 180L;

    private static ScheduledExecutorService statTimer =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(r, "StatTimer");
                }
            });

    private static boolean logStats() {
        testLogger.info("---------====---------");
        testLogger.info("fatalErrors: {}", fatalErrors);
        testLogger.info("---------====---------");

        return fatalErrors.get() == 0;
    }

    private static Integer getReferencedTrieNodes(final Source<byte[], byte[]> stateDS, final boolean includeAccounts,
                                                         byte[] ... roots) {
        final AtomicInteger ret = new AtomicInteger(0);
        SecureTrie trie = new SecureTrie(new SourceCodec.BytesKey<>(stateDS, Serializers.TrieNodeSerializer));
        for (byte[] root : roots) {
            trie.scanTree(root, new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, Value node) {
                    ret.incrementAndGet();
                }

                @Override
                public void doOnValue(byte[] nodeHash, Value node, byte[] key, byte[] value) {
                    if (includeAccounts) {
                        AccountState accountState = new AccountState(value);
                        if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                            ret.incrementAndGet();
                        }
                        if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                            ret.addAndGet(getReferencedTrieNodes(stateDS, false, accountState.getStateRoot()));
                        }
                    }
                }
            });
        }
        return ret.get();
    }

    private static void checkNodes(Ethereum ethereum, CommonConfig commonConfig) {
        try {
            Source<byte[], byte[]> stateDS = commonConfig.stateSource();
            byte[] stateRoot = ethereum.getBlockchain().getBestBlock().getHeader().getStateRoot();
            Integer rootsSize = getReferencedTrieNodes(stateDS, true, stateRoot);
            testLogger.info("Node validation successful");
            testLogger.info("Non-unique node size: {}", rootsSize);
        } catch (Exception ex) {
            testLogger.error("Node validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    private static void checkHeaders(Ethereum ethereum) {
        try {
            int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
            byte[] lastParentHash = null;
            testLogger.info("Checking headers from best block: {}", blockNumber);

            while (blockNumber >= 0) {
                Block currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                if (lastParentHash != null) {
                    assert FastByteComparisons.equal(currentBlock.getHash(), lastParentHash);
                }
                lastParentHash = currentBlock.getHeader().getParentHash();
                assert lastParentHash != null;
                blockNumber--;
            }

            testLogger.info("Checking headers successful, ended on block: {}", blockNumber + 1);
        } catch (Exception ex) {
            testLogger.error("Block header validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    private static void checkBlocks(Ethereum ethereum) {
        try {
            int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
            testLogger.info("Checking blocks from best block: {}", blockNumber);

            while (blockNumber > 0) {
                Block currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                // Validate uncles
                assert ((BlockchainImpl) ethereum.getBlockchain()).validateUncles(currentBlock);
                blockNumber--;
            }

            testLogger.info("Checking blocks successful, ended on block: {}", blockNumber + 1);
        } catch (Exception ex) {
            testLogger.error("Block validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    private static void checkTransactions(Ethereum ethereum) {
        try {
            int blockNumber = (int) ethereum.getBlockchain().getBestBlock().getHeader().getNumber();
            testLogger.info("Checking block transactions from best block: {}", blockNumber);

            while (blockNumber > 0) {
                Block currentBlock = ethereum.getBlockchain().getBlockByNumber(blockNumber);

                List<TransactionReceipt> receipts = new ArrayList<>();
                for (Transaction tx : currentBlock.getTransactionsList()) {
                    TransactionInfo txInfo = ((BlockchainImpl) ethereum.getBlockchain()).getTransactionInfo(tx.getHash());
                    assert txInfo != null;
                    receipts.add(txInfo.getReceipt());
                }

                Bloom logBloom = new Bloom();
                for (TransactionReceipt receipt : receipts) {
                    logBloom.or(receipt.getBloomFilter());
                }
                assert FastByteComparisons.equal(currentBlock.getLogBloom(), logBloom.getData());
                assert FastByteComparisons.equal(currentBlock.getReceiptsRoot(), calcReceiptsTrie(receipts));

                blockNumber--;
            }

            testLogger.info("Checking block transactions successful, ended on block: {}", blockNumber + 1);
        } catch (Exception ex) {
            testLogger.error("Block validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    private static void fullSanityCheck(Ethereum ethereum, CommonConfig commonConfig) {

        // nodes
        testLogger.info("Validating nodes: Start");
        checkNodes(ethereum, commonConfig);
        testLogger.info("Validating nodes: End");
        logStats();

        // headers
        testLogger.info("Validating block headers: Start");
        checkHeaders(ethereum);
        testLogger.info("Validating block headers: End");
        logStats();

        // blocks
        testLogger.info("Validating blocks: Start");
        checkBlocks(ethereum);
        testLogger.info("Validating blocks: End");
        logStats();

        // receipts
        testLogger.info("Validating transaction receipts: Start");
        checkTransactions(ethereum);
        testLogger.info("Validating transaction receipts: End");
        logStats();

        if (!firstRun.get()) {
            statTimer.shutdownNow();
        }

        firstRun.set(false);
    }

    @Test
    public void testDoubleCheck() throws Exception {

        runEthereum();

        new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                while(firstRun.get()) {
                    sleep(1000);
                }
                testLogger.info("Stopping first run");
                regularNode.close();
                testLogger.info("First run stopped");
                sleep(60_000);
                testLogger.info("Starting second run");
                runEthereum();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            }
        }).start();

        if(statTimer.awaitTermination(MAX_RUN_MINUTES, TimeUnit.MINUTES)) {
            logStats();
            // Checking error stats
            if (!logStats()) assert false;
        }
    }

    public void runEthereum() throws Exception {
        testLogger.info("Starting EthereumJ regular instance!");
        this.regularNode = EthereumFactory.createEthereum(RegularConfig.class);
    }
}
