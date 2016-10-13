package org.ethereum.datasource.test;

import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.trie.TraceAllNodes;
import org.ethereum.trie.TrieIterator;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.copyOfRange;
import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.matchingNibbleLength;
import static org.ethereum.util.CompactEncoder.*;
import static org.spongycastle.util.Arrays.concatenate;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class TrieImpl implements Trie<byte[]> {


    private static final Logger logger = LoggerFactory.getLogger("trie");

    private static byte PAIR_SIZE = 2;
    private static byte LIST_SIZE = 17;

//    private Object prevRoot;
    Source<byte[], Value> cache;
    Object  root;

    public TrieImpl(Source<byte[], Value> cache, byte[] root) {
        this.cache = cache;
        if (root != null && FastByteComparisons.equal(root, EMPTY_TRIE_HASH)) {
            root = null;
        }
        this.root = root;
    }

//    public TrieImpl(KeyValueDataSource db) {
//        this(db, "");
//    }
//
//    public TrieImpl(KeyValueDataSource db, Object root) {
//        this.root = root;
//        this.prevRoot = root;
//    }

    public TrieIterator getIterator() {
        throw new RuntimeException("Not supported");
    }

//    public void setCache(Cache cache) {
//        this.cache = cache;
//    }

//    public Cache getCache() {
//        return this.cache;
//    }

//    public Object getPrevRoot() {
//        return prevRoot;
//    }

    public Object getRoot() {
        return root;
    }

    public void setRoot(byte[] root) {
        this.root = root;
    }

    public void deserializeRoot(byte[] data){
        synchronized (cache) {
            try {
                ByteArrayInputStream b = new ByteArrayInputStream(data);
                ObjectInputStream o = new ObjectInputStream(b);
                root = o.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

//    public boolean isPruningEnabled() {
//        return pruningEnabled;
//    }
//
//    public TrieImpl withPruningEnabled(boolean pruningEnabled) {
//        this.pruningEnabled = pruningEnabled;
//        return this;
//    }

    /**************************************
     * Public (query) interface functions *
     **************************************/

    /**
     * Retrieve a value from a key as String.
     */
    public byte[] get(String key) {
        return this.get(key.getBytes());
    }

    @Override
    public byte[] get(byte[] key) {
        synchronized (cache) {
            if (logger.isDebugEnabled())
                logger.debug("Retrieving key {}", Hex.toHexString(key));
            byte[] k = binToNibbles(key);
            Value c = new Value(this.get(this.root, k));

            return c.asBytes();
        }
    }

    /**
     * Insert key/value pair into trie.
     */
    @Override
    public void put(byte[] key, byte[] value) {
        if (key == null)
            throw new NullPointerException("Key should not be blank");
        synchronized (cache) {
            byte[] k = binToNibbles(key);

            if (isEmptyNode(root)) {
                cache.delete(getRootHash());
            }
            this.root = this.insertOrDelete(this.root, k, value);
            if (logger.isDebugEnabled()) {
                logger.debug("Added key {} and value {}", Hex.toHexString(key), Hex.toHexString(value));
                logger.debug("New root-hash: {}", Hex.toHexString(this.getRootHash()));
            }
        }
    }

    /**
     * Delete a key/value pair from the trie.
     */
    public void delete(String key) {
        this.put(key.getBytes(), EMPTY_BYTE_ARRAY);
    }

    @Override
    public void delete(byte[] key) {
        synchronized (cache) {
            this.put(key, EMPTY_BYTE_ARRAY);
            if (logger.isDebugEnabled()) {
                logger.debug("Deleted value for key {}", Hex.toHexString(key));
                logger.debug("New root-hash: {}", Hex.toHexString(this.getRootHash()));
            }
        }
    }

    @Override
    public byte[] getRootHash() {
        synchronized (cache) {
            if (root == null
                    || (root instanceof byte[] && ((byte[]) root).length == 0)
                    || (root instanceof String && "".equals(root))) {
                return EMPTY_TRIE_HASH;
            } else if (root instanceof byte[]) {
                return (byte[]) this.getRoot();
            } else {
                Value rootValue = new Value(this.getRoot());
                return rootValue.hash();
            }
        }
    }

    /****************************************
     *          Private functions           *
     ****************************************/

    private Object get(Object node, byte[] key) {
        synchronized (cache) {

            // Return the node if key is empty (= found)
            if (key.length == 0 || isEmptyNode(node)) {
                return node;
            }

            Value currentNode = this.getNode(node);
            if (currentNode == null) return null;

            if (currentNode.length() == PAIR_SIZE) {
                // Decode the key
                byte[] k = unpackToNibbles(currentNode.get(0).asBytes());
                Object v = currentNode.get(1).asObj();

                if (key.length >= k.length && Arrays.equals(k, copyOfRange(key, 0, k.length))) {
                    return this.get(v, copyOfRange(key, k.length, key.length));
                } else {
                    return "";
                }
            } else {
                return this.get(currentNode.get(key[0]).asObj(), copyOfRange(key, 1, key.length));
            }
        }
    }

    private Object insertOrDelete(Object node, byte[] key, byte[] value) {
        if (value.length != 0) {
            return this.insert(node, key, value);
        } else {
            return this.delete(node, key);
        }
    }

    /**
     * Update or add the item inside a node.
     * @return the updated node with rlp encoded
     */
    private Object insert(Object node, byte[] key, Object value) {

        if (key.length == 0) {
            return value;
        }

        if (isEmptyNode(node)) {
            Object[] newNode = new Object[]{packNibbles(key), value};
            return this.putToCache(newNode);
        }

        Value currentNode = this.getNode(node);

        if (currentNode == null) {
            throw new RuntimeException("Invalid Trie state, missing node " + new Value(node));
        }

        // Check for "special" 2 slice type node
        if (currentNode.length() == PAIR_SIZE) {
            // Decode the key
            byte[] k = unpackToNibbles(currentNode.get(0).asBytes());
            Object v = currentNode.get(1).asObj();

            // Matching key pair (ie. there's already an object with this key)
            if (Arrays.equals(k, key)) {
                Object[] newNode = new Object[]{packNibbles(key), value};
                return this.putToCache(newNode);
            }

            Object newHash;
            int matchingLength = matchingNibbleLength(key, k);
            if (matchingLength == k.length) {
                // Insert the hash, creating a new node
                byte[] remainingKeypart = copyOfRange(key, matchingLength, key.length);
                newHash = this.insert(v, remainingKeypart, value);

            } else {

                // Expand the 2 length slice to a 17 length slice
                // Create two nodes to putToCache into the new 17 length node
                Object oldNode = this.insert("", copyOfRange(k, matchingLength + 1, k.length), v);
                Object newNode = this.insert("", copyOfRange(key, matchingLength + 1, key.length), value);

                // Create an expanded slice
                Object[] scaledSlice = emptyStringSlice(17);

                // Set the copied and new node
                scaledSlice[k[matchingLength]] = oldNode;
                scaledSlice[key[matchingLength]] = newNode;
                newHash = this.putToCache(scaledSlice);
            }

            markRemoved(currentNode.hash());

            if (matchingLength == 0) {
                // End of the chain, return
                return newHash;
            } else {
                Object[] newNode = new Object[]{packNibbles(copyOfRange(key, 0, matchingLength)), newHash};
                return this.putToCache(newNode);
            }
        } else {

            // Copy the current node over to the new node
            Object[] newNode = copyNode(currentNode);

            // Replace the first nibble in the key
            newNode[key[0]] = this.insert(currentNode.get(key[0]).asObj(), copyOfRange(key, 1, key.length), value);

            if (!FastByteComparisons.equal(getNode(newNode).hash(), currentNode.hash())) {
                markRemoved(currentNode.hash());
                if (!isEmptyNode(currentNode.get(key[0]))) {
                    markRemoved(currentNode.get(key[0]).asBytes());
                }
            }

            return this.putToCache(newNode);
        }
    }

    private Object delete(Object node, byte[] key) {

        if (key.length == 0 || isEmptyNode(node)) {
            return "";
        }

        // New node
        Value currentNode = this.getNode(node);
        if (currentNode == null) {
            throw new RuntimeException("Invalid Trie state, missing node " + new Value(node));
        }

        // Check for "special" 2 slice type node
        if (currentNode.length() == PAIR_SIZE) {
            // Decode the key
            byte[] k = unpackToNibbles(currentNode.get(0).asBytes());
            Object v = currentNode.get(1).asObj();

            // Matching key pair (ie. there's already an object with this key)
            if (Arrays.equals(k, key)) {
                return "";
            } else if (Arrays.equals(copyOfRange(key, 0, k.length), k)) {
                Object hash = this.delete(v, copyOfRange(key, k.length, key.length));
                Value child = this.getNode(hash);

                Object newNode;
                if (child.length() == PAIR_SIZE) {
                    byte[] newKey = concatenate(k, unpackToNibbles(child.get(0).asBytes()));
                    newNode = new Object[]{packNibbles(newKey), child.get(1).asObj()};
                } else {
                    newNode = new Object[]{currentNode.get(0), hash};
                }
                markRemoved(currentNode.hash());
                return this.putToCache(newNode);
            } else {
                return node;
            }
        } else {
            // Copy the current node over to a new node
            Object[] itemList = copyNode(currentNode);

            // Replace the first nibble in the key
            itemList[key[0]] = this.delete(itemList[key[0]], copyOfRange(key, 1, key.length));

            byte amount = -1;
            for (byte i = 0; i < LIST_SIZE; i++) {
                if (itemList[i] != "") {
                    if (amount == -1) {
                        amount = i;
                    } else {
                        amount = -2;
                    }
                }
            }

            Object[] newNode = null;
            if (amount == 16) {
                newNode = new Object[]{packNibbles(new byte[]{16}), itemList[amount]};
            } else if (amount >= 0) {
                Value child = this.getNode(itemList[amount]);
                if (child.length() == PAIR_SIZE) {
                    key = concatenate(new byte[]{amount}, unpackToNibbles(child.get(0).asBytes()));
                    newNode = new Object[]{packNibbles(key), child.get(1).asObj()};
                } else if (child.length() == LIST_SIZE) {
                    newNode = new Object[]{packNibbles(new byte[]{amount}), itemList[amount]};
                }
            } else {
                newNode = itemList;
            }


            if (!FastByteComparisons.equal(getNode(newNode).hash(), currentNode.hash())) {
                markRemoved(currentNode.hash());
            }

            return this.putToCache(newNode);
        }
    }

    private void markRemoved(byte[] hash) {
        cache.delete(hash);
    }

    /**
     * Helper method to retrieve the actual node. If the node is not a list and length
     * is > 32 bytes get the actual node from the db.
     */
    private Value getNode(Object node) {

        Value val = new Value(node);

        // in that case we got a node
        // so no need to encode it
        if (!val.isBytes()) {
            return val;
        }

        byte[] keyBytes = val.asBytes();
        if (keyBytes.length == 0) {
            return val;
        } else if (keyBytes.length < 32) {
            return new Value(keyBytes);
        }
        return this.cache.get(keyBytes);
    }

    private Object putToCache(Object node) {
        Value value = new Value(node);
        byte[] enc = value.encode();
        if (enc.length >= 32) {
            byte[] sha = value.hash();
            cache.put(sha, value);

            return sha;
        }
        return value;
    }

    private boolean isEmptyNode(Object node) {
        Value n = new Value(node);
        return (node == null || (n.isString() && (n.asString().isEmpty() || n.get(0).isNull())) || n.length() == 0);
    }

    private Object[] copyNode(Value currentNode) {
        Object[] itemList = emptyStringSlice(LIST_SIZE);
        for (int i = 0; i < LIST_SIZE; i++) {
            Object cpy = currentNode.get(i).asObj();
            if (cpy != null)
                itemList[i] = cpy;
        }
        return itemList;
    }

    // Simple compare function which compares two tries based on their stateRoot
    @Override
    public boolean equals(Object trie) {
        if (this == trie) return true;
        return trie instanceof org.ethereum.trie.Trie && Arrays.equals(this.getRootHash(), ((org.ethereum.trie.Trie) trie).getRootHash());
    }

    public void sync() {
        synchronized (cache) {
//            this.cache.commit();
//            this.prevRoot = this.root;
        }
    }

    public void undo() {
        synchronized (cache) {
//            this.cache.undo();
//            this.root = this.prevRoot;
        }
    }

    // Returns a copy of this trie
    public TrieImpl copy() {
//        synchronized (cache) {
//            TrieImpl trie = new TrieImpl(this.cache.getDb(), this.root);
//            for (ByteArrayWrapper key : this.cache.getNodes().keySet()) {
//                Node node = this.cache.getNodes().get(key);
//                trie.cache.getNodes().put(key, node.copy());
//            }
//            return trie;
//        }
        return null;
    }

    /********************************
     *      Utility functions       *
     *******************************/

    // Created an array of empty elements of required length
    private static Object[] emptyStringSlice(int l) {
        Object[] slice = new Object[l];
        for (int i = 0; i < l; i++) {
            slice[i] = "";
        }
        return slice;
    }

    /**
     * Insert/delete operations on a Trie structure
     * leaves the old nodes in cache, this method scans the
     * cache and removes them. The method is not thread
     * safe, the tree should not be modified during the
     * cleaning process.
     */
    public void cleanCache() {
//        synchronized (cache) {
//
//            CollectFullSetOfNodes collectAction = new CollectFullSetOfNodes();
//            long startTime = System.currentTimeMillis();
//
//            this.scanTree(this.getRootHash(), collectAction);
//
//            Set<ByteArrayWrapper> hashSet = collectAction.getCollectedHashes();
//            Map<ByteArrayWrapper, Node> nodes = this.getCache().getNodes();
//            Set<ByteArrayWrapper> toRemoveSet = new HashSet<>();
//
//            for (ByteArrayWrapper key : nodes.keySet())
//                if (!hashSet.contains(key.getData()))
//                    toRemoveSet.add(key);
//
//            for (ByteArrayWrapper key : toRemoveSet) {
//                this.getCache().delete(key.getData());
//
//                if (logger.isTraceEnabled())
//                    logger.trace("Garbage collected node: [{}]",
//                            Hex.toHexString(key.getData()));
//            }
//            logger.info("Garbage collected node list, size: [{}]", toRemoveSet.size());
//            logger.info("Garbage collection time: [{}ms]", System.currentTimeMillis() - startTime);
//        }
    }

    public void printFootPrint() {

//        this.getCache().getNodes();
    }

    public void scanTree(byte[] hash, org.ethereum.trie.TrieImpl.ScanAction scanAction) {
        synchronized (cache) {

            Value node = cache.get(hash);
            if (node == null) {
                throw new RuntimeException("Not found: " + Hex.toHexString(hash));
            }

            if (node.isList()) {
                List<Object> siblings = node.asList();
                if (siblings.size() == PAIR_SIZE) {
                    Value val = new Value(siblings.get(1));
                    if (val.isHashCode() && !hasTerminator((byte[]) siblings.get(0)))
                        scanTree(val.asBytes(), scanAction);
                } else {
                    for (int j = 0; j < LIST_SIZE; ++j) {
                        Value val = new Value(siblings.get(j));
                        if (val.isHashCode())
                            scanTree(val.asBytes(), scanAction);
                    }
                }
                scanAction.doOnNode(hash, node);
            }
        }
    }

    public void deserialize(byte[] data){
//        synchronized (cache) {
//            RLPList rlpList = (RLPList) RLP.decode2(data).get(0);
//
//            RLPItem keysElement = (RLPItem) rlpList.get(0);
//            RLPList valsList = (RLPList) rlpList.get(1);
//            RLPItem root = (RLPItem) rlpList.get(2);
//
//            for (int i = 0; i < valsList.size(); ++i) {
//
//                byte[] val = valsList.get(i).getRLPData();
//                byte[] key = new byte[32];
//
//                Value value = Value.fromRlpEncoded(val);
//                System.arraycopy(keysElement.getRLPData(), i * 32, key, 0, 32);
//                cache.getNodes().put(wrap(key), new Node(value));
//            }
//
//            this.deserializeRoot(root.getRLPData());
//        }
    }

    public byte[] serialize() {

//        synchronized (cache) {
//            Map<ByteArrayWrapper, Node> map = getCache().getNodes();
//
//            int keysTotalSize = 0;
//            int valsTotalSize = 0;
//
//            Set<ByteArrayWrapper> keys = map.keySet();
//            for (ByteArrayWrapper key : keys) {
//                Node node = map.get(key);
//                if (node == null) continue;
//
//                byte[] keyBytes = key.getData();
//                keysTotalSize += keyBytes.length;
//
//                byte[] valBytes = node.getValue().getData();
//                valsTotalSize += valBytes.length + calcElementPrefixSize(valBytes);
//            }
//
//            byte[] root = null;
//            try {
//                ByteArrayOutputStream b = new ByteArrayOutputStream();
//                ObjectOutputStream o = new ObjectOutputStream(b);
//                o.writeObject(this.getRoot());
//                root = b.toByteArray();
//                root = RLP.encodeElement(root);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            byte[] keysHeader = RLP.encodeLongElementHeader(keysTotalSize);
//            byte[] valsHeader = RLP.encodeListHeader(valsTotalSize);
//            byte[] listHeader = RLP.encodeListHeader(keysTotalSize + keysHeader.length +
//                    valsTotalSize + valsHeader.length + root.length);
//
//            byte[] rlpData = new byte[keysTotalSize + keysHeader.length +
//                    valsTotalSize + valsHeader.length + listHeader.length + root.length];
//
//            // copy headers:
//            //      [ rlp_list_header, rlp_keys_header, rlp_keys, rlp_vals_header, rlp_val]
//
//            System.arraycopy(listHeader, 0, rlpData, 0, listHeader.length);
//            System.arraycopy(keysHeader, 0, rlpData, listHeader.length, keysHeader.length);
//            System.arraycopy(valsHeader,
//                    0,
//                    rlpData,
//                    (listHeader.length + keysHeader.length + keysTotalSize),
//                    valsHeader.length);
//            System.arraycopy(root,
//                    0,
//                    rlpData,
//                    (listHeader.length + keysHeader.length + keysTotalSize + valsTotalSize + valsHeader.length),
//                    root.length);
//
//
//            int k_1 = 0;
//            int k_2 = 0;
//            for (ByteArrayWrapper key : keys) {
//                Node node = map.get(key);
//                if (node == null) continue;
//
//                System.arraycopy(key.getData(), 0, rlpData,
//                        (listHeader.length + keysHeader.length + k_1),
//                        key.getData().length);
//
//                k_1 += key.getData().length;
//
//                byte[] valBytes = RLP.encodeElement(node.getValue().getData());
//
//                System.arraycopy(valBytes, 0, rlpData,
//                        listHeader.length + keysHeader.length + keysTotalSize + valsHeader.length + k_2,
//                        valBytes.length);
//                k_2 += valBytes.length;
//            }
//
//            return rlpData;
//        }
        throw new RuntimeException("Not implemented");
    }

    public String getTrieDump() {

        synchronized (cache) {
            TraceAllNodes traceAction = new TraceAllNodes();
            Value value = new Value(root);
            if (value.isHashCode()) {
                this.scanTree(this.getRootHash(), traceAction);
            } else {
                traceAction.doOnNode(this.getRootHash(), value);
            }

            final String root;
            if (this.getRoot() instanceof Value) {
                root = "root: " + Hex.toHexString(getRootHash()) + " => " + this.getRoot() + "\n";
            } else {
                root = "root: " + Hex.toHexString(getRootHash()) + "\n";
            }
            return root + traceAction.getOutput();
        }
    }

    public boolean validate() {
        synchronized (cache) {
            logger.info("Validating state trie...");
            final int[] cnt = new int[1];
            try {
                scanTree(getRootHash(), new org.ethereum.trie.TrieImpl.ScanAction() {
                    @Override
                    public void doOnNode(byte[] hash, Value node) {
                        cnt[0]++;
                    }
                });
            } catch (Exception e) {
                logger.error("Bad trie", e);
                return false;
            }
            logger.info("Done. Nodes: " + cnt[0]);
            return true;
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void flush() {
        // does nothing since Trie has no its own state
    }
}
