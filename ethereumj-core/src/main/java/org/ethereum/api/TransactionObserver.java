package org.ethereum.api;

import org.ethereum.util.Functional;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public interface TransactionObserver extends AbstractObserver<TransactionState> {

    TransactionState getCurrentState();

    TransactionState waitTillConfirmed(int confirmationBlocksCount);

    void notifyOnConfirmed(Functional.Consumer<TransactionState> listener, int confirmationBlocksCount);
}
