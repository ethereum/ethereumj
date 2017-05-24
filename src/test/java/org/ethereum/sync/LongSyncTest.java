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
package org.ethereum.sync;

import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.*;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.eth.handler.Eth62;
import org.ethereum.net.eth.handler.EthHandler;
import org.ethereum.net.eth.message.*;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ethereum.util.FileUtil.recursiveDelete;
import static org.junit.Assert.fail;
import static org.spongycastle.util.encoders.Hex.decode;

/**
 * @author Mikhail Kalinin
 * @since 14.12.2015
 */
@Ignore("Long network tests")
public class LongSyncTest {

    private static Node nodeA;
    private static List<Block> mainB1B10;
    private static Block b10;

    private Ethereum ethereumA;
    private Ethereum ethereumB;
    private EthHandler ethA;
    private String testDbA;
    private String testDbB;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {

        nodeA = new Node("enode://3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6@localhost:30334");

        SysPropConfigA.props.overrideParams(
                "peer.listen.port", "30334",
                "peer.privateKey", "3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c",
                // nodeId: 3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6
                "genesis", "genesis-light-old.json"
        );
        SysPropConfigA.props.setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());

        SysPropConfigB.props.overrideParams(
                "peer.listen.port", "30335",
                "peer.privateKey", "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec",
                "genesis", "genesis-light-old.json",
                "sync.enabled", "true",
                "sync.max.hashes.ask", "3",
                "sync.max.blocks.ask", "2"
        );
        SysPropConfigB.props.setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());

        /*
         1  => ed1b6f07d738ad92c5bdc3b98fe25afea9c863dd351711776d9ce1ffb9e3d276
         2  => 43808666b662d131c6cff336a0d13608767ead9c9d5f181e95caa3597f3faf14
         3  => 1b5c231211f500bc73148dc9d9bdb9de2265465ba441a0db1790ba4b3f5f3e9c
         4  => db517e04399dbf5a65caf6b2572b3966c8f98a1d29b1e50dc8db51e54c15d83d
         5  => c42d6dbaa756eda7f4244a3507670d764232bd7068d43e6d8ef680c6920132f6
         6  => 604c92e8d16dafb64134210d521fcc85aec27452e75aedf708ac72d8240585d3
         7  => 3f51b0471eb345b1c5f3c6628e69744358ff81d3f64a3744bbb2edf2adbb0ebc
         8  => 62cfd04e29d941954e68ac8ca18ef5cd78b19809eaed860ae72589ebad53a21d
         9  => d32fc8e151f158d52fe0be6cba6d0b5c20793a00c4ad0d32db8ccd9269199a29
         10 => 22d8c1d909eb142ea0d69d0a38711874f98d6eef1bc669836da36f6b557e9564
         */
        mainB1B10 = loadBlocks("sync/main-b1-b10.dmp");

        b10 = mainB1B10.get(mainB1B10.size() - 1);
    }

    private static List<Block> loadBlocks(String path) throws URISyntaxException, IOException {

        URL url = ClassLoader.getSystemResource(path);
        File file = new File(url.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        List<Block> blocks = new ArrayList<>(strData.size());
        for (String rlp : strData) {
            blocks.add(new Block(decode(rlp)));
        }

        return blocks;
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

    @Before
    public void setupTest() throws InterruptedException {
        testDbA = "test_db_" + new BigInteger(32, new Random());
        testDbB = "test_db_" + new BigInteger(32, new Random());

        SysPropConfigA.props.setDataBaseDir(testDbA);
        SysPropConfigB.props.setDataBaseDir(testDbB);
    }

    @After
    public void cleanupTest() {
        recursiveDelete(testDbA);
        recursiveDelete(testDbB);
        SysPropConfigA.eth62 = null;
    }

    // general case, A has imported 10 blocks
    // expected: B downloads blocks from A => B synced
    @Test
    public void test1() throws InterruptedException {

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        semaphore.await(40, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // bodies validation: A doesn't send bodies for blocks lower than its best block
    // expected: B drops A
    @Test
    public void test2() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockBodies(GetBlockBodiesMessage msg) {
                List<byte[]> bodies = Arrays.asList(
                        mainB1B10.get(0).getEncodedBody()
                );

                BlockBodiesMessage response = new BlockBodiesMessage(bodies);
                sendMessage(response);
            }
        };

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // headers validation: headers count in A respond more than requested limit
    // expected: B drops A
    @Test
    public void test3() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (Arrays.equals(msg.getBlockIdentifier().getHash(), b10.getHash())) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Arrays.asList(
                        mainB1B10.get(0).getHeader(),
                        mainB1B10.get(1).getHeader(),
                        mainB1B10.get(2).getHeader(),
                        mainB1B10.get(3).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // headers validation: A sends empty response
    // expected: B drops A
    @Test
    public void test4() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (Arrays.equals(msg.getBlockIdentifier().getHash(), b10.getHash())) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Collections.emptyList();

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // headers validation: first header in response doesn't meet expectations
    // expected: B drops A
    @Test
    public void test5() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (Arrays.equals(msg.getBlockIdentifier().getHash(), b10.getHash())) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Arrays.asList(
                        mainB1B10.get(1).getHeader(),
                        mainB1B10.get(2).getHeader(),
                        mainB1B10.get(3).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // headers validation: first header in response doesn't meet expectations - second story
    // expected: B drops A
    @Test
    public void test6() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                List<BlockHeader> headers = Collections.singletonList(
                        mainB1B10.get(1).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        ethereumA = EthereumFactory.createEthereum(SysPropConfigA.props, SysPropConfigA.class);

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        // A == b10

        ethereumB = EthereumFactory.createEthereum(SysPropConfigB.props, SysPropConfigB.class);

        ethereumB.connect(nodeA);

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // headers validation: headers order is incorrect, reverse = false
    // expected: B drops A
    @Test
    public void test7() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (Arrays.equals(msg.getBlockIdentifier().getHash(), b10.getHash())) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Arrays.asList(
                        mainB1B10.get(0).getHeader(),
                        mainB1B10.get(2).getHeader(),
                        mainB1B10.get(1).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // headers validation: ancestor's parent hash and header's hash does not match, reverse = false
    // expected: B drops A
    @Test
    public void test8() throws InterruptedException {

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (Arrays.equals(msg.getBlockIdentifier().getHash(), b10.getHash())) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Arrays.asList(
                        mainB1B10.get(0).getHeader(),
                        new BlockHeader(new byte[32], new byte[32], new byte[32], new byte[32], new byte[32],
                                2, new byte[] {0}, 0, 0, new byte[0], new byte[0], new byte[0]),
                        mainB1B10.get(2).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        // A == b10, B == genesis

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    private void setupPeers() throws InterruptedException {
        setupPeers(b10);
    }

    private void setupPeers(Block best) throws InterruptedException {

        ethereumA = EthereumFactory.createEthereum(SysPropConfigA.class);

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        for (Block b : mainB1B10) {
            ImportResult result = blockchainA.tryToConnect(b);
            Assert.assertEquals(result, ImportResult.IMPORTED_BEST);
            if (b.equals(best)) break;
        }

        // A == best

        ethereumB = EthereumFactory.createEthereum(SysPropConfigB.props, SysPropConfigB.class);

        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {
                ethA = (EthHandler) channel.getEthHandler();
            }
        });

        final CountDownLatch semaphore = new CountDownLatch(1);

        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPeerAddedToSyncPool(Channel peer) {
                semaphore.countDown();
            }
        });

        ethereumB.connect(nodeA);

        semaphore.await(10, SECONDS);
        if(semaphore.getCount() > 0) {
            fail("Failed to set up peers");
        }
    }

    @Configuration
    @NoAutoscan
    public static class SysPropConfigA {
        static SystemProperties props = new SystemProperties();
        static Eth62 eth62 = null;

        @Bean
        public SystemProperties systemProperties() {
            return props;
        }

        @Bean
        @Scope("prototype")
        public Eth62 eth62() throws IllegalAccessException, InstantiationException {
            if (eth62 != null) return eth62;
            return new Eth62();
        }
    }

    @Configuration
    @NoAutoscan
    public static class SysPropConfigB {
        static SystemProperties props = new SystemProperties();

        @Bean
        public SystemProperties systemProperties() {
            return props;
        }
    }
}
