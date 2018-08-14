package org.ethereum.publish.event;

import org.ethereum.core.BlockSummary;

public class BlockAdded extends Event<BlockSummary> {

    public BlockAdded(BlockSummary data) {
        super(data);
    }
}
