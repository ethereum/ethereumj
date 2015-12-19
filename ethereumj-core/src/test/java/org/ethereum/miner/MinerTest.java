package org.ethereum.miner;

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.mine.BlockMiner;
import org.ethereum.mine.Ethash;
import org.ethereum.mine.MinerListener;
import org.ethereum.net.eth.handler.Eth62;
import org.ethereum.net.eth.message.*;
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
                        "peer.active = [{ url = \"enode://b23b3b9f38f1d314b27e63c63f3e45a6ea5f82c83f282e2d38f2f01c22165e897656fe2e5f9f18616b81f41cbcf2e9100fc9f8dad099574f3d84cf9623de2fc9@localhost:20301\" }] \n" +
                        "sync.enabled = true \n" +
                        "genesis = genesis-harder.json \n" +
//                        "genesis = frontier.json \n" +
                        "database.dir = testDB-1 \n"));

        Ethereum ethereum2 = EthereumFactory.createEthereum(SysPropConfig2.props, SysPropConfig2.class);

        ethereum2.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                System.err.println("=== New block: " + block);
            }

            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                System.err.println("=== Tx: " + transactions);
            }

            @Override
            public void onSyncDone() {
                System.err.println("=== Sync Done!");
            }
        });

        Thread.sleep(100000000L);
    }


    static String blockInfo(Block b) {
        boolean ours = Hex.toHexString(b.getExtraData()).startsWith("cccccccccc");
        return (ours ? "###" : "  #") + b.getNumber() +
                " (" + b.getShortHash() + " <~ " + Hex.toHexString(b.getParentHash()).substring(0, 6) + ")";
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
                        "mine.extraDataHex = cccccccccccccccccccc"));

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
            public void onSyncDone() {
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
//            public void onPendingTransactionsReceived(List<Transaction> transactions) {
//                System.err.println("=== Tx: " + transactions);
//            }
//        });

        System.out.println("=== Waiting for sync ...");
        semaphore.await(10, TimeUnit.SECONDS);
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
                boolean validate = Ethash.getForBlock(block.getNumber()).validate(block.getHeader());
                System.out.println("=== MinerTest.blockMined " + blockInfo(block));
                System.out.println("=== MinerTest.blockMined: " + validate);
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


//        ECKey senderKey = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
//        byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");
//
//        for (int i = ethereum1.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 200; i++, j++) {
//            {
//                Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
//                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
//                        receiverAddr, new byte[]{77}, new byte[0]);
//                tx.sign(senderKey.getPrivKeyBytes());
//                System.out.println("=== Submitting tx: " + tx);
//                ethereum1.submitTransaction(tx);
//            }
//            Thread.sleep(7000);
//        }

        Thread.sleep(1000000000);

        ethereum1.close();

        System.out.println("Passed.");

    }

    public static void main(String[] args) throws Exception {
        ECKey k = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec")).decompress();
        System.out.println(Hex.toHexString(k.getPrivKeyBytes()));
        System.out.println(Hex.toHexString(k.getAddress()));
        System.out.println(Hex.toHexString(k.getNodeId()));
    }

    @Test
    public void blockTest() {
        String rlp = "f9020ef90209f90204a0ce32abbe515a133a112ccd2c3b0e1ecbb4fc9dd8a2db3e76ee9e3546622acddaa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a023cc06ee8d21946c0493a23d7d5be530f9bfbdeeaad9b30dcdc307fd4e2ffaa7a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008180018510000000008084566ee20091457468657265756d4a20706f7765726564a048d4e7e2f5eb3a17f4a26b378ca09f487b23b67aa8e451560aff2c9b55cf756c84d7cc4190c0c08180";
        NewBlockMessage msg = new NewBlockMessage(Hex.decode(rlp));
        Block b = msg.getBlock();
        System.out.println(b);
    }

    @Test
    public void blockTest1() {
        String rlp = "f90208a0a222983b8f3d46311c7a4a89ae9a07c60d498b7a2c3621adc064bc8e0ac0dd9ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a09052035a0ca4ae5792dfd5279dfc7e7f7d63d8eaf9561b18d4b2c4c894af6e8ca056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000081800485100000000080845670363691457468657265756d4a20706f7765726564a04a1f7e5d11e5b1c2a28a5a415e3520c097e99a1aac4a1e2557ef3054cf35144c88dffeebb4e41c5240 | f90208a0afa12498813f5ec1cd82bf6206a0145b149672ea5ead4b03ff1d08f62cd4aff8a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a051bfb1c07c620d90830bc4f9f23ae388c42790bbb70306dd6685117af46f9aa5a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000081800385100000000080845670363491457468657265756d4a20706f7765726564a0e2530e391b2248e5fa643d53bab8f44749e8dd0876ba916c881b259cb90a62a2888b6d430114b19ae1 | f90208a05fe71b942066cc76b925957879bd5154a301d033888a64cdf566a08cab826022a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a0a3deab4161e9376259fb859a4886a73e07f142e8c4201fad22423c49345efe37a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000081800285100000000080845670362a91457468657265756d4a20706f7765726564a06e691f42fba7ec9f7310f9bcdba53825bdb2d072e5665824b746a748633cfc0488111075dd5820461c | f9020aa0ce32abbe515a133a112ccd2c3b0e1ecbb4fc9dd8a2db3e76ee9e3546622acddaa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479450b8f981ce93fd5b81b844409169148428400bf3a0cc7dd9d98978db65ced1311318be2df0eae5b5f309d3ae70fd97e27768751c96a07d93344b81e6e3a527017b30ebf7b19870fb19d362e58f5187690c5759d4dcd9a05f2ebdfcb73ace2b863e9d7b4f0db19b4d8329bd253ecae37da8c46868a0f591b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000081800185100000000082a410845670362291457468657265756d4a20706f7765726564a05bacf4f966cdb83102cf517e43452e9f3add9e77c43653d03ad0b633e9f1fa6b88176109b55b38279f | f90213a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0de3f7341e1962242704c94039867d8ae5e2637645453ca54f099dec7e8df22a3a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008180808510000000008080a011bbe8db4e347b4e8c937c1c8370e4b5ed33adb3db69cbdb7a38e1e50b1b82faa00000000000000000000000000000000000000000000000000000000000000000880000000000000000 | f90213a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0de3f7341e1962242704c94039867d8ae5e2637645453ca54f099dec7e8df22a3a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008180808510000000008080a011bbe8db4e347b4e8c937c1c8370e4b5ed33adb3db69cbdb7a38e1e50b1b82faa00000000000000000000000000000000000000000000000000000000000000000880000000000000000";
        for (String s : rlp.split("\\|")) {
            BlockHeader blockHeader = new BlockHeader(Hex.decode(s));
            System.out.println(blockHeader);
        }
    }

}
