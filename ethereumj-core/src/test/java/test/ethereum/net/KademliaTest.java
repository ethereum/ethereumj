package test.ethereum.net;

import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.*;
import org.ethereum.net.rlpx.discover.NodeBucket;
import org.ethereum.net.rlpx.discover.NodeEntry;
import org.ethereum.net.rlpx.discover.NodeTable;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.merge;
import static org.junit.Assert.*;

public class KademliaTest {

    @Test
    public void test1() {
        //init table with one home node
        NodeTable t = getTestNodeTable(0);
        Node homeNode = t.getNode();

        //table should contain the home node only
        assertEquals(t.getAllNodes().size(), 1);

        Node bucketNode = t.getAllNodes().get(0).getNode();

        assertEquals(homeNode, bucketNode);

    }

    @Test
    public void test2() {
        NodeTable t = getTestNodeTable(0);
        Node n = getNode();
        t.addNode(n);

        assertTrue(containsNode(t, n));

        t.dropNode(n);

        assertFalse(containsNode(t, n));
    }

    @Test
    public void test3() {
        NodeTable t = getTestNodeTable(1000);
        showBuckets(t);

        List<Node> closest1 = t.getClosestNodes(t.getNode().getId());
        List<Node> closest2 = t.getClosestNodes(getNodeId());

        assertNotEquals(closest1, closest2);
    }

    public static byte[] getNodeId() {
        Random gen = new Random();
        byte[] id = new byte[64];
        gen.nextBytes(id);
        return id;
    }

    public static Node getNode() {
        return new Node(getNodeId(), "127.0.0.1", 30303);
    }

    public static NodeTable getTestNodeTable(int nodesQuantity) {
        NodeTable testTable = new NodeTable(getNode());

        for (int i = 0; i < nodesQuantity; i++) {
            testTable.addNode(getNode());
        }

        return testTable;
    }

    public static void showBuckets(NodeTable t) {
        for (NodeBucket b : t.getBuckets()) {
            if (b.getNodesCount() > 0) {
                System.out.println(String.format("Bucket %d nodes %d depth %d", b.getDepth(), b.getNodesCount(), b.getDepth()));
            }
        }
    }

    public static boolean containsNode(NodeTable t, Node n) {
        for (NodeEntry e : t.getAllNodes()) {
            if (e.getNode().toString().equals(n.toString())) {
                return true;
            }
        }
        return false;
    }
}
