package org.ethereum.core;

import java.math.BigInteger;

/**
 * Decorates {@link Transaction} class with additional attributes
 * related to Pending Transaction logic
 *
 * @author Mikhail Kalinin
 * @since 11.08.2015
 */
public class PendingTransaction {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingTransaction)) return false;

        PendingTransaction that = (PendingTransaction) o;

        return transaction.equals(that.transaction);
    }

    @Override
    public int hashCode() {
        return transaction.hashCode();
    }
}
