package org.ethereum.publish.event;

import org.ethereum.core.BlockSummary;

public class BestBlockAdded extends Event<BestBlockAdded.Data> {

    public static class Data {
        private final BlockSummary blockSummary;
        private final boolean best;

        public Data(BlockSummary blockSummary, boolean best) {
            this.blockSummary = blockSummary;
            this.best = best;
        }

        public BlockSummary getBlockSummary() {
            return blockSummary;
        }

        public boolean isBest() {
            return best;
        }
    }

    public BestBlockAdded(BlockSummary blockSummary, boolean best) {
        super(new Data(blockSummary, best));
    }
}