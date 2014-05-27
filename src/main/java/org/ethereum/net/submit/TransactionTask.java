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
    boolean obsolete = false;


    public TransactionTask(Transaction tx) {
        this.tx = tx;
    }

    @Override
    public Object call() throws Exception {

        try {
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
        } catch (Throwable th) {
            logger.info("exception caugh: {}", th.getCause());
            MainData.instance.removePendingTransaction(tx);
        }

        return null;
    }

}
