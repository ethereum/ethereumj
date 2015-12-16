package org.ethereum.mine;

import org.ethereum.core.Block;

/**
 * Created by Anton Nashatyrev on 10.12.2015.
 */
public interface MinerListener {
    void miningStarted();
    void miningStopped();
    void blockMiningStarted(Block block);
    void blockMined(Block block);
    void blockMiningCanceled(Block block);
}
