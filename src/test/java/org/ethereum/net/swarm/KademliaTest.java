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
import org.ethereum.net.rlpx.discover.table.NodeTable;
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
            Map<Node, Object> reachnodes = new IdentityHashMap<>();
            reachable.put(node, reachnodes.keySet());
            List<Node> closestNodes = table.getClosestNodes(node.getId());
            int max = 16;
            for (Node closestNode : closestNodes) {
                reachnodes.put(closestNode, null);
                if (--max == 0) break;
            }
        }

        for (Node node : nodes) {
            System.out.println("Closing node " + nameMap.get(node));
            Map<Node, Object> closure = new IdentityHashMap<>();
            addAll(reachable, reachable.get(node), closure.keySet());
            reachable.put(node, closure.keySet());
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
