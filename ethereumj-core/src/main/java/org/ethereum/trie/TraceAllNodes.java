package org.ethereum.trie;

import org.ethereum.util.Value;

import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 29.08.2014
 */
public class TraceAllNodes implements TrieImpl.ScanAction {

    StringBuilder output = new StringBuilder();

    @Override
    public void doOnNode(byte[] hash, TrieImpl.Node node) {

        output.append(Hex.toHexString(hash)).append(" ==> ").append(node.toString()).append("\n");
    }

    @Override
    public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {}

    public String getOutput() {
        return output.toString();
    }
}
