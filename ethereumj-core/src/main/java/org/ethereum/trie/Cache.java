package org.ethereum.trie;

import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.Value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Cache {

    private final KeyValueDataSource dataSource;
    private Map<ByteArrayWrapper, Node> nodes = new ConcurrentHashMap<>();
    private boolean isDirty;

    public Cache(KeyValueDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Put the node in the cache if RLP encoded value is longer than 32 bytes
     *
     * @param o the Node which could be a pair-, multi-item Node or single Value
     * @return sha3 hash of RLP encoded node if length &gt; 32 otherwise return node itself
     */
    public Object put(Object o) {
        Value value = new Value(o);
        byte[] enc = value.encode();
        if (enc.length >= 32) {
            byte[] sha = HashUtil.sha3(enc);
            this.nodes.put(new ByteArrayWrapper(sha), new Node(value, true));
            this.isDirty = true;
            return sha;
        }
        return value;
    }

    public Value get(byte[] key) {
        ByteArrayWrapper keyObj = new ByteArrayWrapper(key);
        // First check if the key is the cache
        if (this.nodes.get(keyObj) != null) {
            return this.nodes.get(keyObj).getValue();
        }

        // Get the key of the database instead and cache it
        byte[] data = this.dataSource.get(key);
        Value value = Value.fromRlpEncoded(data);
        // Create caching node
        this.nodes.put(keyObj, new Node(value, false));

        return value;
    }

    public void delete(byte[] key) {
        ByteArrayWrapper keyObj = new ByteArrayWrapper(key);
        this.nodes.remove(keyObj);

        if (dataSource == null) return;
        this.dataSource.delete(key);
    }

    public void commit() {

        if (dataSource == null) return;

        // Don't try to commit if it isn't dirty
        if (!this.isDirty) {
            return;
        }


        Map<byte[], byte[]> batch = new HashMap<>();
        for (ByteArrayWrapper key : this.nodes.keySet()) {
            Node node = this.nodes.get(key);
            if (node.isDirty()) {
                batch.put(key.getData(), node.getValue().encode());
                node.setDirty(false);
            }
        }

        dataSource.updateBatch(batch);
        this.isDirty = false;
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
}
