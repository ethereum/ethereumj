package org.ethereum.trie;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 29.08.2014
 */
public class CollectFullSetOfNodes implements TrieImpl.ScanAction {
    Set<ByteArrayWrapper> nodes = new HashSet<>();

    @Override
    public void doOnNode(byte[] hash, TrieImpl.Node node) {
        nodes.add(new ByteArrayWrapper(hash));
    }

    @Override
    public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {}

    public Set<ByteArrayWrapper> getCollectedHashes() {
        return nodes;
    }
}
