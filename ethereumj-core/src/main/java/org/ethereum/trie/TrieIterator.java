package org.ethereum.trie;

import java.util.List;

import org.ethereum.util.Value;
import static org.ethereum.util.CompactEncoder.unpackToNibbles;

/*
 * www.ethereumJ.com
 * @author: Nick Savers
 * Created on: 20/05/2014 10:44
 */
public class TrieIterator {
	
	private TrieImpl trie;
	private String key;
	private String value;

	private List<byte[]> shas;
	private List<String> values;

	public TrieIterator(TrieImpl t) {
		this.trie = t;
	}

	// Some time in the near future this will need refactoring :-)
	// XXX Note to self, IsSlice == inline node. Str == sha3 to node
	private void workNode(Value currentNode) {
		if (currentNode.length() == 2) {
			byte[] k = unpackToNibbles(currentNode.get(0).asBytes());

			if (currentNode.get(1).asString() == "") {
				this.workNode(currentNode.get(1));
			} else {
				if (k[k.length-1] == 16) {
					this.values.add(currentNode.get(1).asString());
				} else {
					this.shas.add(currentNode.get(1).asBytes());
					this.getNode(currentNode.get(1).asBytes());
				}
			}
		} else {
			for (int i = 0; i < currentNode.length(); i++) {
				if (i == 16 && currentNode.get(i).length() != 0) {
					this.values.add(currentNode.get(i).asString());
				} else {
					if (currentNode.get(i).asString() == "") {
						this.workNode(currentNode.get(i));
					} else {
						String val = currentNode.get(i).asString();
						if (val != "") {
							this.shas.add(currentNode.get(1).asBytes());
							this.getNode(val.getBytes());
						}
					}
				}
			}
		}
	}

	private void getNode(byte[] node) {
		Value currentNode = this.trie.getCache().get(node);
		this.workNode(currentNode);
	}

	private List<byte[]> collect() {
		if (this.trie.getRoot() == "") {
			return null;
		}
		this.getNode(new Value(this.trie.getRoot()).asBytes());
		return this.shas;
	}

	public int purge() {
		List<byte[]> shas = this.collect();
			
		for (byte[] sha : shas) {
			this.trie.getCache().delete(sha);	
		}
		return this.values.size();
	}

	private String getKey() {
		return "";
	}

	private String getValue() {
		return "";
	}
}
