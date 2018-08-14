package org.ethereum.publish.event;

import org.ethereum.core.Transaction;

import java.util.List;

/**
 * @author Eugene Shevchenko
 * @deprecated use PendingTransactionUpdated filtering state NEW_PENDING
 * Will be removed in the next release
 */
public class PendingTransactionsReceived extends Event<List<Transaction>> {

    public PendingTransactionsReceived(List<Transaction> transactions) {
        super(transactions);
    }
}
