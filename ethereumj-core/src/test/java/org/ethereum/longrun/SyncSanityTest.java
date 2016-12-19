package org.ethereum.longrun;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.mutable.MutableObject;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Sync with sanity check
 *
 * Runs sync with defined config
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
public class SyncSanityTest {

    private Ethereum regularNode;
    private static AtomicBoolean firstRun = new AtomicBoolean(true);
    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");
    private static final MutableObject<String> configPath = new MutableObject<>("longrun/conf/ropsten.conf");
    private static final MutableObject<Boolean> resetDBOnFirstRun = new MutableObject<>(null);
    private static final AtomicBoolean allChecksAreOver =  new AtomicBoolean(false);

    public SyncSanityTest() throws Exception {

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
                    SyncSanityTest.testLogger.error("Unhandled exception", t);
                }
            }
        }, 0, 15, TimeUnit.SECONDS);
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
     * Just regular EthereumJ node
     */
    static class RegularNode extends BasicNode {
        public RegularNode() {
            // peers need different loggers
            super("sampleNode");
        }

        @Override
        public void onSyncDone() throws Exception {
            // Full sanity check
            fullSanityCheck(ethereum, commonConfig);
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

    private static void fullSanityCheck(Ethereum ethereum, CommonConfig commonConfig) {

        // nodes
        testLogger.info("Validating nodes: Start");
        BlockchainValidation.checkNodes(ethereum, commonConfig, fatalErrors);
        testLogger.info("Validating nodes: End");
        logStats();

        // headers
        testLogger.info("Validating block headers: Start");
        BlockchainValidation.checkHeaders(ethereum, fatalErrors);
        testLogger.info("Validating block headers: End");
        logStats();

        // blocks
        testLogger.info("Validating blocks: Start");
        BlockchainValidation.checkBlocks(ethereum, fatalErrors);
        testLogger.info("Validating blocks: End");
        logStats();

        // receipts
        testLogger.info("Validating transaction receipts: Start");
        BlockchainValidation.checkTransactions(ethereum, fatalErrors);
        testLogger.info("Validating transaction receipts: End");
        logStats();

        if (!firstRun.get()) {
            allChecksAreOver.set(true);
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
            // Checking for errors
            assert allChecksAreOver.get();
            if (!logStats()) assert false;
        }
    }

    public void runEthereum() throws Exception {
        testLogger.info("Starting EthereumJ regular instance!");
        this.regularNode = EthereumFactory.createEthereum(RegularConfig.class);
    }
}
