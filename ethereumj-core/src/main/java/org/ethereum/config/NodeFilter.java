package org.ethereum.config;

import org.ethereum.net.rlpx.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 14.01.2016.
 */
public class NodeFilter {
    private List<Entry> entries = new ArrayList<>();

    public void add(byte[] nodeId, String hostIpPattern) {
        entries.add(new Entry(nodeId, hostIpPattern));
    }

    public boolean accept(Node node) {
        for (Entry entry : entries) {
            if (entry.accept(node)) return true;
        }
        return false;
    }

    public boolean accept(InetAddress nodeAddr) {
        for (Entry entry : entries) {
            if (entry.accept(nodeAddr)) return true;
        }
        return false;
    }

    private class Entry {
        byte[] nodeId;
        String hostIpPattern;

        public Entry(byte[] nodeId, String hostIpPattern) {
            this.nodeId = nodeId;
            if (hostIpPattern != null) {
                int idx = hostIpPattern.indexOf("*");
                if (idx > 0) {
                    hostIpPattern = hostIpPattern.substring(0, idx);
                }
            }
            this.hostIpPattern = hostIpPattern;
        }

        public boolean accept(InetAddress nodeAddr) {
            String ip = nodeAddr.getHostAddress();
            return hostIpPattern != null && ip.startsWith(hostIpPattern);
        }

        public boolean accept(Node node) {
            try {
                return (nodeId == null || Arrays.equals(node.getId(), nodeId))
                        && (hostIpPattern == null || accept(InetAddress.getByName(node.getHost())));
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }
}
