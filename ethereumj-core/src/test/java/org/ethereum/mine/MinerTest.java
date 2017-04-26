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
package org.ethereum.mine;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.eth.handler.Eth62;
import org.ethereum.net.eth.message.*;
import org.ethereum.util.ByteUtil;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Long running test
 *
 * Creates an instance
 *
 * Created by Anton Nashatyrev on 13.10.2015.
 */
@Ignore
public class MinerTest {

    @Configuration
    @NoAutoscan
    public static class SysPropConfig1 {
        static Eth62 testHandler = null;

        static SystemProperties props = new SystemProperties();;
        @Bean
        public SystemProperties systemProperties() {
            return props;
        }
    }

    @Configuration
    @NoAutoscan
    public static class SysPropConfig2 {
        static Eth62 testHandler = null;

        static SystemProperties props = new SystemProperties();;
        @Bean
        public SystemProperties systemProperties() {
            return props;
        }
    }

    @BeforeClass
    public static void setup() {
//        Constants.MINIMUM_DIFFICULTY = BigInteger.valueOf(1);
    }

    @Test
    public void startMiningConsumer() throws Exception {
        SysPropConfig2.props.overrideParams(ConfigFactory.parseString(
                "peer.listen.port = 30336 \n" +
                        "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                        "peer.networkId = 555 \n" +
                        "peer.active = [" +
                        "{ url = \"enode://b23b3b9f38f1d314b27e63c63f3e45a6ea5f82c83f282e2d38f2f01c22165e897656fe2e5f9f18616b81f41cbcf2e9100fc9f8dad099574f3d84cf9623de2fc9@localhost:20301\" }," +
                        "{ url = \"enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335\" }" +
                        "] \n" +
                        "sync.enabled = true \n" +
                        "genesis = genesis-harder.json \n" +
//                        "genesis = frontier.json \n" +
                        "database.dir = testDB-1 \n"));

        Ethereum ethereum2 = EthereumFactory.createEthereum(SysPropConfig2.props, SysPropConfig2.class);

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereum2.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                System.err.println("=== New block: " + blockInfo(block));
                System.err.println(block);

                for (Transaction tx : block.getTransactionsList()) {
//                    Pair<Transaction, Long> remove = submittedTxs.remove(new ByteArrayWrapper(tx.getHash()));
//                    if (remove == null) {
//                        System.err.println("===== !!! Unknown Tx: " + tx);
//                    } else {
//                        System.out.println("===== Tx included in " + (System.currentTimeMillis() - remove.getRight()) / 1000
//                                + " sec: " + tx);
//                    }

                }

//                for (Pair<Transaction, Long> pair : submittedTxs.values()) {
//                    if (System.currentTimeMillis() - pair.getRight() > 60 * 1000) {
//                        System.err.println("==== !!! Lost Tx: " + (System.currentTimeMillis() - pair.getRight()) / 1000
//                                + " sec: " + pair.getLeft());
//                    }
//                }
            }

            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                System.err.println("=== Tx: " + transactions);
            }

            @Override
            public void onSyncDone(SyncState state) {
                semaphore.countDown();
                System.err.println("=== Sync Done!");
            }
        });

        System.out.println("Waiting for sync...");
        semaphore.await();

//        ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
//        byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");
        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");

        for (int i = ethereum2.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 200; i++, j++) {
            {
                Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                        receiverAddr, new byte[]{77}, new byte[0]);
                tx.sign(senderKey);
                System.out.println("=== Submitting tx: " + tx);
                ethereum2.submitTransaction(tx);

                submittedTxs.put(new ByteArrayWrapper(tx.getHash()), Pair.of(tx, System.currentTimeMillis()));
            }
            Thread.sleep(7000);
        }

        Thread.sleep(100000000L);
    }


    Map<ByteArrayWrapper, Pair<Transaction, Long>> submittedTxs = Collections.synchronizedMap(
            new HashMap<ByteArrayWrapper, Pair<Transaction, Long>>());

    static String blockInfo(Block b) {
        boolean ours = Hex.toHexString(b.getExtraData()).startsWith("cccccccccc");
        String txs = "Tx[";
        for (Transaction tx : b.getTransactionsList()) {
            txs += ByteUtil.byteArrayToLong(tx.getNonce()) + ", ";
        }
        txs = txs.substring(0, txs.length() - 2) + "]";
        return (ours ? "##" : "  ") + b.getShortDescr() + " " + txs;
    }

    @Test
    public void startMiningTest() throws FileNotFoundException, InterruptedException {
        SysPropConfig1.props.overrideParams(ConfigFactory.parseString(
                "peer.listen.port = 30335 \n" +
                        "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                        "peer.networkId = 555 \n" +
                        "peer.active = [{ url = \"enode://b23b3b9f38f1d314b27e63c63f3e45a6ea5f82c83f282e2d38f2f01c22165e897656fe2e5f9f18616b81f41cbcf2e9100fc9f8dad099574f3d84cf9623de2fc9@localhost:20301\" }] \n" +
                        "sync.enabled = true \n" +
                        "genesis = genesis-harder.json \n" +
//                        "genesis = frontier.json \n" +
                        "database.dir = testDB-2 \n" +
                        "mine.extraDataHex = cccccccccccccccccccc \n" +
                        "mine.cpuMineThreads = 2"));

//        SysPropConfig2.props.overrideParams(ConfigFactory.parseString(
//                "peer.listen.port = 30336 \n" +
//                        "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
//                        "peer.networkId = 555 \n" +
//                        "peer.active = [{ url = \"enode://b23b3b9f38f1d314b27e63c63f3e45a6ea5f82c83f282e2d38f2f01c22165e897656fe2e5f9f18616b81f41cbcf2e9100fc9f8dad099574f3d84cf9623de2fc9@localhost:20301\" }] \n" +
//                        "sync.enabled = true \n" +
//                        "genesis = genesis-light.json \n" +
////                        "genesis = frontier.json \n" +
//                        "database.dir = testDB-1 \n"));

        Ethereum ethereum1 = EthereumFactory.createEthereum(SysPropConfig1.props, SysPropConfig1.class);
//        Ethereum ethereum2 = EthereumFactory.createEthereum(SysPropConfig2.props, SysPropConfig2.class);

        final CountDownLatch semaphore = new CountDownLatch(1);
        ethereum1.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                System.out.println("=== New block: " + blockInfo(block));
            }

            @Override
            public void onSyncDone(SyncState state) {
                semaphore.countDown();
            }
        });

//        ethereum2.addListener(new EthereumListenerAdapter() {
//            @Override
//            public void onBlock(Block block, List<TransactionReceipt> receipts) {
//                System.err.println("=== New block: " + block);
//            }
//
//            @Override
//            public void onPendingStateChanged(List<Transaction> transactions) {
//                System.err.println("=== Tx: " + transactions);
//            }
//        });

        System.out.println("=== Waiting for sync ...");
        semaphore.await(600, TimeUnit.SECONDS);
        System.out.println("=== Sync done.");


        BlockMiner blockMiner = ethereum1.getBlockMiner();
        blockMiner.addListener(new MinerListener() {
            @Override
            public void miningStarted() {
                System.out.println("=== MinerTest.miningStarted");
            }

            @Override
            public void miningStopped() {
                System.out.println("=== MinerTest.miningStopped");
            }

            @Override
            public void blockMiningStarted(Block block) {
                System.out.println("=== MinerTest.blockMiningStarted " + blockInfo(block));
            }

            @Override
            public void blockMined(Block block) {
//                boolean validate = Ethash.getForBlock(block.getNumber()).validate(block.getHeader());
                System.out.println("=== MinerTest.blockMined " + blockInfo(block));
//                System.out.println("=== MinerTest.blockMined: " + validate);
            }

            @Override
            public void blockMiningCanceled(Block block) {
                System.out.println("=== MinerTest.blockMiningCanceled " + blockInfo(block));
            }
        });
        Ethash.fileCacheEnabled = true;
        blockMiner.setFullMining(true);
        blockMiner.startMining();

//        System.out.println("======= Waiting for block #4");
//        semaphore.await(60, TimeUnit.SECONDS);
//        if(semaphore.getCount() > 0) {
//            throw new RuntimeException("4 blocks were not imported.");
//        }
//
//        System.out.println("======= Sending forked block without parent...");


        ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
        byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");

        for (int i = ethereum1.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
            {
                Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                        receiverAddr, new byte[]{77}, new byte[0]);
                tx.sign(senderKey);
                System.out.println("=== Submitting tx: " + tx);
                ethereum1.submitTransaction(tx);

                submittedTxs.put(new ByteArrayWrapper(tx.getHash()), Pair.of(tx, System.currentTimeMillis()));
            }
            Thread.sleep(5000);
        }

        Thread.sleep(1000000000);

        ethereum1.close();

        System.out.println("Passed.");

    }

    public static void main(String[] args) throws Exception {
        ECKey k = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        System.out.println(Hex.toHexString(k.getPrivKeyBytes()));
        System.out.println(Hex.toHexString(k.getAddress()));
        System.out.println(Hex.toHexString(k.getNodeId()));
    }

    @Test
    public void blockTest() {
        String rlp = "f90498f90490f90217a0887405e8cf98cfdbbf5ab4521d45a0803e397af61852d94dc46ca077787088bfa0d6d234f05ac90931822b7b6f244cef81" +
                "83e5dd3080483273d6c2fcc02399216294ee0250c19ad59305b2bdb61f34b45b72fe37154fa0caa558a47a66b975426c6b963c46bb83d969787cfedc09fd2cab8ab83155568da07c970ab3f2004e2aa0" +
                "7d7b3dda348a3a4f8f4ab98b3504c46c1ffae09ef5bd23a077007d3a4c7c88823e9f80b1ba48ec74f88f40e9ec9c5036235fc320fe8a29ffb90100000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008311fc6782" +
                "02868508cc8cac48825208845682c3479ad983010302844765746887676f312e352e318777696e646f7773a020269a7f434e365b012659d1b190aa1586c22d7bbf0ed7fad73ca7d2b60f623c8841c814" +
                "3ae845fb3df867f8656e850ba43b7400830fffff9431e2e1ed11951c7091dfba62cd4b7145e947219c4d801ba0ad82686ee482723f88d9760b773e7b8234f126271e53369b928af0cbab302baea04fe3" +
                "dd2e0dbcc5d53123fe49f3515f0844c7e4c6dd3752f0cf916f4bb5cbe80bf9020af90207a08752c2c47c96537cf695bdecc9696a8ea35b6bfdc1389a134b47ad63fea38c2ca01dcc4de8dec75d7aab85" +
                "b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a05bff1dc620da4d3a123f8e08536434071281d64fc106105fb3bc94b6b1b8913ba0b59542" +
                "42bb4483396ae474b02651b40d4a9d61ab99a7455e57ef31f2688bdf81a03068c58706501d3059f40a5366debf4bf1cad48439d19f00154076c5d96c26d6b90100000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "008311f7ea8202848508d329637f825208845682c3438acccccccccccccccccccca0216ef6369d46fe0ad70d2b7f9aa0785f33cbb8894733859136b5143c0ed8b09f88bc45a9ac8c1cea67842b0113c6";
        NewBlockMessage msg = new NewBlockMessage(Hex.decode(rlp));
        Block b = msg.getBlock();
        System.out.println(b);
        System.out.println(msg.getDifficultyAsBigInt().toString(16));
    }

    @Test
    public void blockTest1() {
        String rlp =                 "f90214a0a9c93d6bcd5dbcc94e0f467c88c59851b0951990c1c340c7b20aa967758ecd87a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794ee0250c19ad59305b2bdb61f34b45b72fe37154fa0ed230be6c9531d27a387fa5ca2cb2e70848d5de33636ae2a28d5e9e623a3089da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000831116b881d9850ec4a4290b8084567406d39ad983010302844765746887676f312e352e318777696e646f7773a09a518a25af220fb8afe23bcafd71e4a0dba0da38972e962b07ed89dab34ac2748872311081e40c488a | f90214a01e479ea9dc53e675780469952ea87d531eb3d47808e2b57339055bdc6e61ae57a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794ee0250c19ad59305b2bdb61f34b45b72fe37154fa0ea92e8c9e36ffe81be59f06af1a3b4b18b778838d4fac19f4dfeb08a5a1046dfa056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000831118da81da850ec0f300028084567406dd9ad983010302844765746887676f312e352e318777696e646f7773a056bacad6b399e5e39f5168080941d54807f25984544f6bc885bbf1a0ffd0a0298856ceeb676b74d420 | " +
                "f90214a0b7992b18db1b3514b90376fe96235bc73db9eba3cb21ecb190d34e9c680c914fa06ab8465069b6d6a758c73894d6fbd2ad98f9c551a7a99672aedba3b12d1e76f594ee0250c19ad59305b2bdb61f34b45b72fe37154fa0499179489d7c04a781c3fd8b8b0f0a04030fd2057a11e86e6a12e4baa07dfdd6a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000083111afd81db850ebd42c3438084567406e19ad983010302844765746887676f312e352e318777696e646f7773a04d78ab8e21ea8630f52a60ed60d945c7bbb8267777d28a98612b77a673663430886b676858a8d6b99a | f90204a08680005ea64540a769286d281cb931a97e7abed2611f00f2c6f47a7aaad5faa8a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a0bf55a6e82564fb532316e694838dae38d21fa80bc8af1867e418cb26bcdf0e61a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000831118da81dc850ebd42c3438084567406f58acccccccccccccccccccca04c2135ea0bb1f148303b201141df207898fa0897e3fb48fe661cda3ba2880da388b4ce6cb5535077af | f90204a05897e0e01ed54cf189751c6dd7b0107b2b3b1c841cb7d9bdb6f2aca6ff770c17a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a01ad754d90de6789c4fa5708992d9f089165e5047d52e09abc22cf49428af23cda056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000831116b781dd850ebd42c3438084567407048acccccccccccccccccccca0b314fab93d91a0adea632fd58027cb39857a0ad188c473f41a640052a6a0141d88d64656f7bb5f1066";
        for (String s : rlp.split("\\|")) {
            BlockHeader blockHeader = new BlockHeader(Hex.decode(s));
            System.out.println(Hex.toHexString(blockHeader.getHash()).substring(0, 6));
            System.out.println(blockHeader);
        }
    }


}
