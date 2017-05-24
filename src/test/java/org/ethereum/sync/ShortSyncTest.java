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
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Blockchain;
import org.ethereum.core.TransactionReceipt;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
public class ShortSyncTest {

    private static BigInteger minDifficultyBackup;
    private static Node nodeA;
    private static List<Block> mainB1B10;
    private static List<Block> forkB1B5B8_;
    private static Block b10;
    private static Block b8_;

    private Ethereum ethereumA;
    private Ethereum ethereumB;
    private EthHandler ethA;
    private String testDbA;
    private String testDbB;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {

        SystemProperties.getDefault().setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());

        nodeA = new Node("enode://3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6@localhost:30334");

        SysPropConfigA.props.overrideParams(
                "peer.listen.port", "30334",
                "peer.privateKey", "3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c",
                // nodeId: 3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6
                "genesis", "genesis-light.json"
        );

        SysPropConfigB.props.overrideParams(
                "peer.listen.port", "30335",
                "peer.privateKey", "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec",
                "genesis", "genesis-light.json",
                "sync.enabled", "true"
        );

        mainB1B10 = loadBlocks("sync/main-b1-b10.dmp");
        forkB1B5B8_ = loadBlocks("sync/fork-b1-b5-b8_.dmp");

        b10 = mainB1B10.get(mainB1B10.size() - 1);
        b8_ = forkB1B5B8_.get(forkB1B5B8_.size() - 1);
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
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
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

    // positive gap, A on main, B on main
    // expected: B downloads missed blocks from A => B on main
    @Test
    public void test1() throws InterruptedException {

        setupPeers();

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

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

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // positive gap, A on fork, B on main
    // positive gap, A on fork, B on fork (same story)
    // expected: B downloads missed blocks from A => B on A's fork
    @Test
    public void test2() throws InterruptedException {

        setupPeers();

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }

        // A == b8', B == genesis

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b8_)) {
                    semaphore.countDown();
                }
            }
        });

        ethA.sendNewBlock(b8_);

        semaphore.await(10, SECONDS);

        // check if B == b8'
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // positive gap, A on main, B on fork
    // expected: B finds common ancestor and downloads missed blocks from A => B on main
    @Test
    public void test3() throws InterruptedException {

        setupPeers();

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        for (Block b : forkB1B5B8_) {
            blockchainB.tryToConnect(b);
        }

        // A == b10, B == b8'

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // negative gap, A on main, B on main
    // expected: B skips A's block as already imported => B on main
    @Test
    public void test4() throws InterruptedException {

        setupPeers();

        final Block b5 = mainB1B10.get(4);
        Block b9 = mainB1B10.get(8);

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
            if (b.isEqual(b5)) break;
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b9)) break;
        }

        // A == b5, B == b9

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        ethA.sendNewBlockHashes(b5);

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        // A == b10

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // negative gap, A on fork, B on main
    // negative gap, A on fork, B on fork (same story)
    // expected: B downloads A's fork and imports it as NOT_BEST => B on its chain
    @Test
    public void test5() throws InterruptedException {

        setupPeers();

        Block b9 = mainB1B10.get(8);

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b9)) break;
        }

        // A == b8', B == b9

        final CountDownLatch semaphore = new CountDownLatch(1);
        final CountDownLatch semaphoreB8_ = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
                if (block.isEqual(b8_)) {
                    semaphoreB8_.countDown();
                }
            }
        });

        ethA.sendNewBlockHashes(b8_);

        semaphoreB8_.await(10, SECONDS);
        if(semaphoreB8_.getCount() > 0) {
            fail("PeerB didn't import b8'");
        }

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        // A == b10

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // negative gap, A on main, B on fork
    // expected: B finds common ancestor and downloads A's blocks => B on main
    @Test
    public void test6() throws InterruptedException {

        setupPeers();

        final Block b7 = mainB1B10.get(6);

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
            if (b.isEqual(b7)) break;
        }

        for (Block b : forkB1B5B8_) {
            blockchainB.tryToConnect(b);
        }

        // A == b7, B == b8'

        final CountDownLatch semaphore = new CountDownLatch(1);
        final CountDownLatch semaphoreB7 = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b7)) {
                    semaphoreB7.countDown();
                }
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        ethA.sendNewBlockHashes(b7);

        semaphoreB7.await(10, SECONDS);

        // check if B == b7
        if(semaphoreB7.getCount() > 0) {
            fail("PeerB didn't recover a gap");
        }

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        // A == b10
        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // positive gap, A on fork, B on main
    // A does a re-branch to main
    // expected: B downloads main blocks from A => B on main
    @Test
    public void test7() throws InterruptedException {

        setupPeers();

        Block b4 = mainB1B10.get(3);

        // A == B == genesis

        final Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b4)) break;
        }

        // A == b8', B == b4

        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof NewBlockMessage) {
                    // it's time to do a re-branch
                    for (Block b : mainB1B10) {
                        blockchainA.tryToConnect(b);
                    }
                }
            }
        });

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        ethA.sendNewBlock(b8_);
        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // negative gap, A on fork, B on main
    // A does a re-branch to main
    // expected: B downloads A's fork and imports it as NOT_BEST => B on main
    @Test
    public void test8() throws InterruptedException {

        setupPeers();

        final Block b7_ = forkB1B5B8_.get(6);
        Block b8 = mainB1B10.get(7);

        // A == B == genesis

        final Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
            if (b.isEqual(b7_)) break;
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b8)) break;
        }

        // A == b7', B == b8

        final CountDownLatch semaphore = new CountDownLatch(1);
        final CountDownLatch semaphoreB7_ = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b7_)) {
                    // it's time to do a re-branch
                    for (Block b : mainB1B10) {
                        blockchainA.tryToConnect(b);
                    }

                    semaphoreB7_.countDown();
                }
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        ethA.sendNewBlockHashes(b7_);

        semaphoreB7_.await(10, SECONDS);
        if(semaphoreB7_.getCount() > 0) {
            fail("PeerB didn't import b7'");
        }

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // positive gap, A on fork, B on main
    // A doesn't send common ancestor
    // expected: B drops A and all its blocks => B on main
    @Test
    public void test9() throws InterruptedException {

        // handler which don't send an ancestor
        SysPropConfigA.eth62 = new Eth62() {
            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                // process init header request correctly
                if (msg.getMaxHeaders() == 1) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = new ArrayList<>();
                for (int i = 7; i < mainB1B10.size(); i++) {
                    headers.add(mainB1B10.get(i).getHeader());
                }

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }
        };

        setupPeers();

        Block b6 = mainB1B10.get(5);

        // A == B == genesis

        final Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b6)) break;
        }

        // A == b8', B == b6

        ethA.sendNewBlock(b8_);

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

        // back to usual handler
        SysPropConfigA.eth62 = null;

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        final CountDownLatch semaphoreConnect = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPeerAddedToSyncPool(Channel peer) {
                semaphoreConnect.countDown();
            }
        });
        ethereumB.connect(nodeA);

        // await connection
        semaphoreConnect.await(10, SECONDS);
        if(semaphoreConnect.getCount() > 0) {
            fail("PeerB is not able to connect to PeerA");
        }

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // negative gap, A on fork, B on main
    // A doesn't send the gap block in ancestor response
    // expected: B drops A and all its blocks => B on main
    @Test
    public void test10() throws InterruptedException {

        // handler which don't send a gap block
        SysPropConfigA.eth62 = new Eth62() {
            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (msg.getMaxHeaders() == 1) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = new ArrayList<>();
                for (int i = 0; i < forkB1B5B8_.size() - 1; i++) {
                    headers.add(forkB1B5B8_.get(i).getHeader());
                }

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }
        };

        setupPeers();

        Block b9 = mainB1B10.get(8);

        // A == B == genesis

        final Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b9)) break;
        }

        // A == b8', B == b9

        ethA.sendNewBlockHashes(b8_);

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

        // back to usual handler
        SysPropConfigA.eth62 = null;

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b10)) {
                    semaphore.countDown();
                }
            }
        });

        final CountDownLatch semaphoreConnect = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPeerAddedToSyncPool(Channel peer) {
                semaphoreConnect.countDown();
            }
        });
        ethereumB.connect(nodeA);

        // await connection
        semaphoreConnect.await(10, SECONDS);
        if(semaphoreConnect.getCount() > 0) {
            fail("PeerB is not able to connect to PeerA");
        }

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

        // A == b10

        ethA.sendNewBlock(b10);

        semaphore.await(10, SECONDS);

        // check if B == b10
        if(semaphore.getCount() > 0) {
            fail("PeerB bestBlock is incorrect");
        }
    }

    // A sends block with low TD to B
    // expected: B skips this block
    @Test
    public void test11() throws InterruptedException {

        Block b5 = mainB1B10.get(4);
        final Block b6_ = forkB1B5B8_.get(5);

        setupPeers();

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        final Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b5)) break;
        }

        // A == b8', B == b5

        final CountDownLatch semaphore1 = new CountDownLatch(1);
        final CountDownLatch semaphore2 = new CountDownLatch(1);
        ethereumB.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.isEqual(b6_)) {
                    if (semaphore1.getCount() > 0) {
                        semaphore1.countDown();
                    } else {
                        semaphore2.countDown();
                    }
                }
            }
        });

        ethA.sendNewBlock(b6_);
        semaphore1.await(10, SECONDS);

        if(semaphore1.getCount() > 0) {
            fail("PeerB doesn't accept block with higher TD");
        }

        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
        }

        // B == b10

        ethA.sendNewBlock(b6_);

        semaphore2.await(5, SECONDS);

        // check if B skips b6'
        if(semaphore2.getCount() == 0) {
            fail("PeerB doesn't skip block with lower TD");
        }
    }

    // bodies validation: A doesn't send bodies corresponding to headers which were sent previously
    // expected: B drops A
    @Test
    public void test12() throws InterruptedException {

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

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();

        for (Block b : mainB1B10) {
            blockchainA.tryToConnect(b);
        }

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

        ethA.sendNewBlock(b10);

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // bodies validation: headers order is incorrect in the response, reverse = true
    // expected: B drops A
    @Test
    public void test13() throws InterruptedException {

        Block b9 = mainB1B10.get(8);

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (msg.getMaxHeaders() == 1) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Arrays.asList(
                        forkB1B5B8_.get(7).getHeader(),
                        forkB1B5B8_.get(6).getHeader(),
                        forkB1B5B8_.get(4).getHeader(),
                        forkB1B5B8_.get(5).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }
        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b9)) break;
        }

        // A == b8', B == b10

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        ethA.sendNewBlockHashes(b8_);

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    // bodies validation: ancestor's parent hash and header's hash does not match, reverse = true
    // expected: B drops A
    @Test
    public void test14() throws InterruptedException {

        Block b9 = mainB1B10.get(8);

        SysPropConfigA.eth62 = new Eth62() {

            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {

                if (msg.getMaxHeaders() == 1) {
                    super.processGetBlockHeaders(msg);
                    return;
                }

                List<BlockHeader> headers = Arrays.asList(
                        forkB1B5B8_.get(7).getHeader(),
                        forkB1B5B8_.get(6).getHeader(),
                        new BlockHeader(new byte[32], new byte[32], new byte[32], new byte[32], new byte[32],
                                6, new byte[] {0}, 0, 0, new byte[0], new byte[0], new byte[0]),
                        forkB1B5B8_.get(4).getHeader()
                );

                BlockHeadersMessage response = new BlockHeadersMessage(headers);
                sendMessage(response);
            }

        };

        setupPeers();

        Blockchain blockchainA = (Blockchain) ethereumA.getBlockchain();
        Blockchain blockchainB = (Blockchain) ethereumB.getBlockchain();

        for (Block b : forkB1B5B8_) {
            blockchainA.tryToConnect(b);
        }
        for (Block b : mainB1B10) {
            blockchainB.tryToConnect(b);
            if (b.isEqual(b9)) break;
        }

        // A == b8', B == b10

        final CountDownLatch semaphoreDisconnect = new CountDownLatch(1);
        ethereumA.addListener(new EthereumListenerAdapter() {
            @Override
            public void onRecvMessage(Channel channel, Message message) {
                if (message instanceof DisconnectMessage) {
                    semaphoreDisconnect.countDown();
                }
            }
        });

        ethA.sendNewBlockHashes(b8_);

        semaphoreDisconnect.await(10, SECONDS);

        // check if peer was dropped
        if(semaphoreDisconnect.getCount() > 0) {
            fail("PeerA is not dropped");
        }
    }

    private void setupPeers() throws InterruptedException {

        ethereumA = EthereumFactory.createEthereum(SysPropConfigA.props, SysPropConfigA.class);
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
