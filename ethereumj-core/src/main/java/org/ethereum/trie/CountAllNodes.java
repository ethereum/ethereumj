package org.ethereum.trie;

import org.ethereum.util.Value;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 29/08/2014 10:46
 */

public class CountAllNodes implements Trie.ScanAction {

    int counted = 0;

    @Override
    public void doOnNode(byte[] hash, Value node) {
        ++counted;
    }

    public int getCounted(){return counted;}
}
