package org.ethereum.net.submit;

import org.ethereum.core.Transaction;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.PeerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

/**
 * @author Roman Mandeleil
 * Created on: 23/05/2014 18:33
 */
public class TransactionTask implements Callable<Transaction> {

	private Logger logger = LoggerFactory.getLogger(TransactionTask.class);

	private Transaction tx;

	public TransactionTask(Transaction tx) {
		this.tx = tx;
	}

	@Override
	public Transaction call() throws Exception {

		try {
			logger.info("Call() tx: {}", tx.toString());

			PeerClient peer = WorldManager.getInstance().getActivePeer();
			WalletTransaction walletTransaction = WorldManager.getInstance()
					.getWallet().addByWalletTransaction(tx);
			peer.getP2pHandler().sendTransaction(tx);

			while (walletTransaction.getApproved() < 1) {
				sleep(10);
			}
			logger.info("return approved: {}", walletTransaction.getApproved());
		} catch (Throwable th) {
			logger.warn("Exception caught: {}", th);
			WorldManager.getInstance().getWallet().removeTransaction(tx);
		}
		return null;
	}
}
