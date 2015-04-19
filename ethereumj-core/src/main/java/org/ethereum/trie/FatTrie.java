package org.ethereum.trie;

import org.ethereum.crypto.SHA3Helper;
import org.ethereum.datasource.KeyValueDataSource;

import static org.ethereum.crypto.SHA3Helper.sha3;

public class FatTrie implements Trie{

    TrieImpl origTrie;
    SecureTrie secureTrie;

    public FatTrie() {
        origTrie = new TrieImpl(null);
        secureTrie = new SecureTrie(null);
    }

    public FatTrie(KeyValueDataSource origTrieDS, KeyValueDataSource secureTrieDS) {
        origTrie = new TrieImpl(origTrieDS);
        secureTrie = new SecureTrie(secureTrieDS);
    }


    public TrieImpl getOrigTrie() {
        return origTrie;
    }

    public SecureTrie getSecureTrie() {
        return secureTrie;
    }

    @Override
    public byte[] get(byte[] key) {
        return secureTrie.get(key);
    }

    @Override
    public void update(byte[] key, byte[] value) {
        origTrie.update(key, value);
        secureTrie.update(key, value);
    }

    @Override
    public void delete(byte[] key) {
        origTrie.delete(key);
        secureTrie.delete(key);
    }

    @Override
    public byte[] getRootHash() {
        return secureTrie.getRootHash();
    }

    @Override
    public void setRoot(byte[] root) {

        secureTrie.setRoot(root);
//        throw new UnsupportedOperationException("Fat trie doesn't support root rollbacks");
    }

    @Override
    public void sync() {
        origTrie.sync();
        secureTrie.sync();
    }

    @Override
    public void undo() {
        origTrie.undo();
        secureTrie.undo();
    }

    @Override
    public String getTrieDump() {
        return secureTrie.getTrieDump();
    }

    @Override
    public boolean validate() {
        return secureTrie.validate();
    }
}
