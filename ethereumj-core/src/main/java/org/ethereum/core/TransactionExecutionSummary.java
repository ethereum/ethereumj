package org.ethereum.core;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.InternalTransaction;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static java.util.Collections.*;
import static org.ethereum.util.BIUtil.toBI;

public class TransactionExecutionSummary {

    private byte[] transactionHash;
    private BigInteger value = BigInteger.ZERO;
    private BigInteger gasPrice = BigInteger.ZERO;
    private BigInteger gasLimit = BigInteger.ZERO;
    private BigInteger gasUsed = BigInteger.ZERO;
    private BigInteger gasLeftover = BigInteger.ZERO;

    private List<DataWord> deletedAccounts = emptyList();
    private List<InternalTransaction> internalTransactions = emptyList();
    private Map<DataWord, DataWord> storageDiff = emptyMap();

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public BigInteger getFee() {
        return gasLimit.subtract(gasLeftover).multiply(gasPrice);
    }

    public BigInteger getRefund() {
        return gasLeftover.multiply(gasPrice);
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public BigInteger getValue() {
        return value;
    }

    public List<DataWord> getDeletedAccounts() {
        return deletedAccounts;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public Map<DataWord, DataWord> getStorageDiff() {
        return storageDiff;
    }

    public BigInteger getGasLeftover() {
        return gasLeftover;
    }

    public static Builder builderFor(Transaction transaction) {
        return new Builder(transaction);
    }

    public static class Builder {

        private final TransactionExecutionSummary summary;

        Builder(Transaction transaction) {
            Assert.notNull(transaction, "Cannot build TransactionExecutionSummary for null transaction.");

            summary = new TransactionExecutionSummary();
            summary.transactionHash = transaction.getHash();
            summary.gasLimit = toBI(transaction.getGasLimit());
            summary.gasPrice = toBI(transaction.getGasPrice());
            summary.value = toBI(transaction.getValue());
        }


        public Builder gasUsed(BigInteger gasUsed) {
            summary.gasUsed = gasUsed;
            return this;
        }

        public Builder gasLeftover(BigInteger gasLeftover) {
            summary.gasLeftover = gasLeftover;
            return this;
        }

        public Builder internalTransactions(List<InternalTransaction> internalTransactions) {
            summary.internalTransactions = unmodifiableList(internalTransactions);
            return this;
        }

        public Builder deletedAccounts(List<DataWord> deletedAccounts) {
            summary.deletedAccounts = unmodifiableList(deletedAccounts);
            return this;
        }

        public Builder storageDiff(Map<DataWord, DataWord> storageDiff) {
            summary.storageDiff = unmodifiableMap(storageDiff);
            return this;
        }

        public TransactionExecutionSummary build() {
            return summary;
        }
    }
}
