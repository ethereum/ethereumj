package org.ethereum.trie;

import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.ethereum.util.ByteUtil.length;
import static org.ethereum.util.ByteUtil.wrap;
import static org.ethereum.util.Value.fromRlpEncoded;

/**
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Cache {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private KeyValueDataSource dataSource;
    private Map<ByteArrayWrapper, Node> nodes = new HashMap<>();
    private Set<ByteArrayWrapper> removedNodes = new HashSet<>();
    private boolean isDirty;
    
    public Cache(KeyValueDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void markRemoved(byte[] key) {
        ByteArrayWrapper keyW = new ByteArrayWrapper(key);
        removedNodes.add(keyW);
        nodes.remove(keyW);
    }

    /**
     * Put the node in the cache if RLP encoded value is longer than 32 bytes
     *
     * @param o the Node which could be a pair-, multi-item Node or single Value
     * @return keccak hash of RLP encoded node if length &gt; 32 otherwise return node itself
     */
    public Object put(Object o) {
        Value value = new Value(o);
        byte[] enc = value.encode();
        if (enc.length >= 32) {
            byte[] sha = value.hash();
            ByteArrayWrapper key = wrap(sha);
            this.nodes.put(key, new Node(value, true));
            this.removedNodes.remove(key);
            this.isDirty = true;

            return sha;
        }
        return value;
    }

    public Value get(byte[] key) {
        ByteArrayWrapper wrappedKey = wrap(key);
        // First check if the key is the cache
        Node node = this.nodes.get(wrappedKey);
        if (node == null) {
            byte[] data = (this.dataSource == null) ? null : this.dataSource.get(key);
            node = new Node(fromRlpEncoded(data), false);

            this.nodes.put(wrappedKey, node);
        }

        return node.getValue();
    }

    public void delete(byte[] key) {
        ByteArrayWrapper wrappedKey = wrap(key);
        this.nodes.remove(wrappedKey);

        if (dataSource != null) {
            this.dataSource.delete(key);
        }
    }

    public void commit() {
        // Don't try to commit if it isn't dirty
        if ((dataSource == null) || !this.isDirty) return;

        long start = System.nanoTime();

        int batchMemorySize = 0;
        Map<byte[], byte[]> batch = new HashMap<>();
        for (ByteArrayWrapper nodeKey : this.nodes.keySet()) {
            Node node = this.nodes.get(nodeKey);

            if (node == null || node.isDirty()) {
                byte[] value;
                if (node != null) {
                    node.setDirty(false);
                    value = node.getValue().encode();
                } else {
                    value = null;
                }

                byte[] key = nodeKey.getData();

                batch.put(key, value);
                batchMemorySize += length(key, value);
            }
        }
        for (ByteArrayWrapper removedNode : removedNodes) {
            batch.put(removedNode.getData(), null);
        }

        this.dataSource.updateBatch(batch);
        this.isDirty = false;
        this.nodes.clear();
        this.removedNodes.clear();

    }

    public void undo() {
        Iterator<Map.Entry<ByteArrayWrapper, Node>> iter = this.nodes.entrySet().iterator();
        while (iter.hasNext()) {
            if (iter.next().getValue().isDirty()) {
                iter.remove();
            }
        }
        this.isDirty = false;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public Map<ByteArrayWrapper, Node> getNodes() {
        return nodes;
    }

    public KeyValueDataSource getDb() {
        return dataSource;
    }

    public String cacheDump() {
        StringBuffer cacheDump = new StringBuffer();
        for (ByteArrayWrapper key : nodes.keySet()) {
            Node node = nodes.get(key);
            if (node.getValue() != null)
                cacheDump.append(key.toString()).append(" : ").append(node.getValue().toString()).append("\n");
        }

        return cacheDump.toString();
    }

    public void setDB(KeyValueDataSource dataSource) {
        if (this.dataSource == dataSource) return;

        Map<byte[], byte[]> rows = new HashMap<>();
        if (this.dataSource == null) {
            for (ByteArrayWrapper key : nodes.keySet()) {
                Node node = nodes.get(key);
                if (node == null) {
                    rows.put(key.getData(), null);
                }else if (!node.isDirty()) {
                    rows.put(key.getData(), node.getValue().encode());
                }
            }
        } else {
            for (byte[] key : this.dataSource.keys()) {
                rows.put(key, this.dataSource.get(key));
            }
            this.dataSource.close();
        }

        dataSource.updateBatch(rows);
        this.dataSource = dataSource;
    }
}
