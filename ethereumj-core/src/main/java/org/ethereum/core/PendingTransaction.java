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
package org.ethereum.core;

import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Decorates {@link Transaction} class with additional attributes
 * related to Pending Transaction logic
 *
 * @author Mikhail Kalinin
 * @since 11.08.2015
 */
public class PendingTransaction {

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
     * transaction
     */
    private Transaction transaction;

    /**
     * number of block that was best at the moment when transaction's been added
     */
    private long blockNumber;

    public PendingTransaction(byte[] bytes) {
        parse(bytes);
    }

    public PendingTransaction(Transaction transaction) {
        this(transaction, 0);
    }

    public PendingTransaction(Transaction transaction, long blockNumber) {
        this.transaction = transaction;
        this.blockNumber = blockNumber;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public byte[] getSender() {
        return transaction.getSender();
    }

    public byte[] getHash() {
        return transaction.getHash();
    }

    public byte[] getBytes() {
        byte[] numberBytes = BigInteger.valueOf(blockNumber).toByteArray();
        byte[] txBytes = transaction.getEncoded();
        byte[] bytes = new byte[1 + numberBytes.length + txBytes.length];

        bytes[0] = (byte) numberBytes.length;
        System.arraycopy(numberBytes, 0, bytes, 1, numberBytes.length);

        System.arraycopy(txBytes, 0, bytes, 1 + numberBytes.length, txBytes.length);

        return bytes;
    }

    private void parse(byte[] bytes) {
        byte[] numberBytes = new byte[bytes[0]];
        byte[] txBytes = new byte[bytes.length - 1 - numberBytes.length];

        System.arraycopy(bytes, 1, numberBytes, 0, numberBytes.length);

        System.arraycopy(bytes, 1 + numberBytes.length, txBytes, 0, txBytes.length);

        this.blockNumber = new BigInteger(numberBytes).longValue();
        this.transaction = new Transaction(txBytes);
    }

    @Override
    public String toString() {
        return "PendingTransaction [" +
                "  transaction=" + transaction +
                ", blockNumber=" + blockNumber +
                ']';
    }

    /**
     *  Two pending transaction are equal if equal their sender + nonce
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingTransaction)) return false;

        PendingTransaction that = (PendingTransaction) o;

        return Arrays.equals(getSender(), that.getSender()) &&
                Arrays.equals(transaction.getNonce(), that.getTransaction().getNonce());
    }

    @Override
    public int hashCode() {
        return ByteUtil.byteArrayToInt(getSender()) + ByteUtil.byteArrayToInt(transaction.getNonce());
    }
}
