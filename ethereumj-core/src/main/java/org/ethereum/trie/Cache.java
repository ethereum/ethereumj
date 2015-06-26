package org.ethereum.trie;

import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.ethereum.util.ByteUtil.wrap;
import static org.ethereum.util.Value.fromRlpEncoded;

/**
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Cache {

    private static final Logger gLogger = LoggerFactory.getLogger("general");

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
            byte[] sha = value.hash();
            this.nodes.put(wrap(sha), new Node(value, true));
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
            byte[] data = this.dataSource.get(key);
            node = new Node(fromRlpEncoded(data), false);
            
            this.nodes.put(wrappedKey, node);
        }

        return node.getValue();
    }

    public void delete(byte[] key) {
        this.nodes.remove(wrap(key));

        if (dataSource == null) return;
        this.dataSource.delete(key);
    }

    public void commit() {
        // Don't try to commit if it isn't dirty
        if ((dataSource == null) || !this.isDirty) return;

        long start = System.nanoTime();

        long totalSize = 0;
        Map<byte[], byte[]> batch = new HashMap<>();
        for (ByteArrayWrapper key : this.nodes.keySet()) {
            Node node = this.nodes.get(key);
            
            if (node.isDirty()) {
                node.setDirty(false);
                
                byte[] value = node.getValue().encode();
                batch.put(key.getData(), value);

                totalSize += value.length;
            }
        }

        dataSource.updateBatch(batch);
        this.isDirty = false;
        this.nodes.clear();

        long finish = System.nanoTime();
        
        float flushSize = (float) totalSize / 1048576;
        float flushTime = (float) (finish - start) / 1_000_000;
        gLogger.info(format("Flush state in: %02.2f ms, %d nodes, %02.2fMB", flushTime, batch.size(), flushSize));
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
