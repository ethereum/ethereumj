package org.ethereum.api;

import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListener;
import org.ethereum.api.type.*;

/**
 * Created by Anton Nashatyrev on 08.09.2016.
 */
public interface TransactionState {

    EthereumListener.PendingTransactionState getState();

    int getConfirmingBlockCount();

    BlockId getIncludingBlock();

    TransactionReceipt getTransactionReceipt();
}
