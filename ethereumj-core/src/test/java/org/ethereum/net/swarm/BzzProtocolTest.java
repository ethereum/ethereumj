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
package org.ethereum.net.swarm;

import org.ethereum.net.rlpx.Node;
import org.ethereum.net.swarm.bzz.BzzMessage;
import org.ethereum.net.swarm.bzz.BzzProtocol;
import org.ethereum.net.swarm.bzz.PeerAddress;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.Functional;
import org.ethereum.util.Utils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by Admin on 24.06.2015.
 */
public class BzzProtocolTest {

    interface Predicate<T> { boolean test(T t);}

    public static class FilterPrinter extends PrintWriter {
        String filter;
        Predicate<String> pFilter;
        public FilterPrinter(OutputStream out) {
            super(out, true);
        }

        @Override
        public void println(String x) {
            if (pFilter == null || pFilter.test(x)) {
//            if (filter == null || x.contains(filter)) {
                super.println(x);
            }
        }

        public void setFilter(final String filter) {
            pFilter = new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    return s.contains(filter);
                }
            };
        }

        public void setFilter(Predicate<String> pFilter) {
            this.pFilter = pFilter;
        }
    }

    static FilterPrinter stdout = new FilterPrinter(System.out);

    public static class TestPipe {
        protected Functional.Consumer<BzzMessage> out1;
        protected Functional.Consumer<BzzMessage> out2;
        protected String name1, name2;

        public TestPipe(Functional.Consumer<BzzMessage> out1, Functional.Consumer<BzzMessage> out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        protected TestPipe() {
        }

        Functional.Consumer<BzzMessage> createIn1() {
            return new Functional.Consumer<BzzMessage>() {
                @Override
                public void accept(BzzMessage bzzMessage) {
                    BzzMessage smsg = serialize(bzzMessage);
                    if (TestPeer.MessageOut) {
                        stdout.println("+ " + name1 + " => " + name2 + ": " + smsg);
                    }
                    out2.accept(smsg);
                }
            };
        }
        Functional.Consumer<BzzMessage> createIn2() {
            return new Functional.Consumer<BzzMessage>() {
                @Override
                public void accept(BzzMessage bzzMessage) {
                    BzzMessage smsg = serialize(bzzMessage);
                    if (TestPeer.MessageOut) {
                        stdout.println("+ " + name2 + " => " + name1 + ": " + smsg);
                    }
                    out1.accept(smsg);
                }
            };
        }

        public void setNames(String name1, String name2) {
            this.name1 = name1;
            this.name2 = name2;
        }

        private BzzMessage serialize(BzzMessage msg) {
            try {
                return msg.getClass().getConstructor(byte[].class).newInstance(msg.getEncoded());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Functional.Consumer<BzzMessage> getOut1() {
            return out1;
        }

        public Functional.Consumer<BzzMessage> getOut2() {
            return out2;
        }
    }

    public static class TestAsyncPipe extends TestPipe {

        static ScheduledExecutorService exec = Executors.newScheduledThreadPool(32);
        static Queue<Future<?>> tasks = new LinkedBlockingQueue<>();

        class AsyncConsumer implements Functional.Consumer<BzzMessage> {
            Functional.Consumer<BzzMessage> delegate;

            boolean rev;

            public AsyncConsumer(Functional.Consumer<BzzMessage> delegate, boolean rev) {
                this.delegate = delegate;
                this.rev = rev;
            }

            @Override
            public void accept(final BzzMessage bzzMessage) {
                ScheduledFuture<?> future = exec.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!rev) {
                                if (TestPeer.MessageOut) {
                                    stdout.println("- " + name1 + " => " + name2 + ": " + bzzMessage);
                                }
                            } else {
                                if (TestPeer.MessageOut) {
                                    stdout.println("- " + name2 + " => " + name1 + ": " + bzzMessage);
                                }
                            }
                            delegate.accept(bzzMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, channelLatencyMs, TimeUnit.MILLISECONDS);
                tasks.add(future);
            }

        }

        long channelLatencyMs = 2;

        public static void waitForCompletion() {
            try {
                while(!tasks.isEmpty()) {
                    tasks.poll().get();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public TestAsyncPipe(Functional.Consumer<BzzMessage> out1, Functional.Consumer<BzzMessage> out2) {
            this.out1 = new AsyncConsumer(out1, false);
            this.out2 = new AsyncConsumer(out2, true);
        }
    }

    public static class SimpleHive extends Hive {
        Map<BzzProtocol, Object> peers = new IdentityHashMap<>();
//        PeerAddress thisAddress;
        TestPeer thisPeer;
//        NodeTable nodeTable;

        public SimpleHive(PeerAddress thisAddress) {
            super(thisAddress);
//            this.thisAddress = thisAddress;
//            nodeTable = new NodeTable(thisAddress.toNode());
        }

        public SimpleHive setThisPeer(TestPeer thisPeer) {
            this.thisPeer = thisPeer;
            return this;
        }

        @Override
        public void addPeer(BzzProtocol peer) {
            peers.put(peer, null);
            super.addPeer(peer);
//            nodeTable.addNode(peer.getNode().toNode());
//            peersAdded();
        }

//        @Override
//        public void removePeer(BzzProtocol peer) {
//            peers.remove(peer);
//            nodeTable.dropNode(peer.getNode().toNode());
//        }
//
//        @Override
//        public void addPeerRecords(BzzPeersMessage req) {
//            for (PeerAddress peerAddress : req.getPeers()) {
//                nodeTable.addNode(peerAddress.toNode());
//            }
//            peersAdded();
//        }

        @Override
        public Collection<PeerAddress> getNodes(Key key, int max) {
            List<Node> closestNodes = nodeTable.getClosestNodes(key.getBytes());
            ArrayList<PeerAddress> ret = new ArrayList<>();
            for (Node node : closestNodes) {
                ret.add(new PeerAddress(node));
                if (--max == 0) break;
            }
            return ret;
        }

        @Override
        public Collection<BzzProtocol> getPeers(Key key, int maxCount) {
            if (thisPeer == null) return peers.keySet();
//            TreeMap<Key, TestPeer> sort = new TreeMap<Key, TestPeer>(new Comparator<Key>() {
//                @Override
//                public int compare(Key o1, Key o2) {
//                    for (int i = 0; i < o1.getBytes().length; i++) {
//                        if (o1.getBytes()[i] > o2.getBytes()[i]) return 1;
//                        if (o1.getBytes()[i] < o2.getBytes()[i]) return -1;
//                    }
//                    return 0;
//                }
//            });
//            for (TestPeer testPeer : TestPeer.staticMap.values()) {
//                if (thisPeer != testPeer) {
//                    sort.put(distance(key, new Key(testPeer.peerAddress.getId())), testPeer);
//                }
//            }
            List<Node> closestNodes = nodeTable.getClosestNodes(key.getBytes());
            ArrayList<BzzProtocol> ret = new ArrayList<>();
            for (Node node : closestNodes) {
                ret.add(thisPeer.getPeer(new PeerAddress(node)));

                if (--maxCount == 0) break;
            }
            return ret;
        }
    }

    public static class TestPeer {
        static Map<PeerAddress, TestPeer> staticMap = Collections.synchronizedMap(new HashMap<PeerAddress, TestPeer>());

        public static boolean MessageOut = false;
        public static boolean AsyncPipe = false;

        String name;
        PeerAddress peerAddress;

        LocalStore localStore;
        Hive hive;
        NetStore netStore;

        Map<Key, BzzProtocol> connections = new HashMap<>();

        public TestPeer(int num) {
            this(new PeerAddress(new byte[]{0, 0, (byte) ((num >> 8) & 0xFF), (byte) (num & 0xFF)}, 1000 + num,
                    sha3(new byte[]{(byte) ((num >> 8) & 0xFF), (byte) (num & 0xFF)})), "" + num);
        }

        public TestPeer(PeerAddress peerAddress, String name) {
            this.name = name;
            this.peerAddress = peerAddress;
            localStore = new LocalStore(new MemStore(), new MemStore());
            hive = new SimpleHive(peerAddress).setThisPeer(this);
            netStore = new NetStore(localStore, hive);

            netStore.start(peerAddress);

            staticMap.put(peerAddress, this);
        }

        public BzzProtocol getPeer(PeerAddress addr) {
            Key peerKey = new Key(addr.getId());
            BzzProtocol protocol = connections.get(peerKey);
            if (protocol == null) {
                connect(staticMap.get(addr));
                protocol = connections.get(peerKey);
            }
            return protocol;
        }

        private BzzProtocol createPeerProtocol(PeerAddress addr) {
            Key peerKey = new Key(addr.getId());
            BzzProtocol protocol = connections.get(peerKey);
            if (protocol == null) {
                protocol = new BzzProtocol(netStore);
                connections.put(peerKey, protocol);
            }
            return protocol;
        }

        public void connect(TestPeer peer) {
            BzzProtocol myBzz = this.createPeerProtocol(peer.peerAddress);
            BzzProtocol peerBzz = peer.createPeerProtocol(peerAddress);

            TestPipe pipe = AsyncPipe ? new TestAsyncPipe(myBzz, peerBzz) : new TestPipe(myBzz, peerBzz);

            pipe.setNames(this.name, peer.name);
            System.out.println("Connecting: " + this.name + " <=> " + peer.name);
            myBzz.setMessageSender(pipe.createIn1());
            peerBzz.setMessageSender(pipe.createIn2());
            myBzz.start();
            peerBzz.start();
        }

        public void connect(PeerAddress addr) {
            TestPeer peer = staticMap.get(addr);
            if (peer != null) {
                connect(peer);
            }
        }
    }

    @Test
    public void simple3PeersTest() throws Exception {
        TestPeer.MessageOut = true;
        TestPeer.AsyncPipe = true;

        TestPeer p1 = new TestPeer(1);
        TestPeer p2 = new TestPeer(2);
        TestPeer p3 = new TestPeer(3);
//        TestPeer p4 = new TestPeer(4);

        System.out.println("Put chunk to 1");
        Key key = new Key(new byte[]{0x22, 0x33});
        Chunk chunk = new Chunk(key, new byte[] {0,0,0,0,0,0,0,0, 77, 88});
        p1.netStore.put(chunk);

        System.out.println("Connect 1 <=> 2");
        p1.connect(p2);
        System.out.println("Connect 2 <=> 3");
        p2.connect(p3);
//        p2.connect(p4);

//        Thread.sleep(3000);

        System.err.println("Requesting chunk from 3...");
        Chunk chunk1 = p3.netStore.get(key);
        Assert.assertEquals(key, chunk1.getKey());
        Assert.assertArrayEquals(chunk.getData(), chunk1.getData());
    }

    private String dumpPeers(TestPeer[] allPeers, Key key) {
        String s = "Name\tChunks\tPeers\tMsgIn\tMsgOut\n";
        for (TestPeer peer : allPeers) {
            s += (peer.name + "\t" +
                    (int)((Statter.SimpleStatter)((MemStore) peer.localStore.memStore).statCurChunks).getLast() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statHandshakes)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statInMsg)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statOutMsg)).getCount()) + "\t" +
                    (key != null ? ", keyDist: " + hex(getDistance(peer.peerAddress.getId(), key.getBytes())) : "") +"\n";
            s += "  Chunks:\n";
            for (Key k : ((MemStore) peer.localStore.memStore).store.keySet()) {
                s += ("    " + k.toString().substring(0,8) + ", dist: "
                        + hex(getDistance(peer.peerAddress.getId(), k.getBytes()))) + "\n";
            }
            s += "  Nodes:\n";
            Map<Node, BzzProtocol> entries = peer.hive.getAllEntries();
            SortedMap<Long, Node> sort = new TreeMap<>();
            for (Node node : entries.keySet()) {
                int dist = getDistance(node.getId(), key.getBytes());
                sort.put(0xFFFFFFFFl & dist, node);
            }
            for (Node node : sort.values()) {
                s += "  " + (entries.get(node) == null ? " " : "*") + " "
                        + node.getHost() + ", dist: " +
                        hex(getDistance(peer.peerAddress.getId(), node.getId())) +
                        (key != null ? ", keyDist: " + hex(getDistance(node.getId(), key.getBytes())) : "") + "\n";
            }
        }
        return s;
    }

    private String hex(int i ) {
        return "0x" + Utils.align(Integer.toHexString(i), '0', 8, true);
    }

    private int getDistance(byte[] k1, byte[] k2) {
        int i1 = ByteUtil.byteArrayToInt(Arrays.copyOfRange(k1, 0, 4));
        int i2 = ByteUtil.byteArrayToInt(Arrays.copyOfRange(k2, 0, 4));
        return i1 ^ i2;
    }

    @Ignore
    @Test
    public void manyPeersTest() throws InterruptedException {
        TestPeer.AsyncPipe = true;
//        TestPeer.MessageOut = true;

        final int maxStoreCount = 3;

        TestPeer p0 = new TestPeer(0);
        p0.netStore.maxStorePeers = maxStoreCount;
        System.out.println("Creating chain of peers");
        final TestPeer[] allPeers = new TestPeer[100];
        allPeers[0] = p0;
        for (int i = 1; i < allPeers.length; i++) {
            allPeers[i] = new TestPeer(i);
            allPeers[i].netStore.maxStorePeers = maxStoreCount;
//            System.out.println("Connecting " + i + " <=> " + (i-1));
            allPeers[i].connect(allPeers[i-1]);
//            System.out.println("Connecting " + i + " <=> " + (0));
//            allPeers[i].connect(p0);
        }
//        p0.netStore.put(chunk);

        System.out.println("Waiting for net idle ");
        TestAsyncPipe.waitForCompletion();
        TestPeer.MessageOut = true;
        stdout.setFilter(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.startsWith("+") && s.contains("BzzStoreReqMessage");
            }
        });
//        System.out.println("==== Storage statistics:\n" + dumpPeers(allPeers, null));
//        System.out.println("Sleeping...");

//        System.out.println("Connecting a new peer...");
//        allPeers[allPeers.length-1].connect(new TestPeer(allPeers.length));

//        Thread.sleep(10000000);
        System.out.println("Put chunk to 0");
//        Key key = new Key(new byte[]{0x22, 0x33});
//        Chunk chunk = new Chunk(key, new byte[] {0,0,0,0,0,0,0,0, 77, 88});
        Chunk[] chunks = new Chunk[10];
        int shift = 1;
        for (int i = 0; i < chunks.length; i++) {
            Key key = new Key(sha3(new byte[]{0x22, (byte) (i+shift)}));
//            stdout.setFilter(Hex.toHexString(key.getBytes()));
            chunks[i] = new Chunk(key, new byte[] {0,0,0,0,0,0,0,0, 77, (byte) i});
            System.out.println("==== Storage statistics before:\n" + dumpPeers(allPeers, key));
            System.out.println("Putting chunk" + i);
            p0.netStore.put(chunks[i]);
            TestAsyncPipe.waitForCompletion();
            System.out.println("==== Storage statistics after:\n" + dumpPeers(allPeers, key));
        }

        System.out.println("Waiting for net idle ");
        TestAsyncPipe.waitForCompletion();

        TestPeer.MessageOut = true;

        System.out.println("==== Storage statistics:");

        System.out.println("Name\tChunks\tPeers\tMsgIn\tMsgOut");
        for (TestPeer peer : allPeers) {
            System.out.println(peer.name + "\t" +
                    (int)((Statter.SimpleStatter)((MemStore) peer.localStore.memStore).statCurChunks).getLast() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statHandshakes)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statInMsg)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statOutMsg)).getCount());
            for (Key key : ((MemStore) peer.localStore.memStore).store.keySet()) {
                System.out.println("    " + key);
            }
        }

//        TestPeer.MessageOut = true;
        System.out.println("Requesting chunk from the last...");
        for (int i = 0; i < chunks.length; i++) {
            Key key = new Key(sha3(new byte[]{0x22, (byte) (i+shift)}));
            System.out.println("======== Looking for " + key);
            Chunk chunk1 = allPeers[allPeers.length - 1].netStore.get(key);
            System.out.println("########### Found: " + chunk1);
            Assert.assertEquals(key, chunk1.getKey());
            Assert.assertArrayEquals(chunks[i].getData(), chunk1.getData());
        }

        System.out.println("All found!");

        System.out.println("==== Storage statistics:");

        System.out.println("Name\tChunks\tPeers\tMsgIn\tMsgOut");
        for (TestPeer peer : allPeers) {
            System.out.println(peer.name + "\t" +
                    (int)((Statter.SimpleStatter)((MemStore) peer.localStore.memStore).statCurChunks).getLast() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statHandshakes)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statInMsg)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statOutMsg)).getCount());
//            for (Key key : ((MemStore) peer.localStore.memStore).store.keySet()) {
//                System.out.println("    " + key);
//            }
        }

    }

    @Ignore // OutOfMemory
    @Test
    public void manyPeersLargeDataTest() {
        TestPeer.AsyncPipe = true;
//        TestPeer.MessageOut = true;

        TestPeer p0 = new TestPeer(0);

        System.out.println("Creating chain of peers");
        TestPeer[] allPeers = new TestPeer[100];
        allPeers[0] = p0;
        for (int i = 1; i < allPeers.length; i++) {
            allPeers[i] = new TestPeer(i);
            System.out.println("Connecting " + i + " <=> " + (i-1));
            allPeers[i].connect(allPeers[i-1]);
        }

        TreeChunker chunker = new TreeChunker();
        int chunks = 100;
        byte[] data = new byte[(int) (chunks * chunker.getChunkSize())];
        for (int i = 0; i < chunks; i++) {
            for (int idx = (int) (i * chunker.getChunkSize()); idx < (i+1) * chunker.getChunkSize(); idx++) {
                data[idx] = (byte) (i + 1);
            }
        }

        System.out.println("Split and put data to node 0...");
        Key key = chunker.split(new Util.ArrayReader(data),
                new Util.ChunkConsumer(p0.netStore));

        System.out.println("Assemble data back from the last node ...");
        SectionReader reader = chunker.join(allPeers[allPeers.length - 1].netStore, key);
        Assert.assertEquals(data.length, reader.getSize());
        byte[] data1 = new byte[(int) reader.getSize()];
        reader.read(data1, 0);
        for (int i = 0; i < data.length; i++) {
            if (data[i] != data1[i]) {
                System.out.println("Not equal at index " + i);
            }
            Assert.assertEquals(data[i], data1[i]);
        }

        System.out.println("==== Storage statistics:");

        System.out.println("Name\tChunks\tPeers\tMsgIn\tMsgOut");
        for (TestPeer peer : allPeers) {
            System.out.println(peer.name + "\t" +
                    (int)((Statter.SimpleStatter)((MemStore) peer.localStore.memStore).statCurChunks).getLast() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statHandshakes)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statInMsg)).getCount() + "\t" +
                    ((Statter.SimpleStatter)(peer.netStore.statOutMsg)).getCount());
        }

    }

    @Test
    public void simpleTest() {
        PeerAddress peerAddress1 = new PeerAddress(new byte[] {0,0,0,1}, 1001, new byte[] {1});
        PeerAddress peerAddress2 = new PeerAddress(new byte[] {0,0,0,2}, 1002, new byte[] {2});
        LocalStore localStore1 = new LocalStore(new MemStore(), new MemStore());
        LocalStore localStore2 = new LocalStore(new MemStore(), new MemStore());
        Hive hive1 = new SimpleHive(peerAddress1);
        Hive hive2 = new SimpleHive(peerAddress2);
        NetStore netStore1 = new NetStore(localStore1, hive1);
        NetStore netStore2 = new NetStore(localStore2, hive2);


        netStore1.start(peerAddress1);
        netStore2.start(peerAddress2);

        BzzProtocol bzz1 = new BzzProtocol(netStore1);
        BzzProtocol bzz2 = new BzzProtocol(netStore2);

        TestPipe pipe = new TestPipe(bzz1, bzz2);
        pipe.setNames("1", "2");

        bzz1.setMessageSender(pipe.createIn1());
        bzz2.setMessageSender(pipe.createIn2());
        bzz1.start();
        bzz2.start();

        Key key = new Key(new byte[]{0x22, 0x33});
        Chunk chunk = new Chunk(key, new byte[] {0,0,0,0,0,0,0,0, 77, 88});
        netStore1.put(chunk);
//        netStore1.put(chunk);
        localStore1.clean();
        Chunk chunk1 = netStore1.get(key);
        Assert.assertEquals(key, chunk1.getKey());
        Assert.assertArrayEquals(chunk.getData(), chunk1.getData());
    }
}
