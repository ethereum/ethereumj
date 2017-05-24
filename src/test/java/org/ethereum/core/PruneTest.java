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
package org.ethereum.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.*;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.*;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

import static org.ethereum.util.ByteUtil.intToBytes;
import static org.ethereum.util.blockchain.EtherUtil.Unit.ETHER;
import static org.ethereum.util.blockchain.EtherUtil.convert;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Nashatyrev on 05.07.2016.
 */
public class PruneTest {

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }

    @Test
    public void testJournal1() throws Exception {
        HashMapDB<byte[]> db = new HashMapDB<>();
        CountingBytesSource countDB = new CountingBytesSource(db);
        JournalSource<byte[]> journalDB = new JournalSource<>(countDB);

        put(journalDB, "11");
        put(journalDB, "22");
        put(journalDB, "33");
        journalDB.commitUpdates(intToBytes(1));
        checkKeys(db.getStorage(), "11", "22", "33");

        put(journalDB, "22");
        delete(journalDB, "33");
        put(journalDB, "44");
        journalDB.commitUpdates(intToBytes(2));
        checkKeys(db.getStorage(), "11", "22", "33", "44");

        journalDB.persistUpdate(intToBytes(1));
        checkKeys(db.getStorage(), "11", "22", "33", "44");

        journalDB.revertUpdate(intToBytes(2));
        checkKeys(db.getStorage(), "11", "22", "33");

        put(journalDB, "22");
        delete(journalDB, "33");
        put(journalDB, "44");
        journalDB.commitUpdates(intToBytes(3));
        checkKeys(db.getStorage(), "11", "22", "33", "44");

        delete(journalDB, "22");
        put(journalDB, "33");
        delete(journalDB, "44");
        journalDB.commitUpdates(intToBytes(4));
        checkKeys(db.getStorage(), "11", "22", "33", "44");

        journalDB.persistUpdate(intToBytes(3));
        checkKeys(db.getStorage(), "11", "22", "33", "44");

        journalDB.persistUpdate(intToBytes(4));
        checkKeys(db.getStorage(), "11", "22", "33");

        delete(journalDB, "22");
        journalDB.commitUpdates(intToBytes(5));
        checkKeys(db.getStorage(), "11", "22", "33");

        journalDB.persistUpdate(intToBytes(5));
        checkKeys(db.getStorage(), "11", "33");

    }

    private static void put(Source<byte[], byte[]> db, String key) {
        db.put(Hex.decode(key), Hex.decode(key));
    }
    private static void delete(Source<byte[], byte[]> db, String key) {
        db.delete(Hex.decode(key));
    }

    private static void checkKeys(Map<byte[], byte[]> map, String ... keys) {
        Assert.assertEquals(keys.length, map.size());
        for (String key : keys) {
            assertTrue(map.containsKey(Hex.decode(key)));
        }
    }


    @Test
    public void simpleTest() throws Exception {
        final int pruneCount = 3;
        SystemProperties.getDefault().overrideParams(
                "database.prune.enabled", "true",
                "database.prune.maxDepth", "" + pruneCount,
                "mine.startNonce", "0");

        StandaloneBlockchain bc = new StandaloneBlockchain();

        ECKey alice = ECKey.fromPrivate(BigInteger.ZERO);
        ECKey bob = ECKey.fromPrivate(BigInteger.ONE);

//        System.out.println("Gen root: " + Hex.toHexString(bc.getBlockchain().getBestBlock().getStateRoot()));
        bc.createBlock();
        Block b0 = bc.getBlockchain().getBestBlock();
        bc.sendEther(alice.getAddress(), convert(3, ETHER));
        Block b1_1 = bc.createBlock();

        bc.sendEther(alice.getAddress(), convert(3, ETHER));
        Block b1_2 = bc.createForkBlock(b0);

        bc.sendEther(alice.getAddress(), convert(3, ETHER));
        Block b1_3 = bc.createForkBlock(b0);

        bc.sendEther(alice.getAddress(), convert(3, ETHER));
        Block b1_4 = bc.createForkBlock(b0);

        bc.sendEther(bob.getAddress(), convert(5, ETHER));
        bc.createBlock();

        bc.sendEther(alice.getAddress(), convert(3, ETHER));
        bc.createForkBlock(b1_2);

        for (int i = 0; i < 9; i++) {
            bc.sendEther(alice.getAddress(), convert(3, ETHER));
            bc.sendEther(bob.getAddress(), convert(5, ETHER));
            bc.createBlock();
        }

        byte[][] roots = new byte[pruneCount + 1][];
        for (int i = 0; i < pruneCount + 1; i++) {
            long bNum = bc.getBlockchain().getBestBlock().getNumber() - i;
            Block b = bc.getBlockchain().getBlockByNumber(bNum);
            roots[i] = b.getStateRoot();
        }

        checkPruning(bc.getStateDS(), bc.getPruningStateDS(), roots);

        long bestBlockNum = bc.getBlockchain().getBestBlock().getNumber();

        Assert.assertEquals(convert(30, ETHER), bc.getBlockchain().getRepository().getBalance(alice.getAddress()));
        Assert.assertEquals(convert(50, ETHER), bc.getBlockchain().getRepository().getBalance(bob.getAddress()));

        {
            Block b1 = bc.getBlockchain().getBlockByNumber(bestBlockNum - 1);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(convert(3 * 9, ETHER), r1.getBalance(alice.getAddress()));
            Assert.assertEquals(convert(5 * 9, ETHER), r1.getBalance(bob.getAddress()));
        }

        {
            Block b1 = bc.getBlockchain().getBlockByNumber(bestBlockNum - 2);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(convert(3 * 8, ETHER), r1.getBalance(alice.getAddress()));
            Assert.assertEquals(convert(5 * 8, ETHER), r1.getBalance(bob.getAddress()));
        }

        {
            Block b1 = bc.getBlockchain().getBlockByNumber(bestBlockNum - 3);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(convert(3 * 7, ETHER), r1.getBalance(alice.getAddress()));
            Assert.assertEquals(convert(5 * 7, ETHER), r1.getBalance(bob.getAddress()));
        }

        {
            // this state should be pruned already
            Block b1 = bc.getBlockchain().getBlockByNumber(bestBlockNum - 4);
            Repository r1 = bc.getBlockchain().getRepository().getSnapshotTo(b1.getStateRoot());
            Assert.assertEquals(BigInteger.ZERO, r1.getBalance(alice.getAddress()));
            Assert.assertEquals(BigInteger.ZERO, r1.getBalance(bob.getAddress()));
        }
    }

    static HashMapDB<byte[]> stateDS;
    static String getCount(String hash) {
        byte[] bytes = stateDS.get(Hex.decode(hash));
        return bytes == null ? "0" : "" + bytes[3];
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
        SystemProperties.getDefault().overrideParams(
                "database.prune.enabled", "true",
                "database.prune.maxDepth", "" + pruneCount);

        StandaloneBlockchain bc = new StandaloneBlockchain();

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

        byte[][] roots = new byte[pruneCount + 1][];
        for (int i = 0; i < pruneCount + 1; i++) {
            long bNum = bc.getBlockchain().getBestBlock().getNumber() - i;
            Block b = bc.getBlockchain().getBlockByNumber(bNum);
            roots[i] = b.getStateRoot();
        }

        checkPruning(bc.getStateDS(), bc.getPruningStateDS(), roots);
    }

    @Test
    public void twoContractsTest() throws Exception {
        final int pruneCount = 3;
        SystemProperties.getDefault().overrideParams(
                "database.prune.enabled", "true",
                "database.prune.maxDepth", "" + pruneCount);

        String src =
                "contract Simple {" +
                "  uint public n;" +
                "  function set(uint _n) { n = _n; } " +
                "  function inc() { n++; } " +
                "}";

        StandaloneBlockchain bc = new StandaloneBlockchain();

        Block b0 = bc.getBlockchain().getBestBlock();

        SolidityContract contr1 = bc.submitNewContract(src);
        SolidityContract contr2 = bc.submitNewContract(src);
        Block b1 = bc.createBlock();
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b1.getStateRoot(), b0.getStateRoot());

        // add/remove/add in the same block
        contr1.callFunction("set", 0xaaaaaaaaaaaaL);
        contr2.callFunction("set", 0xaaaaaaaaaaaaL);
        Block b2 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b2.getStateRoot(), b1.getStateRoot(), b0.getStateRoot());

        contr2.callFunction("set", 0xbbbbbbbbbbbbL);
        Block b3 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b3.getStateRoot(), b2.getStateRoot(), b1.getStateRoot(), b0.getStateRoot());

        // force prune
        Block b4 = bc.createBlock();
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b4.getStateRoot(), b3.getStateRoot(), b2.getStateRoot(), b1.getStateRoot());
        Block b5 = bc.createBlock();
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b5.getStateRoot(), b4.getStateRoot(), b3.getStateRoot(), b2.getStateRoot());
        Block b6 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b6.getStateRoot(), b5.getStateRoot(), b4.getStateRoot(), b3.getStateRoot());

        contr1.callFunction("set", 0xaaaaaaaaaaaaL);
        contr2.callFunction("set", 0xaaaaaaaaaaaaL);
        Block b7 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b7.getStateRoot(), b6.getStateRoot(), b5.getStateRoot(), b4.getStateRoot());

        contr1.callFunction("set", 0xbbbbbbbbbbbbL);
        Block b8 = bc.createBlock();
        Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b8.getStateRoot(), b7.getStateRoot(), b6.getStateRoot(), b5.getStateRoot());

        contr2.callFunction("set", 0xbbbbbbbbbbbbL);
        Block b8_ = bc.createForkBlock(b7);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b8.getStateRoot(), b8_.getStateRoot(), b7.getStateRoot(), b6.getStateRoot(), b5.getStateRoot());
        Block b9_ = bc.createForkBlock(b8_);
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b9_.getStateRoot(), b8.getStateRoot(), b8_.getStateRoot(), b7.getStateRoot(), b6.getStateRoot());

        Block b9 = bc.createForkBlock(b8);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b9.getStateRoot(), b9_.getStateRoot(), b8.getStateRoot(), b8_.getStateRoot(), b7.getStateRoot(), b6.getStateRoot());
        Block b10 = bc.createForkBlock(b9);
        Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr2.callConstFunction("n")[0]);
        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b10.getStateRoot(), b9.getStateRoot(), b9_.getStateRoot(), b8.getStateRoot(), b8_.getStateRoot(), b7.getStateRoot());


        Block b11 = bc.createForkBlock(b10);
        Assert.assertEquals(BigInteger.valueOf(0xbbbbbbbbbbbbL), contr1.callConstFunction("n")[0]);
        Assert.assertEquals(BigInteger.valueOf(0xaaaaaaaaaaaaL), contr2.callConstFunction("n")[0]);

        checkPruning(bc.getStateDS(), bc.getPruningStateDS(),
                b11.getStateRoot(), b10.getStateRoot(), b9.getStateRoot(), /*b9_.getStateRoot(),*/ b8.getStateRoot());
    }

    @Test
    public void branchTest() throws Exception {
        final int pruneCount = 3;
        SystemProperties.getDefault().overrideParams(
                "database.prune.enabled", "true",
                "database.prune.maxDepth", "" + pruneCount);

        StandaloneBlockchain bc = new StandaloneBlockchain();

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
    public void storagePruneTest() throws Exception {
        final int pruneCount = 3;
        SystemProperties.getDefault().overrideParams(
                "details.inmemory.storage.limit", "200",
                "database.prune.enabled", "true",
                "database.prune.maxDepth", "" + pruneCount);

        StandaloneBlockchain bc = new StandaloneBlockchain();
        BlockchainImpl blockchain = (BlockchainImpl) bc.getBlockchain();
//        RepositoryImpl repository = (RepositoryImpl) blockchain.getRepository();
//        HashMapDB storageDS = new HashMapDB();
//        repository.getDetailsDataStore().setStorageDS(storageDS);

        SolidityContract contr = bc.submitNewContract(
                "contract Simple {" +
                        "  uint public n;" +
                        "  mapping(uint => uint) largeMap;" +
                        "  function set(uint _n) { n = _n; } " +
                        "  function put(uint k, uint v) { largeMap[k] = v; }" +
                        "}");
        Block b1 = bc.createBlock();

        int entriesForExtStorage = 100;

        for (int i = 0; i < entriesForExtStorage; i++) {
            contr.callFunction("put", i, i);
            if (i % 100 == 0) bc.createBlock();
        }
        bc.createBlock();
        blockchain.flush();
        contr.callFunction("put", 1000000, 1);
        bc.createBlock();
        blockchain.flush();

        for (int i = 0; i < 100; i++) {
            contr.callFunction("set", i);
            bc.createBlock();
            blockchain.flush();
            System.out.println(bc.getStateDS().getStorage().size() + ", " + bc.getStateDS().getStorage().size());
        }

        System.out.println("Done");
    }


    @Ignore
    @Test
    public void rewriteSameTrieNode() throws Exception {
        final int pruneCount = 3;
        SystemProperties.getDefault().overrideParams(
                "database.prune.enabled", "true",
                "database.prune.maxDepth", "" + pruneCount);

        StandaloneBlockchain bc = new StandaloneBlockchain();
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

    public void checkPruning(final HashMapDB<byte[]> stateDS, final Source<byte[], byte[]> stateJournalDS, byte[] ... roots) {
        System.out.println("Pruned storage size: " + stateDS.getStorage().size());

        Set<ByteArrayWrapper> allRefs = new HashSet<>();
        for (byte[] root : roots) {

            Set<ByteArrayWrapper> bRefs = getReferencedTrieNodes(stateJournalDS, true, root);
            System.out.println("#" + Hex.toHexString(root).substring(0,8) + " refs: ");
            for (ByteArrayWrapper bRef : bRefs) {
                System.out.println("    " + bRef.toString().substring(0, 8));
            }
            allRefs.addAll(bRefs);
        }

        System.out.println("Trie nodes closure size: " + allRefs.size());
        if (allRefs.size() != stateDS.getStorage().size()) {
            for (byte[] hash : stateDS.getStorage().keySet()) {
                if (!allRefs.contains(new ByteArrayWrapper(hash))) {
                    System.out.println("Extra node: " + Hex.toHexString(hash));
                }
            }
//            Assert.assertEquals(allRefs.size(), stateDS.getStorage().size());
        }

        for (byte[] key : stateDS.getStorage().keySet()) {
//            Assert.assertTrue(allRefs.contains(new ByteArrayWrapper(key)));
        }
    }

    public Set<ByteArrayWrapper> getReferencedTrieNodes(final Source<byte[], byte[]> stateDS, final boolean includeAccounts,
                                                        byte[] ... roots) {
        final Set<ByteArrayWrapper> ret = new HashSet<>();
        for (byte[] root : roots) {
            SecureTrie trie = new SecureTrie(stateDS, root);
            trie.scanTree(new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, TrieImpl.Node node) {
                    ret.add(new ByteArrayWrapper(hash));
                }

                @Override
                public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
                    if (includeAccounts) {
                        AccountState accountState = new AccountState(value);
                        if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                            ret.add(new ByteArrayWrapper(accountState.getCodeHash()));
                        }
                        if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                            ret.addAll(getReferencedTrieNodes(stateDS, false, accountState.getStateRoot()));
                        }
                    }
                }
            });
        }
        return ret;
    }

    public String dumpState(final Source<byte[], byte[]> stateDS, final boolean includeAccounts,
                                                        byte[] root) {
        final StringBuilder ret = new StringBuilder();
        SecureTrie trie = new SecureTrie(stateDS, root);
        trie.scanTree(new TrieImpl.ScanAction() {
            @Override
            public void doOnNode(byte[] hash, TrieImpl.Node node) {
            }

            @Override
            public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
                if (includeAccounts) {
                    AccountState accountState = new AccountState(value);
                    ret.append(Hex.toHexString(nodeHash) + ": Account: " + Hex.toHexString(key) + ", Nonce: " + accountState.getNonce() + ", Balance: " + accountState.getBalance() + "\n");
                    if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                        ret.append("    CodeHash: " + Hex.toHexString(accountState.getCodeHash()) + "\n");
                    }
                    if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                        ret.append(dumpState(stateDS, false, accountState.getStateRoot()));
                    }
                } else {
                    ret.append("    " + Hex.toHexString(nodeHash) + ": " + Hex.toHexString(key) + " = " + Hex.toHexString(value) + "\n");
                }
            }
        });
        return ret.toString();
    }
}
