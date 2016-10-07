package org.ethereum.datasource.test;

import org.ethereum.util.Value;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class TrieImpl implements Trie<byte[]> {

    Source<byte[], Value> cache;
    byte[] root;

    public TrieImpl(Source<byte[], Value> trieCache, byte[] root) {
        this.cache = trieCache;
        this.root = root;
    }

    @Override
    public byte[] getRootHash() {
        return new byte[0];
    }

    @Override
    public void put(byte[] key, byte[] val) {

    }

    @Override
    public byte[] get(byte[] key) {
        return new byte[0];
    }

    @Override
    public void delete(byte[] key) {

    }
}
