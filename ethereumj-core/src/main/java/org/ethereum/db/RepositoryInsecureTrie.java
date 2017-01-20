package org.ethereum.db;

import org.ethereum.datasource.CachedSource;
import org.ethereum.datasource.Source;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initialized repository with default stateSource
 * Similar to initialized {@link RepositoryRoot} but
 * this one expects that keys are already hashed with sha3()
 */
@Component
public class RepositoryInsecureTrie extends RepositoryRoot {

    @Autowired
    public RepositoryInsecureTrie(StateSource stateDS) {
        super(stateDS);
    }

    public RepositoryInsecureTrie(Source<byte[], byte[]> stateDS, byte[] root) {
        super(stateDS, root);
    }

    @Override
    protected TrieImpl createTrie(CachedSource.BytesKey<Value> trieCache, byte[] root) {
        return new TrieImpl(trieCache, root);
    }
}
