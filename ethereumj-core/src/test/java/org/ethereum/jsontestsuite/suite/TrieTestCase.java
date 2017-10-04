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
package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.ethereum.datasource.NoDeleteSource;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.Map;

import static org.ethereum.jsontestsuite.suite.Utils.parseData;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrieTestCase {

    @JsonIgnore
    String name;

    Object in;
    String root;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getIn() {
        return in;
    }

    public void setIn(Object in) {
        this.in = in;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @SuppressWarnings("unchecked")
    public String calculateRoot(boolean secure) {

        Trie<byte[]> trie;

        if (secure) {
            trie = new SecureTrie(new NoDeleteSource<>(new HashMapDB<byte[]>()));
        } else {
            trie = new TrieImpl(new NoDeleteSource<>(new HashMapDB<byte[]>()));
        }

        if (in instanceof Map) {

            for (Map.Entry<String, String> e : ((Map<String, String>)in).entrySet()) {

                byte[] key = e.getKey().startsWith("0x") ? parseData(e.getKey()) : e.getKey().getBytes();
                byte[] value = null;
                if (e.getValue() != null) {
                    value = e.getValue().startsWith("0x") ? parseData(e.getValue()) : e.getValue().getBytes();
                }

                trie.put(key, value);
            }

        } else if (in instanceof List) {

            for (List<String> pair : ((List<List<String>>)in)) {

                byte[] key = pair.get(0).startsWith("0x") ? parseData(pair.get(0)) : pair.get(0).getBytes();
                byte[] value = null;
                if (pair.get(1) != null) {
                    value = pair.get(1).startsWith("0x") ? parseData(pair.get(1)) : pair.get(1).getBytes();
                }

                trie.put(key, value);
            }

        } else {
            throw new IllegalArgumentException("Not supported format of Trie testcase");
        }

        return "0x" + Hex.toHexString(trie.getRootHash());
    }

    @Override
    public String toString() {
        return "TrieTestCase{" +
                "name='" + name + '\'' +
                ", in=" + in +
                ", root='" + root + '\'' +
                '}';
    }
}
