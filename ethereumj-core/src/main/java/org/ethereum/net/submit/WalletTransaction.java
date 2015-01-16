package org.ethereum.net.submit;

import org.ethereum.core.Transaction;

/**
 * @author Roman Mandeleil
 * @since 23.05.2014
 */
public class WalletTransaction {

    private final Transaction tx;
    private int approved = 0; // each time the tx got from the wire this value increased

    public WalletTransaction(Transaction tx) {
        this.tx = tx;
    }

    public void incApproved() {
        approved++;
    }

    public int getApproved() {
        return approved;
    }
}
