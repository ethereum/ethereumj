package org.ethereum.net.submit;

import org.ethereum.core.Transaction;
import org.ethereum.manager.MainData;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.ClientPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 23/05/2014 18:33
 */
public class TransactionTask implements Callable<Transaction> {

	private Logger logger = LoggerFactory.getLogger("TransactionTask");

	private Transaction tx;
    
    public TransactionTask(Transaction tx) {
        this.tx = tx;
    }

    @Override
    public Transaction call() throws Exception {

        try {
            logger.info("call() tx: {}", tx.toString());

            ClientPeer peer = MainData.instance.getActivePeer();

			WalletTransaction walletTransaction = WorldManager.getInstance()
					.addWalletTransaction(tx);
            peer.sendTransaction(tx);

            while(walletTransaction.getApproved() < 1 ) {
                sleep(10);
            }
            logger.info("return approved: {}", walletTransaction.getApproved());
        } catch (Throwable th) {
            logger.info("exception caugh: {}", th);
            WorldManager.getInstance().removeWalletTransaction(tx);
        }
        return null;
    }
}
