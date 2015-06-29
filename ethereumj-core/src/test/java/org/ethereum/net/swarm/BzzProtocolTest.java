package org.ethereum.net.swarm;

import org.ethereum.net.swarm.bzz.BzzMessage;
import org.ethereum.net.swarm.bzz.BzzProtocol;
import org.ethereum.net.swarm.bzz.PeerAddress;
import org.ethereum.util.Functional;
import org.hibernate.internal.util.collections.IdentitySet;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static java.lang.Math.min;

/**
 * Created by Admin on 24.06.2015.
 */
public class BzzProtocolTest {

    public static class TestPipe {
        private Functional.Consumer<BzzMessage> out1;
        private Functional.Consumer<BzzMessage> out2;
        private String name1, name2;

        public TestPipe(Functional.Consumer<BzzMessage> out1, Functional.Consumer<BzzMessage> out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        Functional.Consumer<BzzMessage> createIn1() {
            return new Functional.Consumer<BzzMessage>() {
                @Override
                public void accept(BzzMessage bzzMessage) {
                    BzzMessage smsg = serialize(bzzMessage);
                    System.out.println(name1 + " => " + name2 + ": " + smsg);
                    out2.accept(smsg);
                }
            };
        }
        Functional.Consumer<BzzMessage> createIn2() {
            return new Functional.Consumer<BzzMessage>() {
                @Override
                public void accept(BzzMessage bzzMessage) {
                    BzzMessage smsg = serialize(bzzMessage);
                    System.out.println(name2 + " => " + name1 + ": " + smsg);
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

    public static class SimpleHive extends Hive {
        Set<BzzProtocol> peers = new IdentitySet();
        PeerAddress thisAddress;
        TestPeer thisPeer;

        public SimpleHive(PeerAddress thisAddress) {
            this.thisAddress = thisAddress;
        }

        public SimpleHive setThisPeer(TestPeer thisPeer) {
            this.thisPeer = thisPeer;
            return this;
        }

        Key distance(Key k1, Key k2) {
            byte[] res = new byte[min(k1.getBytes().length, k2.getBytes().length)];
            for (int i = 0; i < res.length; i++) {
                res[i] = (byte) (k1.getBytes()[i] ^ k2.getBytes()[i]);
            }
            return new Key(res);
        }

        @Override
        public void addPeer(BzzProtocol peer) {
            peers.add(peer);
        }

        @Override
        public void removePeer(BzzProtocol peer) {
            peers.remove(peer);
        }

        @Override
        public Collection<BzzProtocol> getPeers(Key key, int max) {
            if (thisPeer == null) return peers;
            TreeMap<Key, TestPeer> sort = new TreeMap<Key, TestPeer>(new Comparator<Key>() {
                @Override
                public int compare(Key o1, Key o2) {
                    for (int i = 0; i < o1.getBytes().length; i++) {
                        if (o1.getBytes()[i] > o2.getBytes()[i]) return 1;
                        if (o1.getBytes()[i] < o2.getBytes()[i]) return -1;
                    }
                    return 0;
                }
            });
            for (TestPeer testPeer : TestPeer.staticMap.values()) {
                if (thisPeer != testPeer) {
                    sort.put(distance(key, new Key(testPeer.peerAddress.getId())), testPeer);
                }
            }
            ArrayList<BzzProtocol> ret = new ArrayList<>();
            for (Map.Entry<Key, TestPeer> keyTestPeerEntry : sort.entrySet()) {
                ret.add(thisPeer.getPeer(keyTestPeerEntry.getValue().peerAddress));
                if (--max == 0) break;
            }
            return ret;
        }
    }

    public static class TestPeer {
        static Map<PeerAddress, TestPeer> staticMap = new HashMap<>();

        String name;
        PeerAddress peerAddress;

        LocalStore localStore;
        Hive hive;
        NetStore netStore;

        Map<Key, BzzProtocol> connections = new HashMap<>();

        public TestPeer(int num) {
            this(new PeerAddress(new byte[]{0, 0, (byte) ((num >> 8) & 0xFF), (byte) (num & 0xFF)}, 1000 + num,
                    new byte[]{(byte) ((num >> 8) & 0xFF), (byte) (num & 0xFF)}), "" + num);
        }

        public TestPeer(PeerAddress peerAddress, String name) {
            this.name = name;
            this.peerAddress = peerAddress;
            localStore = new LocalStore(new MemStore(), new MemStore());
            hive = new SimpleHive(peerAddress).setThisPeer(this);
            netStore = new NetStore(localStore, hive);

            netStore.start(peerAddress.toPeerInfo(), null);

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

            TestPipe pipe = new TestPipe(myBzz, peerBzz);
            pipe.setNames(this.name, peer.name);
            System.out.println("Connecting: " + this.name + " <=> " + peer.name);
            myBzz.setMessageSender(pipe.createIn1());
            peerBzz.setMessageSender(pipe.createIn2());
            myBzz.start(peer.peerAddress.toPeer());
            peerBzz.start(this.peerAddress.toPeer());
        }

        public void connect(PeerAddress addr) {
            TestPeer peer = staticMap.get(addr);
            if (peer != null) {
                connect(peer);
            }
        }
    }

//    @Test
    public void simple3PeersTest() {
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

        System.out.println("Requesting chunk from 3...");
        Chunk chunk1 = p3.netStore.get(key);
        Assert.assertEquals(key, chunk1.getKey());
        Assert.assertArrayEquals(chunk.getData(), chunk1.getData());
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


        netStore1.start(peerAddress1.toPeerInfo(), null);
        netStore2.start(peerAddress2.toPeerInfo(), null);

        BzzProtocol bzz1 = new BzzProtocol(netStore1);
        BzzProtocol bzz2 = new BzzProtocol(netStore2);

        TestPipe pipe = new TestPipe(bzz1, bzz2);
        pipe.setNames("1", "2");

        bzz1.setMessageSender(pipe.createIn1());
        bzz2.setMessageSender(pipe.createIn2());
        bzz1.start(peerAddress2.toPeer());
        bzz2.start(peerAddress1.toPeer());

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
