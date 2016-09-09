package org.ethereum.api;

import org.ethereum.util.Functional;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public interface TransactionObserver extends AbstractObserver<TransactionState> {

    TransactionState getCurrentState();

    TransactionState waitConfirmed(int confirmationBlocksCount) throws InvalidTransactionException;

    TransactionState waitPending() throws InvalidTransactionException;

    void notifyOnConfirmed(Functional.Consumer<TransactionState> listener, int confirmationBlocksCount);
}
