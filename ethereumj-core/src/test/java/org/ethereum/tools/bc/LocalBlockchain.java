package org.ethereum.tools.bc;

import org.ethereum.core.Block;

/**
 * Created by Anton Nashatyrev on 24.03.2016.
 */
public interface LocalBlockchain extends EasyBlockchain {

    Block createBlock();

    Block createForkBlock(Block parent);
}
