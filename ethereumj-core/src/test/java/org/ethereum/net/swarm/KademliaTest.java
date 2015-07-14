package org.ethereum.net.swarm;

import org.ethereum.net.rlpx.Node;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import org.hibernate.internal.util.collections.IdentitySet;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * Created by Admin on 01.07.2015.
 */
public class KademliaTest {

    @Ignore
    @Test
    public void nodesConnectivityTest() {
        Map<Node, Integer> nameMap = new IdentityHashMap<>();
        Node[] nodes = new Node[300];
        NodeTable table = getTestNodeTable();
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = getNode(1000 + i);
            table.addNode(nodes[i]);
            nameMap.put(nodes[i], i);
        }

        Map<Node, Set<Node>> reachable = new IdentityHashMap<>();

        for (Node node : nodes) {
            Set<Node> reachnodes = new IdentitySet();
            reachable.put(node, reachnodes);
            List<Node> closestNodes = table.getClosestNodes(node.getId());
            int max = 16;
            for (Node closestNode : closestNodes) {
                reachnodes.add(closestNode);
                if (--max == 0) break;
            }
        }

        for (Node node : nodes) {
            System.out.println("Closing node " + nameMap.get(node));
            Set<Node> closure = new IdentitySet();
            addAll(reachable, reachable.get(node), closure);
            reachable.put(node, closure);
        }

        for (Map.Entry<Node, Set<Node>> entry : reachable.entrySet()) {
            System.out.println("Node " + nameMap.get(entry.getKey())
                    + " has " + entry.getValue().size() + " neighbours");
//            for (Node nb : entry.getValue()) {
//                System.out.println("    " + nameMap.get(nb));
//            }
        }
    }

    static Random gen = new Random(0);
    public static byte[] getNodeId() {
        byte[] id = new byte[64];
        gen.nextBytes(id);
        return id;
    }

    public static Node getNode(int port) {
        return new Node(getNodeId(), "127.0.0.1", port);
    }

    public static NodeTable getTestNodeTable() {
        NodeTable testTable = new NodeTable(getNode(3333));
        return testTable;
    }

    private void addAll(Map<Node, Set<Node>> reachableMap, Set<Node> reachable, Set<Node> ret) {
        for (Node node : reachable) {
            if (!ret.contains(node)) {
                ret.add(node);
                addAll(reachableMap, reachableMap.get(node), ret);
            }
        }
    }
}
