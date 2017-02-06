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
public class RepositoryHashedKeysTrie extends RepositoryRoot {

    @Autowired
    public RepositoryHashedKeysTrie(StateSource stateDS) {
        super(stateDS);
    }

    public RepositoryHashedKeysTrie(Source<byte[], byte[]> stateDS, byte[] root) {
        super(stateDS, root);
    }

    @Override
    protected TrieImpl createTrie(CachedSource.BytesKey<Value> trieCache, byte[] root) {
        return new TrieImpl(trieCache, root);
    }
}
