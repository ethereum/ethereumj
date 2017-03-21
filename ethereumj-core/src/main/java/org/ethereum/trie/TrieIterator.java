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
package org.ethereum.trie;

import org.ethereum.util.Value;

import java.util.List;

import static org.ethereum.util.CompactEncoder.unpackToNibbles;

/*
 * @author Nick Savers
 * @since 20.05.2014
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
    // XXX Note to self, IsSlice == inline node. Str == keccak to node
    private void workNode(Value currentNode) {
        if (currentNode.length() == 2) {
            byte[] k = unpackToNibbles(currentNode.get(0).asBytes());

            if (currentNode.get(1).asString().isEmpty()) {
                this.workNode(currentNode.get(1));
            } else {
                if (k[k.length - 1] == 16) {
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
                    if (currentNode.get(i).asString().isEmpty()) {
                        this.workNode(currentNode.get(i));
                    } else {
                        String val = currentNode.get(i).asString();
                        if (!val.isEmpty()) {
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
