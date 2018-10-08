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
import org.ethereum.core.PendingTransaction;
import org.ethereum.core.TransactionReceipt;

/**
 * Emits when PendingTransaction arrives, executed or dropped and included to a block.
 *
 * @author Eugene Shevchenko
 */
public class PendingTransactionUpdated extends Event<PendingTransactionUpdated.Data> {

    /**
     * Event DTO
     */
    public static class Data {
        private final TransactionReceipt receipt;
        private final PendingTransaction.State state;
        private final Block block;

        /**
         * @param receipt Receipt of the tx execution on the current PendingState
         * @param state   Current state of pending tx
         * @param block   The block which the current pending state is based on (for PENDING tx state)
         *                or the block which tx was included to (for INCLUDED state)
         */
        public Data(Block block, TransactionReceipt receipt, PendingTransaction.State state) {
            this.receipt = receipt;
            this.state = state;
            this.block = block;
        }

        public TransactionReceipt getReceipt() {
            return receipt;
        }

        public PendingTransaction.State getState() {
            return state;
        }

        public Block getBlock() {
            return block;
        }
    }

    public PendingTransactionUpdated(Block block, TransactionReceipt receipt, PendingTransaction.State state) {
        super(new Data(block, receipt, state));
    }
}
