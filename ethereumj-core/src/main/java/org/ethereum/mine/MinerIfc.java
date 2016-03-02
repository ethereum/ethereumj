package org.ethereum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;

/**
 * Mine algorithm interface
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public interface MinerIfc {

    /**
     * Starts mining the block. On successful mining the Block is update with necessary nonce and hash.
     * @return Nonce Future object. The mining can be canceled via this Future. The Future is complete
     * when the block successfully mined.
     */
    ListenableFuture<Long> mine(Block block);

    /**
     * Validates the Proof of Work for the block
     */
    boolean validate(BlockHeader blockHeader);
}
