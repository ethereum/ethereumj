package org.ethereum.api;

import org.ethereum.core.TransactionReceipt;

/**
 * Created by Anton Nashatyrev on 09.09.2016.
 */
public class InvalidTransactionException extends RuntimeException {
    private static final long serialVersionUID = -8749898542151630738L;

    private TransactionReceipt receipt;

    public InvalidTransactionException(TransactionReceipt receipt) {
        super("Invalid transaction (" + receipt.getError() + "): " + receipt.getTransaction());
        this.receipt = receipt;
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }
}
