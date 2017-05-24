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
 * Fast sync with sanity check
 *
 * Runs sync with defined config. Stops when all nodes are downloaded in 1 minute.
 * - checks State Trie is not broken
 * Restarts, waits until SECURE sync state (all headers are downloaded) in 1 minute.
 * - checks block headers
 * Restarts, waits until full sync is over
 * - checks nodes/headers/blocks/tx receipts
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
    private static AtomicBoolean secondRun = new AtomicBoolean(true);
    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");
    private static final MutableObject<String> configPath = new MutableObject<>("longrun/conf/ropsten-fast.conf");
    private static final MutableObject<Boolean> resetDBOnFirstRun = new MutableObject<>(null);
    private static final AtomicBoolean allChecksAreOver =  new AtomicBoolean(false);

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
            super("sampleNode");
        }

        private void stopSync() {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
            ethereum.getChannelManager().close();
            syncPool.close();
        }

        @Override
        public void waitForSync() throws Exception {
            testLogger.info("Waiting for the complete blockchain sync (will take up to an hour on fast sync for the whole chain)...");
            while(true) {
                sleep(10000);
                if (syncState == null) continue;

                switch (syncState) {
                    case UNSECURE:
                        if (!firstRun.get()) break;
                        testLogger.info("[v] Unsecure sync completed");
                        sleep(60000);
                        stopSync();
                        BlockchainValidation.checkNodes(ethereum, commonConfig, fatalErrors);
                        firstRun.set(false);
                        break;
                    case SECURE:
                        if (!secondRun.get()) break;
                        testLogger.info("[v] Secure sync completed");
                        sleep(60000);
                        stopSync();
                        BlockchainValidation.checkFastHeaders(ethereum, commonConfig, fatalErrors);
                        secondRun.set(false);
                        break;
                    case COMPLETE:
                        testLogger.info("[v] Sync complete! The best block: " + bestBlock.getShortDescr());
                        stopSync();
                        return;
                }
            }
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
        BlockchainValidation.fullCheck(ethereum, commonConfig, fatalErrors);
        logStats();
        allChecksAreOver.set(true);
        statTimer.shutdownNow();
    }

    @Test
    public void testTripleCheck() throws Exception {

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
                sleep(10_000);
                testLogger.info("Starting second run");
                runEthereum();
                while(secondRun.get()) {
                    sleep(1000);
                }
                testLogger.info("Stopping second run");
                regularNode.close();
                testLogger.info("Second run stopped");
                sleep(10_000);
                testLogger.info("Starting third run");
                runEthereum();
                while(!allChecksAreOver.get()) {
                    sleep(1000);
                }
                testLogger.info("Stopping third run");
                regularNode.close();
                testLogger.info("All checks are finished");
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
