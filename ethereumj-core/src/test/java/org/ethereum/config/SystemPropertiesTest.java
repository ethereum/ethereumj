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
package org.ethereum.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.ethereum.config.blockchain.OlympicConfig;
import org.ethereum.config.net.*;
import org.ethereum.core.AccountState;
import org.ethereum.core.Genesis;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.rlpx.Node;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Not thread safe - testUseOnlySprintConfig temporarily sets a static flag that may influence other tests.
 * Not thread safe - testGeneratedNodePrivateKey temporarily removes the nodeId.properties file which may influence other tests.
 */
@SuppressWarnings("ConstantConditions")
@NotThreadSafe
public class SystemPropertiesTest {
    private final static Logger logger = LoggerFactory.getLogger(SystemPropertiesTest.class);

    @Test
    public void testPunchBindIp() {
        SystemProperties.getDefault().overrideParams("peer.bind.ip", "");
        long st = System.currentTimeMillis();
        String ip = SystemProperties.getDefault().bindIp();
        long t = System.currentTimeMillis() - st;
        logger.info(ip + " in " + t + " msec");
        Assert.assertTrue(t < 10 * 1000);
        Assert.assertFalse(ip.isEmpty());
    }

    @Test
    public void testExternalIp() {
        SystemProperties.getDefault().overrideParams("peer.discovery.external.ip", "");
        long st = System.currentTimeMillis();
        String ip = SystemProperties.getDefault().externalIp();
        long t = System.currentTimeMillis() - st;
        logger.info(ip + " in " + t + " msec");
        Assert.assertTrue(t < 10 * 1000);
        Assert.assertFalse(ip.isEmpty());
    }

    @Test
    public void testExternalIpWhenSpecificallyConfigured() {
        SystemProperties props = SystemProperties.getDefault();
        props.overrideParams("peer.discovery.external.ip", "1.1.1.1");
        assertEquals("1.1.1.1", props.externalIp());

        props.overrideParams("peer.discovery.external.ip", "no_validation_rules_on_this_value");
        assertEquals("no_validation_rules_on_this_value", props.externalIp());
    }

    @Test
    public void testBlockchainNetConfig() {
        assertConfigNameResolvesToType("main", MainNetConfig.class);
        assertConfigNameResolvesToType("olympic", OlympicConfig.class);
        assertConfigNameResolvesToType("morden", MordenNetConfig.class);
        assertConfigNameResolvesToType("ropsten", RopstenNetConfig.class);
        assertConfigNameResolvesToType("testnet", TestNetConfig.class);
    }

    private <T extends BlockchainNetConfig> void assertConfigNameResolvesToType(String configName, Class<T> expectedConfigType) {
        SystemProperties props = new SystemProperties();
        props.overrideParams("blockchain.config.name", configName);
        BlockchainNetConfig blockchainConfig = props.getBlockchainConfig();
        assertTrue(blockchainConfig.getClass().isAssignableFrom(expectedConfigType));
    }

    @Test
    public void testConfigNamesAreCaseSensitive() {
        assertConfigNameIsUnsupported("mAin");
        assertConfigNameIsUnsupported("Main");
    }

    @Test
    public void testGarbageConfigNamesTriggerExceptions() {
        assertConfigNameIsUnsupported("\t");
        assertConfigNameIsUnsupported("\n");
        assertConfigNameIsUnsupported("");
        assertConfigNameIsUnsupported("fake");
    }

    private void assertConfigNameIsUnsupported(String configName) {
        try {
            SystemProperties props = new SystemProperties();
            props.overrideParams("blockchain.config.name", configName);
            props.getBlockchainConfig();
            fail("Should throw error for unsupported config name " + configName);
        } catch (Exception e) {
            assertEquals("Unknown value for 'blockchain.config.name': '" + configName + "'", e.getMessage());
        }
    }

    @Test
    public void testUseOnlySprintConfig() {
        boolean originalValue = SystemProperties.isUseOnlySpringConfig();

        try {
            SystemProperties.setUseOnlySpringConfig(false);
            assertNotNull(SystemProperties.getDefault());

            SystemProperties.setUseOnlySpringConfig(true);
            assertNull(SystemProperties.getDefault());
        } finally {
            SystemProperties.setUseOnlySpringConfig(originalValue);
        }
    }

    @Test
    public void testValidateMeAnnotatedGetters() {
        assertIncorrectValueTriggersConfigException("peer.discovery.enabled", "not_a_boolean");
        assertIncorrectValueTriggersConfigException("peer.discovery.persist", "not_a_boolean");
        assertIncorrectValueTriggersConfigException("peer.discovery.workers", "not_a_number");
        assertIncorrectValueTriggersConfigException("peer.discovery.touchPeriod", "not_a_number");
        assertIncorrectValueTriggersConfigException("peer.connection.timeout", "not_a_number");
        assertIncorrectValueTriggersConfigException("peer.p2p.version", "not_a_number");
        assertIncorrectValueTriggersConfigException("peer.p2p.framing.maxSize", "not_a_number");
        assertIncorrectValueTriggersConfigException("transaction.approve.timeout", "not_a_number");
        assertIncorrectValueTriggersConfigException("peer.discovery.ip.list", "not_a_ip");
        assertIncorrectValueTriggersConfigException("database.reset", "not_a_boolean");
        assertIncorrectValueTriggersConfigException("database.resetBlock", "not_a_number");
        assertIncorrectValueTriggersConfigException("database.prune.enabled", "not_a_boolean");
        assertIncorrectValueTriggersConfigException("database.resetBlock", "not_a_number");
    }

    private void assertIncorrectValueTriggersConfigException(String... keyValuePairs) {
        try {
            new SystemProperties().overrideParams(keyValuePairs);
            fail("Should've thrown ConfigException");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void testDatabasePruneShouldAdhereToMax() {
        SystemProperties props = new SystemProperties();
        props.overrideParams("database.prune.enabled", "false");
        assertEquals("When database.prune.maxDepth is not set, defaults to -1", -1, props.databasePruneDepth());

        props.overrideParams("database.prune.enabled", "true", "database.prune.maxDepth", "42");
        assertEquals(42, props.databasePruneDepth());
    }

    @Test
    public void testRequireEitherNameOrClassConfiguration() {
        try {
            SystemProperties props = new SystemProperties();
            props.overrideParams("blockchain.config.name", "test", "blockchain.config.class", "org.ethereum.config.net.TestNetConfig");
            props.getBlockchainConfig();
            fail("Should've thrown exception because not 'Only one of two options should be defined'");
        } catch (RuntimeException e) {
            assertEquals("Only one of two options should be defined: 'blockchain.config.name' and 'blockchain.config.class'", e.getMessage());
        }
    }

    @Test
    public void testRequireTypeBlockchainNetConfigOnManualClass() {
        SystemProperties props = new SystemProperties();
        props.overrideParams("blockchain.config.name", null, "blockchain.config.class", "org.ethereum.config.net.TestNetConfig");
        assertTrue(props.getBlockchainConfig().getClass().isAssignableFrom(TestNetConfig.class));
    }

    @Test
    public void testNonExistentBlockchainNetConfigClass() {
        SystemProperties props = new SystemProperties();
        try {
            props.overrideParams("blockchain.config.name", null, "blockchain.config.class", "org.ethereum.config.net.NotExistsConfig");
            props.getBlockchainConfig();
            fail("Should throw exception for invalid class");
        } catch (RuntimeException expected) {
            assertEquals("The class specified via blockchain.config.class 'org.ethereum.config.net.NotExistsConfig' not found", expected.getMessage());
        }
    }

    @Test
    public void testNotInstanceOfBlockchainForkConfig() {
        SystemProperties props = new SystemProperties(ConfigFactory.empty(), getClass().getClassLoader());
        try {
            props.overrideParams("blockchain.config.name", null, "blockchain.config.class", "org.ethereum.config.NodeFilter");
            props.getBlockchainConfig();
            fail("Should throw exception for invalid class");
        } catch (RuntimeException expected) {
            assertEquals("The class specified via blockchain.config.class 'org.ethereum.config.NodeFilter' is not instance of org.ethereum.config.BlockchainForkConfig", expected.getMessage());
        }
    }

    @Test
    public void testEmptyListOnEmptyPeerActiveConfiguration() {
        SystemProperties props = new SystemProperties();
        props.overrideParams("peer.active", null);
        assertEquals(newArrayList(), props.peerActive());
    }

    @Test
    public void testPeerActive() {
        ActivePeer node1 = ActivePeer.asEnodeUrl("node-1", "1.1.1.1");
        ActivePeer node2 = ActivePeer.asNode("node-2", "2.2.2.2");
        Config config = createActivePeersConfig(node1, node2);

        SystemProperties props = new SystemProperties();
        props.overrideParams(config);

        List<Node> activePeers = props.peerActive();
        assertEquals(2, activePeers.size());
    }

    @Test
    public void testRequire64CharsNodeId() {
        assertInvalidNodeId(RandomStringUtils.randomAlphanumeric(63));
        assertInvalidNodeId(RandomStringUtils.randomAlphanumeric(1));
        assertInvalidNodeId(RandomStringUtils.randomAlphanumeric(65));
    }

    private void assertInvalidNodeId(String nodeId) {
        String hexEncodedNodeId = Hex.toHexString(nodeId.getBytes());

        ActivePeer nodeWithInvalidNodeId = ActivePeer.asNodeWithId("node-1", "1.1.1.1", hexEncodedNodeId);
        Config config = createActivePeersConfig(nodeWithInvalidNodeId);

        SystemProperties props = new SystemProperties();
        try {
            props.overrideParams(config);
            fail("Should've thrown exception for invalid node id");
        } catch (RuntimeException ignore) { }
    }

    @Test
    public void testUnexpectedElementInNodeConfigThrowsException() {
        String nodeWithUnexpectedElement = "peer = {" +
                "active = [{\n" +
                "  port = 30303\n" +
                "  nodeName = Test\n" +
                "  unexpectedElement = 12345\n" +
                "}]}";

        Config invalidConfig = ConfigFactory.parseString(nodeWithUnexpectedElement);

        SystemProperties props = new SystemProperties();
        try {
            props.overrideParams(invalidConfig);
        } catch (RuntimeException ignore) { }
    }

    @Test
    public void testActivePeersUsingNodeName() {
        ActivePeer node = ActivePeer.asNodeWithName("node-1", "1.1.1.1", "peer-1");
        Config config = createActivePeersConfig(node);

        SystemProperties props = new SystemProperties();
        props.overrideParams(config);

        List<Node> activePeers = props.peerActive();
        assertEquals(1, activePeers.size());

        Node peer = activePeers.get(0);
        String expectedKeccak512HashOfNodeName = "fcaf073315aa0fe284dd6d76200ede5cc9277f3cb1fd7649ddab3b6a61e96ee91e957" +
                "0b14932be6d6cd837027d50d9521923962909e5a9fdcdcabc3fe29408bb";
        String actualHexEncodedId = Hex.toHexString(peer.getId());
        assertEquals(expectedKeccak512HashOfNodeName, actualHexEncodedId);
    }

    @Test
    public void testRequireEitherNodeNameOrNodeId() {
        ActivePeer node = ActivePeer.asNodeWithName("node-1", "1.1.1.1", null);
        Config config = createActivePeersConfig(node);

        SystemProperties props = new SystemProperties();
        try {
            props.overrideParams(config);
            fail("Should require either nodeName or nodeId");
        } catch (RuntimeException ignore) { }
    }

    private static Config createActivePeersConfig(ActivePeer... activePeers) {
        StringBuilder config = new StringBuilder("peer = {");
        config.append("   active =[");

        for (int i = 0; i < activePeers.length; i++) {
            ActivePeer activePeer = activePeers[i];
            config.append(activePeer.toString());
            if (i < activePeers.length - 1) {
                config.append(",");
            }
        }

        config.append("   ]");
        config.append("}");

        return ConfigFactory.parseString(config.toString());
    }

    @Test
    public void testPeerTrusted() throws Exception{
        TrustedPeer peer1 = TrustedPeer.asNode("node-1", "1.1.1.1");
        TrustedPeer peer2 = TrustedPeer.asNode("node-2", "2.1.1.*");
        TrustedPeer peer3 = TrustedPeer.asNode("node-2", "3.*");
        Config config = createTrustedPeersConfig(peer1, peer2, peer3);

        SystemProperties props = new SystemProperties();
        props.overrideParams(config);

        NodeFilter filter = props.peerTrusted();
        assertTrue(filter.accept(InetAddress.getByName("1.1.1.1")));
        assertTrue(filter.accept(InetAddress.getByName("2.1.1.1")));
        assertTrue(filter.accept(InetAddress.getByName("2.1.1.9")));
        assertTrue(filter.accept(InetAddress.getByName("3.1.1.1")));
        assertTrue(filter.accept(InetAddress.getByName("3.1.1.9")));
        assertTrue(filter.accept(InetAddress.getByName("3.9.1.9")));
        assertFalse(filter.accept(InetAddress.getByName("4.1.1.1")));
    }

    private static Config createTrustedPeersConfig(TrustedPeer... trustedPeers) {
        StringBuilder config = new StringBuilder("peer = {");
        config.append("   trusted =[");

        for (int i = 0; i < trustedPeers.length; i++) {
            TrustedPeer activePeer = trustedPeers[i];
            config.append(activePeer.toString());
            if (i < trustedPeers.length - 1) {
                config.append(",");
            }
        }

        config.append("   ]");
        config.append("}");

        return ConfigFactory.parseString(config.toString());
    }

    @Test
    public void testRequire64CharsPrivateKey() {
        assertInvalidPrivateKey(RandomUtils.nextBytes(1));
        assertInvalidPrivateKey(RandomUtils.nextBytes(31));
        assertInvalidPrivateKey(RandomUtils.nextBytes(33));
        assertInvalidPrivateKey(RandomUtils.nextBytes(64));
        assertInvalidPrivateKey(RandomUtils.nextBytes(0));

        String validPrivateKey = Hex.toHexString(RandomUtils.nextBytes(32));
        SystemProperties props = new SystemProperties();
        props.overrideParams("peer.privateKey", validPrivateKey);
        assertEquals(validPrivateKey, props.privateKey());
    }

    private void assertInvalidPrivateKey(byte[] privateKey) {
        String hexEncodedPrivateKey = Hex.toHexString(privateKey);

        SystemProperties props = new SystemProperties();
        try {
            props.overrideParams("peer.privateKey", hexEncodedPrivateKey);
            props.privateKey();
            fail("Should've thrown exception for invalid private key");
        } catch (RuntimeException ignore) { }
    }

    @Ignore
    @Test
    public void testExposeBugWhereNonHexEncodedIsAcceptedWithoutValidation() {
        SystemProperties props = new SystemProperties();
        String nonHexEncoded = RandomStringUtils.randomAlphanumeric(64);
        try {
            props.overrideParams("peer.privateKey", nonHexEncoded);
            props.privateKey();
            fail("Should've thrown exception for invalid private key");
        } catch (RuntimeException ignore) { }
    }

    /**
     * TODO: Consider using a strategy interface for #getGeneratedNodePrivateKey().
     * Anything 'File' and 'random' generation are difficult to test and assert
     */
    @Test
    public void testGeneratedNodePrivateKeyThroughECKey() throws Exception {
        File outputFile = new File("database-test/nodeId.properties");
        //noinspection ResultOfMethodCallIgnored
        outputFile.delete();

        SystemProperties props = new SystemProperties();
        props.privateKey();

        assertTrue(outputFile.exists());
        String contents = FileCopyUtils.copyToString(new FileReader(outputFile));
        String[] lines = StringUtils.tokenizeToStringArray(contents, "\n");
        assertEquals(4, lines.length);
        assertTrue(lines[0].startsWith("#Generated NodeID."));
        assertTrue(lines[1].startsWith("#"));
        assertTrue(lines[2].startsWith("nodeIdPrivateKey="));
        assertEquals("nodeIdPrivateKey=".length() + 64, lines[2].length());
        assertTrue(lines[3].startsWith("nodeId="));
        assertEquals("nodeId=".length() + 128, lines[3].length());
    }

    @Test
    public void testFastSyncPivotBlockHash() {
        SystemProperties props = new SystemProperties();
        assertNull(props.getFastSyncPivotBlockHash());

        byte[] validPivotBlockHash = RandomUtils.nextBytes(32);
        props.overrideParams("sync.fast.pivotBlockHash", Hex.toHexString(validPivotBlockHash));
        assertTrue(Arrays.equals(validPivotBlockHash, props.getFastSyncPivotBlockHash()));

        assertInvalidPivotBlockHash(RandomUtils.nextBytes(0));
        assertInvalidPivotBlockHash(RandomUtils.nextBytes(1));
        assertInvalidPivotBlockHash(RandomUtils.nextBytes(31));
        assertInvalidPivotBlockHash(RandomUtils.nextBytes(33));
    }

    private void assertInvalidPivotBlockHash(byte[] pivotBlockHash) {
        String hexEncodedPrivateKey = Hex.toHexString(pivotBlockHash);

        SystemProperties props = new SystemProperties();
        try {
            props.overrideParams("sync.fast.pivotBlockHash", hexEncodedPrivateKey);
            props.getFastSyncPivotBlockHash();
            fail("Should've thrown exception for invalid private key");
        } catch (RuntimeException ignore) { }
    }

    @Test
    public void testUseGenesis() throws IOException {
        BigInteger mordenInitialNonse = BigInteger.valueOf(0x100000);
        SystemProperties props = new SystemProperties() {
            @Override
            public BlockchainNetConfig getBlockchainConfig() {
                return new MordenNetConfig();
            }
        };
        Resource sampleGenesisBlock = new ClassPathResource("/config/genesis-sample.json");
        Genesis genesis = props.useGenesis(sampleGenesisBlock.getInputStream());

        /*
         * Assert that MordenNetConfig is used when generating the
         * premine state.
         */
        Map<ByteArrayWrapper, Genesis.PremineAccount> premine = genesis.getPremine();
        assertEquals(1, premine.size());

        Genesis.PremineAccount account = premine.values().iterator().next();
        AccountState state = account.accountState;
        assertEquals(state.getNonce(), mordenInitialNonse);
        //noinspection SpellCheckingInspection
        assertEquals("#0 (4addb5 <~ 000000) Txs:0, Unc: 0", genesis.getShortDescr());
    }

    @Test
    public void testDump() {
        SystemProperties props = new SystemProperties();
        /*
         * No intend to test TypeSafe's render functionality. Perform
         * some high-level asserts to verify that:
         * - it's probably a config
         * - it's probably fairly sized
         * - it didn't break
         */
        String dump = props.dump().trim();
        assertTrue(dump.startsWith("{"));
        assertTrue(dump.endsWith("}"));
        assertTrue(dump.length() > 5 * 1024);
        assertTrue(StringUtils.countOccurrencesOf(dump, "{") > 50);
        assertTrue(StringUtils.countOccurrencesOf(dump, "{") > 50);
    }

    @SuppressWarnings("SameParameterValue")
    static class ActivePeer {
        boolean asEnodeUrl;
        String node;
        String host;
        String nodeId;
        String nodeName;

        static ActivePeer asEnodeUrl(String node, String host) {
            return new ActivePeer(true, node, host, "e437a4836b77ad9d9ffe73ee782ef2614e6d8370fcf62191a6e488276e23717147073a7ce0b444d485fff5a0c34c4577251a7a990cf80d8542e21b95aa8c5e6c", null);
        }

        static ActivePeer asNode(String node, String host) {
            return asNodeWithId(node, host, "e437a4836b77ad9d9ffe73ee782ef2614e6d8370fcf62191a6e488276e23717147073a7ce0b444d485fff5a0c34c4577251a7a990cf80d8542e21b95aa8c5e6c");
        }

        static ActivePeer asNodeWithId(String node, String host, String nodeId) {
            return new ActivePeer(false, node, host, nodeId, null);
        }

        static ActivePeer asNodeWithName(String node, String host, String name) {
            return new ActivePeer(false, node, host, null, name);
        }

        private ActivePeer(boolean asEnodeUrl, String node, String host, String nodeId, String nodeName) {
            this.asEnodeUrl = asEnodeUrl;
            this.node = node;
            this.host = host;
            this.nodeId = nodeId;
            this.nodeName = nodeName;
        }

        public String toString() {
            String hexEncodedNode = Hex.toHexString(node.getBytes());
            if (asEnodeUrl) {
                return "{\n" +
                        "  url = \"enode://" + hexEncodedNode + "@" + host + ".com:30303\" \n" +
                        "}";
            }

            if (StringUtils.hasText(nodeName)) {
                return "{\n" +
                        "  ip = " + host + "\n" +
                        "  port = 30303\n" +
                        "  nodeName = " + nodeName + "\n" +
                        "}\n";
            }

            return "{\n" +
                    "  ip = " + host + "\n" +
                    "  port = 30303\n" +
                    "  nodeId = " + nodeId + "\n" +
                    "}\n";
        }
    }

    @SuppressWarnings("SameParameterValue")
    static class TrustedPeer {
        String ip;
        String nodeId;

        static TrustedPeer asNode(String nodeId, String ipPattern) {
            return new TrustedPeer(nodeId, ipPattern);
        }

        private TrustedPeer(String nodeId, String ipPattern) {
            this.ip = ipPattern;
            this.nodeId = Hex.toHexString(nodeId.getBytes());
        }

        public String toString() {
            return "{\n" +
                    "  ip = \"" + ip + "\"\n" +
                    "  nodeId = " + nodeId + "\n" +
                    "}\n";
        }
    }
}
