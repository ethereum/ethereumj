package org.ethereum.datasource.test;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public interface Trie<V> extends Source<byte[], V> {

    byte[] getRootHash();

    /**
     * Recursively delete all nodes from root
     */
    void clear();
}
