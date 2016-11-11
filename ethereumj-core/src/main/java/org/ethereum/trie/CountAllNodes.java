package org.ethereum.trie;

import org.ethereum.util.Value;

/**
 * @author Roman Mandeleil
 * @since 29.08.2014
 */
public class CountAllNodes implements TrieImpl.ScanAction {

    int counted = 0;

    @Override
    public void doOnNode(byte[] hash, Value node) {
        ++counted;
    }

    @Override
    public void doOnValue(byte[] nodeHash, Value node, byte[] key, byte[] value) {}

    public int getCounted() {
        return counted;
    }
}
