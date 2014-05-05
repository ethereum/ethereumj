package org.ethereum.trie;

import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;

import java.util.Arrays;

import org.ethereum.util.CompactEncoder;
import org.ethereum.util.Value;
import org.iq80.leveldb.DB;

import com.cedarsoftware.util.DeepEquals;

public class Trie {

	private static byte PAIR_SIZE = 2;
	private static byte LIST_SIZE = 17;
	
	// A (modified) Radix Trie implementation. The Trie implements
	// a caching mechanism and will used cached values if they are
	// present. If a node is not present in the cache it will try to
	// fetch it from the database and store the cached value.
	// Please note that the data isn't persisted unless `Sync` is
	// explicitly called.
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

	public void setRoot(Node root) {
		this.root = root;
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
		if (key == null)
			throw new NullPointerException("Key should not be blank");
		byte[] k = CompactEncoder.hexDecode(key.getBytes());
		this.root = this.insertOrDelete(this.root, k, value.getBytes());
	}

	/**
	 * Retrieve a value from a node
	 * 
	 * @param key
	 * @return value
	 */
	public String get(String key) {
		byte[] k = CompactEncoder.hexDecode(key.getBytes());
		Value c = new Value( this.get(this.root, k) );
		return c.asString();
	}

	/**
	 * Delete a key/value pair from the trie
	 * 
	 * @param key
	 */
	public void delete(String key) {
		this.update(key, "");
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
		int length = currentNode.length();

		if (length == PAIR_SIZE) {
			// Decode the key
			byte[] k = CompactEncoder.decode(currentNode.get(0).asBytes());
			Object v = currentNode.get(1).asObj();

			if (key.length >= k.length && Arrays.equals(k, copyOfRange(key, 0, k.length))) {
				return this.get(v, copyOfRange(key, k.length, key.length));
			} else {
				return "";
			}
		} else if (length == LIST_SIZE) {
			return this.get(currentNode.get(key[0]).asObj(), copyOfRange(key, 1, key.length));
		}

		// It shouldn't come this far
		throw new RuntimeException("Unexpected Node length: " + length);
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
    		Object[] newNode = new Object[] { CompactEncoder.encode(key), value };
    		return this.put(newNode);
    	}
    	
    	Value currentNode = this.getNode(node);

    	// Check for "special" 2 slice type node
    	if (currentNode.length() == PAIR_SIZE) {
    		// Decode the key

    		byte[] k = CompactEncoder.decode(currentNode.get(0).asBytes());
    		Object v = currentNode.get(1).asObj();

    		// Matching key pair (ie. there's already an object with this key)
    		if (Arrays.equals(k, key)) {
    			Object[] newNode = new Object[] {CompactEncoder.encode(key), value};
    			return this.put(newNode);
    		}

    		Object newHash;
    		int matchingLength = matchingNibbleLength(key, k);
    		if (matchingLength == k.length) {
    			// Insert the hash, creating a new node
    			newHash = this.insert(v, copyOfRange(key, matchingLength, key.length), value);
    		} else {
    			// Expand the 2 length slice to a 17 length slice
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
    			Object[] newNode = new Object[] {CompactEncoder.encode(copyOfRange(key, 0, matchingLength)), newHash};
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
			byte[] k = CompactEncoder.decode(currentNode.get(0).asBytes());
			Object v = currentNode.get(1).asObj();

			// Matching key pair (ie. there's already an object with this key)
			if (Arrays.equals(k, key)) {
				return "";
			} else if (Arrays.equals(copyOfRange(key, 0, k.length), k)) {
				Object hash = this.delete(v, copyOfRange(key, k.length, key.length));
				Value child = this.getNode(hash);

				Object newNode;
				if (child.length() == PAIR_SIZE) {
					byte[] newKey = concatenate(k, CompactEncoder.decode(child.get(0).asBytes()));
					newNode = new Object[] {CompactEncoder.encode(newKey), child.get(1).asObj()};
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
				newNode = new Object[] { CompactEncoder.encode(new byte[] {16} ), itemList[amount]};
			} else if (amount >= 0) {
				Value child = this.getNode(itemList[amount]);
				if (child.length() == PAIR_SIZE) {
					key = concatenate(new byte[]{amount}, CompactEncoder.decode(child.get(0).asBytes()));
					newNode = new Object[] {CompactEncoder.encode(key), child.get(1).asObj()};
				} else if (child.length() == LIST_SIZE) {
					newNode = new Object[] { CompactEncoder.encode(new byte[]{amount}), itemList[amount]};
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

		String str = n.asString();
		if (str.length() == 0) {
			return n;
		} else if (str.length() < 32) {
			return new Value(str.getBytes());
		}
		return this.cache.get(n.asBytes());
	}
	
	private Object put(Object node) {
		/* TODO?
			c := Conv(t.Root)
			fmt.Println(c.Type(), c.Length())
			if c.Type() == reflect.String && c.AsString() == "" {
				return enc
			}
		*/
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

	// Simple compare function which creates a rlp value out of the evaluated objects
	public boolean cmp(Trie trie) {
		return DeepEquals.deepEquals(this.root, trie.getRoot());
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
		for (byte[] key : this.cache.getNodes().keySet()) {
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
	
	private Object[] emptyStringSlice(int l) {
		Object[] slice = new Object[l];
		for (int i = 0; i < l; i++) {
			slice[i] = "";
		}
		return slice;
	}
}
