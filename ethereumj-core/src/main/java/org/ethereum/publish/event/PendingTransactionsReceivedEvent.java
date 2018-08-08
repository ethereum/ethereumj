package org.ethereum.publish.event;

import org.ethereum.core.Transaction;

import java.util.List;

/**
 * @author Eugene Shevchenko
 * @deprecated use PendingTransactionUpdatedEvent filtering state NEW_PENDING
 * Will be removed in the next release
 */
public class PendingTransactionsReceivedEvent extends Event<List<Transaction>> {

    public PendingTransactionsReceivedEvent(List<Transaction> transactions) {
        super(transactions);
    }
}
