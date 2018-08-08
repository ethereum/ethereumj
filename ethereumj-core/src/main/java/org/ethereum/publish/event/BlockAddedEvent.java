package org.ethereum.publish.event;

import org.ethereum.core.BlockSummary;

public class BlockAddedEvent extends Event<BlockSummary> {

    public BlockAddedEvent(BlockSummary data) {
        super(data);
    }
}
