package org.ethereum.trie;

import java.util.HashSet;
import java.util.Set;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 29/08/2014 10:46
 */

public class CountAllNodes implements Trie.ScanAction {

    int counted = 0;

    @Override
    public void doOnNode(byte[] hash) {
        ++counted;
    }

    public int getCounted(){return counted;}
}
