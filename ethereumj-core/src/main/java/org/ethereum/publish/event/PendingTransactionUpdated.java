/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.publish.event;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

/**
 * Emits when PendingTransaction arrives, executed or dropped and included to a block.
 *
 * @author Eugene Shevchenko
 */
public class PendingTransactionUpdated extends Event<PendingTransactionUpdated.Data> {

    public enum State {
        /**
         * Transaction may be dropped due to:
         * - Invalid transaction (invalid nonce, low gas price, insufficient account funds,
         * invalid signature)
         * - Timeout (when pending transaction is not included to any block for
         * last [transaction.outdated.threshold] blocks
         * This is the final state
         */
        DROPPED,

        /**
         * The same as PENDING when transaction is just arrived
         * Next state can be either PENDING or INCLUDED
         */
        NEW_PENDING,

        /**
         * State when transaction is not included to any blocks (on the main chain), and
         * was executed on the last best block. The repository state is reflected in the PendingState
         * Next state can be either INCLUDED, DROPPED (due to timeout)
         * or again PENDING when a new block (without this transaction) arrives
         */
        PENDING,

        /**
         * State when the transaction is included to a block.
         * This could be the final state, however next state could also be
         * PENDING: when a fork became the main chain but doesn't include this tx
         * INCLUDED: when a fork became the main chain and tx is included into another
         * block from the new main chain
         * DROPPED: If switched to a new (long enough) main chain without this Tx
         */
        INCLUDED;

        public boolean isPending() {
            return this == NEW_PENDING || this == PENDING;
        }
    }

    /**
     * Event DTO
     */
    public static class Data {
        private final TransactionReceipt receipt;
        private final State state;
        private final Block block;

        /**
         * @param receipt Receipt of the tx execution on the current PendingState
         * @param state   Current state of pending tx
         * @param block   The block which the current pending state is based on (for PENDING tx state)
         *                or the block which tx was included to (for INCLUDED state)
         */
        public Data(Block block, TransactionReceipt receipt, State state) {
            this.receipt = receipt;
            this.state = state;
            this.block = block;
        }

        public TransactionReceipt getReceipt() {
            return receipt;
        }

        public State getState() {
            return state;
        }

        public Block getBlock() {
            return block;
        }
    }

    public PendingTransactionUpdated(Block block, TransactionReceipt receipt, State state) {
        super(new Data(block, receipt, state));
    }
}
