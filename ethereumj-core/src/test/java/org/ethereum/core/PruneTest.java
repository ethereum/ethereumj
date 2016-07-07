package org.ethereum.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.Value;
import org.ethereum.util.blockchain.EtherUtil;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static org.ethereum.util.blockchain.EtherUtil.Unit.*;
import static org.ethereum.util.blockchain.EtherUtil.convert;

/**
 * Created by Anton Nashatyrev on 05.07.2016.
 */
public class PruneTest {

    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Test
    public void simpleTest() throws Exception {
        StandaloneBlockchain bc = new StandaloneBlockchain();
        final int pruneCount = 3;
        ((RepositoryImpl) bc.getBlockchain().getRepository()).setPruneBlockCount(pruneCount);

        ECKey alice = new ECKey();
        ECKey bob = new ECKey();

//        System.out.println("Gen root: " + Hex.toHexString(bc.getBlockchain().getBestBlock().getStateRoot()));

        for (int i = 0; i < 10; i++) {
            bc.sendEther(alice.getAddress(), convert(3, ETHER));
            bc.sendEther(bob.getAddress(), convert(5, ETHER));
            bc.createBlock();
//            System.out.println("#" + (i + 1) + ": " + Hex.toHexString(bc.getBlockchain().getBestBlock().getStateRoot()));
//            System.out.println("Miner: " + bc.getBlockchain().getRepository().getBalance(bc.getBlockchain().getMinerCoinbase()));
        }
        bc.getBlockchain().flush();
        System.out.println("Pruned storage size: " + bc.getStateDS().keys().size());

        Set<ByteArrayWrapper> allRefs = new HashSet<>();
        for (int i = 0; i < pruneCount + 1; i++) {
            long bNum = bc.getBlockchain().getBestBlock().getNumber() - i;
            Block b = bc.getBlockchain().getBlockByNumber(bNum);
            Set<ByteArrayWrapper> bRefs = getReferencedTrieNodes(bc.getStateDS(), b.getStateRoot());
            System.out.println("#" + bNum + " refs: ");
            for (ByteArrayWrapper bRef : bRefs) {
                System.out.println("    " + bRef.toString().substring(0, 8));
            }
            allRefs.addAll(bRefs);
        }

        System.out.println("Trie nodes closure size: " + allRefs.size());
        Assert.assertEquals(allRefs.size(), bc.getStateDS().keys().size());
        for (byte[] key : bc.getStateDS().keys()) {
            Assert.assertTrue(allRefs.contains(new ByteArrayWrapper(key)));
        }

        System.out.printf("Best block: " + bc.getBlockchain().getBestBlock().getShortDescr());

        Assert.assertEquals(convert(30, ETHER), bc.getBlockchain().getRepository().getBalance(alice.getAddress()));
        Assert.assertEquals(convert(50, ETHER), bc.getBlockchain().getRepository().getBalance(bob.getAddress()));

        {
            Block b1 = bc.getBlockchain().getBlockByNumber(9);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(convert(3 * 9, ETHER), r1.getBalance(alice.getAddress()));
            Assert.assertEquals(convert(5 * 9, ETHER), r1.getBalance(bob.getAddress()));
        }

        {
            Block b1 = bc.getBlockchain().getBlockByNumber(8);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(convert(3 * 8, ETHER), r1.getBalance(alice.getAddress()));
            Assert.assertEquals(convert(5 * 8, ETHER), r1.getBalance(bob.getAddress()));
        }

        {
            Block b1 = bc.getBlockchain().getBlockByNumber(7);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(convert(3 * 7, ETHER), r1.getBalance(alice.getAddress()));
            Assert.assertEquals(convert(5 * 7, ETHER), r1.getBalance(bob.getAddress()));
        }

        {
            // this state should be pruned already
            Block b1 = bc.getBlockchain().getBlockByNumber(6);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(BigInteger.ZERO, r1.getBalance(alice.getAddress()));
            Assert.assertEquals(BigInteger.ZERO, r1.getBalance(bob.getAddress()));
        }
    }

    @Test
    public void contractTest() throws Exception {
        // checks that pruning doesn't delete the nodes which were 're-added' later
        // e.g. when a contract variable assigned value V1 the trie acquires node with key K1
        // then if the variable reassigned value V2 the trie acquires new node with key K2
        // and the node K1 is not needed anymore and added to the prune list
        // we should avoid situations when the value V1 is back, the node K1 is also back to the trie
        // but erroneously deleted later as was in the prune list
        final int pruneCount = 3;

        StandaloneBlockchain bc = new StandaloneBlockchain();
        ((RepositoryImpl) bc.getBlockchain().getRepository()).setPruneBlockCount(pruneCount);

        SolidityContract contr = bc.submitNewContract(
                "contract Simple {" +
                "  uint public n;" +
                "  function set(uint _n) { n = _n; } " +
                "}");
        bc.createBlock();

        // add/remove/add in the same block
        contr.callFunction("set", 0xaaaaaaaaaaaaL);
        contr.callFunction("set", 0xbbbbbbbbbbbbL);
        contr.callFunction("set", 0xaaaaaaaaaaaaL);
        bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr.callConstFunction("n")[0]);
        // force prune
        bc.createBlock();
        bc.createBlock();
        bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr.callConstFunction("n")[0]);

        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 4; j++) {

                contr.callFunction("set", 0xbbbbbbbbbbbbL);
                for (int k = 0; k < j; k++) {
                    bc.createBlock();
                }
                if (j > 0)
                    Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr.callConstFunction("n")[0]);

                contr.callFunction("set", 0xaaaaaaaaaaaaL);

                for (int k = 0; k < i; k++) {
                    bc.createBlock();
                }

                Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr.callConstFunction("n")[0]);

            }
        }
    }

    @Test
    public void branchTest() throws Exception {
        final int pruneCount = 3;

        StandaloneBlockchain bc = new StandaloneBlockchain();
        ((RepositoryImpl) bc.getBlockchain().getRepository()).setPruneBlockCount(pruneCount);

        SolidityContract contr = bc.submitNewContract(
                "contract Simple {" +
                "  uint public n;" +
                "  function set(uint _n) { n = _n; } " +
                "}");
        Block b1 = bc.createBlock();
        contr.callFunction("set", 0xaaaaaaaaaaaaL);
        Block b2 = bc.createBlock();
        contr.callFunction("set", 0xbbbbbbbbbbbbL);
        Block b2_ = bc.createForkBlock(b1);
        bc.createForkBlock(b2);
        bc.createBlock();
        bc.createBlock();
        bc.createBlock();

        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr.callConstFunction("n")[0]);
    }

    @Test
    public void rewriteSameTrieNode() throws Exception {
        final int pruneCount = 3;

        StandaloneBlockchain bc = new StandaloneBlockchain();
        ((RepositoryImpl) bc.getBlockchain().getRepository()).setPruneBlockCount(pruneCount);
        byte[] receiver = Hex.decode("0000000000000000000000000000000000000000");
        bc.sendEther(receiver, BigInteger.valueOf(0x77777777));
        bc.createBlock();

        for (int i = 0; i < 100; i++) {
            bc.sendEther(new ECKey().getAddress(), BigInteger.valueOf(i));
        }

        SolidityContract contr = bc.submitNewContract(
                "contract Stupid {" +
                        "  function wrongAddress() { " +
                        "    address addr = 0x0000000000000000000000000000000000000000; " +
                        "    addr.call();" +
                        "  } " +
                        "}");
        Block b1 = bc.createBlock();
        contr.callFunction("wrongAddress");
        Block b2 = bc.createBlock();
        contr.callFunction("wrongAddress");
        Block b3 = bc.createBlock();

        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr.callConstFunction("n")[0]);
    }

    public Set<ByteArrayWrapper> getReferencedTrieNodes(KeyValueDataSource stateDS, byte[] ... roots) {
        final Set<ByteArrayWrapper> ret = new HashSet<>();
        SecureTrie trie = new SecureTrie(stateDS);
        for (byte[] root : roots) {
            trie.scanTree(root, new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, Value node) {
                    ret.add(new ByteArrayWrapper(hash));
                }
            });
        }
        return ret;
    }
}
