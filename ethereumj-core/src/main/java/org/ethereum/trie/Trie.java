package org.ethereum.trie;

import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;
import static org.ethereum.util.CompactEncoder.*;

import java.util.Arrays;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.Value;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * The modified Merkle Patricia tree (trie) provides a persistent data structure 
 * to map between arbitrary-length binary data (byte arrays). It is defined in terms of 
 * a mutable data structure to map between 256-bit binary fragments and arbitrary-length 
 * binary data, typically implemented as a database. The core of the trie, and its sole 
 * requirement in terms of the protocol specification is to provide a single value that 
 * identifies a given set of key-value pairs, which may either a 32 byte sequence or 
 * the empty byte sequence. It is left as an implementation consideration to store and 
 * maintain the structure of the trie in a manner the allows effective and efficient 
 * realisation of the protocol.
 *
 * The trie implements a caching mechanism and will use cached values if they are present. 
 * If a node is not present in the cache it will try to fetch it from the database and 
 * store the cached value. 
 *
 * Please note that the data isn't persisted unless `sync` is explicitly called.
 *
 * www.ethereumJ.com
 * @author: Nick Savers
 * Created on: 20/05/2014 10:44
 */
public class Trie implements TrieFacade {

    private Logger logger = LoggerFactory.getLogger("trie");

    private static byte PAIR_SIZE = 2;
    private static byte LIST_SIZE = 17;

    private Object prevRoot;
    private Object root;
    private Cache cache;

    public Trie(DB db) {
        this(db, "");
    }

    public Trie(DB db, Object root) {
        this.cache = new Cache(db);
        this.root = root;
        this.prevRoot = root;
    }

    public TrieIterator getIterator() {
        return new TrieIterator(this);
    }

    public Cache getCache() {
        return this.cache;
    }

    public Object getPrevRoot() {
        return prevRoot;
    }

    public Object getRoot() {
        return root;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**************************************
     * Public (query) interface functions *
     **************************************/

    /**
     * Insert key/value pair into trie
     *
     * @param key
     * @param value
     */
    public void update(String key, String value) {
        this.update(key.getBytes(), value.getBytes());
    }

    /**
     * Insert key/value pair into trie
     *
     * @param key
     * @param value
     */
    public void update(byte[] key, byte[] value) {
        if (key == null)
            throw new NullPointerException("Key should not be blank");
        byte[] k = binToNibbles(key);
        this.root = this.insertOrDelete(this.root, k, value);
        if(logger.isDebugEnabled()) {
            logger.debug("Added key {} and value {}", Hex.toHexString(key), Hex.toHexString(value));
            logger.debug("New root-hash: {}", Hex.toHexString(this.getRootHash()));
        }
    }

    /**
     * Retrieve a value from a node
     *
     * @param key
     * @return value
     */
    public byte[] get(String key) {
        return this.get(key.getBytes());
    }

    /**
     * Retrieve a value from a node
     *
     * @param key
     * @return value
     */
    public byte[] get(byte[] key) {
        if(logger.isDebugEnabled()) {
            logger.debug("Retrieving key {}", Hex.toHexString(key));
        }
        byte[] k = binToNibbles(key);
        Value c = new Value( this.get(this.root, k) );
        return c.asBytes();
    }

    /**
     * Delete a key/value pair from the trie
     *
     * @param key
     */
    public void delete(byte[] key) {
        delete(new String(key));
        if(logger.isDebugEnabled()) {
            logger.debug("Deleted value for key {}", Hex.toHexString(key));
            logger.debug("New root-hash: {}", Hex.toHexString(this.getRootHash()));
        }
    }

    /**
     * Delete a key/value pair from the trie
     *
     * @param key
     */
    public void delete(String key) {
        this.update(key.getBytes(), "".getBytes());
    }

    /****************************************
     * 			Private functions			*
     ****************************************/

    private Object get(Object node, byte[] key) {

        // Return the node if key is empty (= found)
        if (key.length == 0 || isEmptyNode(node)) {
            return node;
        }

        Value currentNode = this.getNode(node);

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

    private Object insertOrDelete(Object node, byte[] key, byte[] value) {
        if (value.length != 0) {
            return this.insert(node, key, value);
        } else {
            return this.delete(node, key);
        }
    }

    /**
     * Update or add the item inside a node
     * return the updated node with rlp encoded
     */
    private Object insert(Object node, byte[] key, Object value) {

        if (key.length == 0) {
            return value;
        }

        if (isEmptyNode(node)) {
            Object[] newNode = new Object[] { packNibbles(key), value };
            return this.put(newNode);
        }

        Value currentNode = this.getNode(node);

        // Check for "special" 2 slice type node
        if (currentNode.length() == PAIR_SIZE) {
            // Decode the key
            byte[] k = unpackToNibbles(currentNode.get(0).asBytes());
            Object v = currentNode.get(1).asObj();

            // Matching key pair (ie. there's already an object with this key)
            if (Arrays.equals(k, key)) {
                Object[] newNode = new Object[] {packNibbles(key), value};
                return this.put(newNode);
            }

            Object newHash;
            int matchingLength = matchingNibbleLength(key, k);
            if (matchingLength == k.length) {
                // Insert the hash, creating a new node
                byte[] remainingKeypart = copyOfRange(key, matchingLength, key.length);
                newHash = this.insert(v, remainingKeypart, value);
            } else { // Expand the 2 length slice to a 17 length slice
                // Create two nodes to put into the new 17 length node
                Object oldNode = this.insert("", copyOfRange(k, matchingLength+1, k.length), v);
                Object newNode = this.insert("", copyOfRange(key, matchingLength+1, key.length), value);
                // Create an expanded slice
                Object[] scaledSlice = emptyStringSlice(17);
                // Set the copied and new node
                scaledSlice[k[matchingLength]] = oldNode;
                scaledSlice[key[matchingLength]] = newNode;
                newHash = this.put(scaledSlice);
            }

            if (matchingLength == 0) {
                // End of the chain, return
                return newHash;
            } else {
                Object[] newNode = new Object[] { packNibbles(copyOfRange(key, 0, matchingLength)), newHash};
                return this.put(newNode);
            }
        } else {
            // Copy the current node over to the new node
            Object[] newNode = copyNode(currentNode);
            // Replace the first nibble in the key
            newNode[key[0]] = this.insert(currentNode.get(key[0]).asObj(), copyOfRange(key, 1, key.length), value);

            return this.put(newNode);
        }
    }

    private Object delete(Object node, byte[] key) {

        if (key.length == 0 || isEmptyNode(node)) {
            return "";
        }

        // New node
        Value currentNode = this.getNode(node);
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
                    newNode = new Object[] {packNibbles(newKey), child.get(1).asObj()};
                } else {
                    newNode = new Object[] {currentNode.get(0).asString(), hash};
                }
                return this.put(newNode);
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
                newNode = new Object[] { packNibbles(new byte[] {16} ), itemList[amount]};
            } else if (amount >= 0) {
                Value child = this.getNode(itemList[amount]);
                if (child.length() == PAIR_SIZE) {
                    key = concatenate(new byte[]{amount}, unpackToNibbles(child.get(0).asBytes()));
                    newNode = new Object[] {packNibbles(key), child.get(1).asObj()};
                } else if (child.length() == LIST_SIZE) {
                    newNode = new Object[] { packNibbles(new byte[]{amount}), itemList[amount]};
                }
            } else {
                newNode = itemList;
            }
            return this.put(newNode);
        }
    }

    /**
     * Helper method to retrieve the actual node
     * If the node is not a list and length is > 32 bytes get the actual node from the db
     *
     * @param node
     * @return
     */
    private Value getNode(Object node) {
        Value n = new Value(node);

        if (!n.get(0).isNull()) {
            return n;
        }

        byte[] str = n.asBytes();
        if (str.length == 0) {
            return n;
        } else if (str.length < 32) {
            return new Value(str);
        }
        return this.cache.get(str);
    }

    private Object put(Object node) {
        return this.cache.put(node);
    }

    private boolean isEmptyNode(Object node) {
        Value n = new Value(node);
        return (node == null || (n.isString() && (n.asString() == "" || n.get(0).isNull())) || n.length() == 0);
    }

    private Object[] copyNode(Value currentNode) {
        Object[] itemList = emptyStringSlice(LIST_SIZE);
        for (int i = 0; i < LIST_SIZE; i++) {
            Object cpy = currentNode.get(i).asObj();
            if (cpy != null) {
                itemList[i] = cpy;
            }
        }
        return itemList;
    }

    // Simple compare function which compared the tries based on their stateRoot
    public boolean cmp(Trie trie) {
        return Arrays.equals(this.getRootHash(), trie.getRootHash());
    }

    // Save the cached value to the database.
    public void sync() {
        this.cache.commit();
        this.prevRoot = this.root;
    }

    public void undo() {
        this.cache.undo();
        this.root = this.prevRoot;
    }

    // Returns a copy of this trie
    public Trie copy() {
        Trie trie = new Trie(this.cache.getDb(), this.root);
        for (ByteArrayWrapper key : this.cache.getNodes().keySet()) {
            Node node = this.cache.getNodes().get(key);
            trie.cache.getNodes().put(key, node.copy());
        }
        return trie;
    }

    /********************************
     *  	Utility functions		*
     *******************************/

    // Returns the amount of nibbles that match each other from 0 ...
    private int matchingNibbleLength(byte[] a, byte[] b) {
        int i = 0;
        while (Arrays.equals(copyOfRange(a, 0, i+1), copyOfRange(b, 0, i+1)) && i < b.length) {
            i++;
        }
        return i;
    }

    // Created an array of empty elements of requred length
    private Object[] emptyStringSlice(int l) {
        Object[] slice = new Object[l];
        for (int i = 0; i < l; i++) {
            slice[i] = "";
        }
        return slice;
    }

    public byte[] getRootHash() {
        Object root = this.getRoot();
        if (root == null
                || (root instanceof byte[] && ((byte[]) root).length == 0)
                || (root instanceof String && "".equals((String) root))) {
            return new byte[0];
        } else if (root instanceof byte[]) {
            return (byte[]) this.getRoot();
        } else {
            Value rootValue = new Value(this.getRoot());
            byte[] val = rootValue.encode();
            return HashUtil.sha3(val);
        }
    }
}
