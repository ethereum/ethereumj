package org.ethereum.publish.event;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListener;

/**
 * Emits when PendingTransaction arrives, executed or dropped and included to a block.
 *
 * @author Eugene Shevchenko
 */
public class PendingTransactionUpdatedEvent extends Event<PendingTransactionUpdatedEvent.Data> {

    /**
     * Event DTO
     */
    public static class Data {
        private final TransactionReceipt receipt;
        private final EthereumListener.PendingTransactionState state;
        private final Block block;

        /**
         * @param receipt Receipt of the tx execution on the current PendingState
         * @param state   Current state of pending tx
         * @param block   The block which the current pending state is based on (for PENDING tx state)
         *                or the block which tx was included to (for INCLUDED state)
         */
        public Data(Block block, TransactionReceipt receipt, EthereumListener.PendingTransactionState state) {
            this.receipt = receipt;
            this.state = state;
            this.block = block;
        }

        public TransactionReceipt getReceipt() {
            return receipt;
        }

        public EthereumListener.PendingTransactionState getState() {
            return state;
        }

        public Block getBlock() {
            return block;
        }
    }

    public PendingTransactionUpdatedEvent(Block block, TransactionReceipt receipt, EthereumListener.PendingTransactionState state) {
        super(new Data(block, receipt, state));
    }
}
