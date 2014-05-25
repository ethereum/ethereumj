package org.ethereum.net.submit;

import org.ethereum.core.Transaction;
import org.ethereum.manager.MainData;
import org.ethereum.net.client.ClientPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 23/05/2014 18:33
 */

public class TransactionTask implements Callable {

    Logger logger = LoggerFactory.getLogger("TransactionTask");

    Transaction tx;


    public TransactionTask(Transaction tx) {
        this.tx = tx;
    }

    @Override
    public Object call() throws Exception {

        logger.info("call() tx: {}", tx.toString());

        ClientPeer peer = MainData.instance.getActivePeer();

        PendingTransaction pendingTransaction =  MainData.instance.addPendingTransaction(tx);
        peer.sendTransaction(tx);

        int i = 0;
        while(pendingTransaction.getApproved() < 1 ){

            ++i;
            sleep(10);
        }

        logger.info("return approved: {}", pendingTransaction.getApproved());

        return null;
    }
}
