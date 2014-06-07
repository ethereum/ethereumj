package org.ethereum.net.submit;

import org.ethereum.core.Transaction;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 23/05/2014 18:41
 */

public class WalletTransaction {

    private Transaction tx;
    int approved = 0; // each time the tx got from the wire this value increased

    public WalletTransaction(Transaction tx) {
        this.tx = tx;
    }

	public void incApproved() {
		++this.approved;
	}

    public int getApproved() {
        return approved;
    }
}

