package org.ethereum.gui;

import org.ethereum.core.Transaction;
import org.ethereum.manager.MainData;
import org.ethereum.net.submit.TransactionExecutor;
import org.ethereum.net.submit.TransactionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 26/05/2014 12:27
 */
public class DialogWorker extends SwingWorker {

	private static Logger logger = LoggerFactory.getLogger(DialogWorker.class);
	
    private Transaction tx;
    private MessageAwareDialog dialog;

    public DialogWorker(Transaction tx, MessageAwareDialog dialog) {
        this.tx = tx;
        this.dialog = dialog;
    }

    @Override
    protected Object doInBackground() throws Exception {
        TransactionTask transactionTask = new TransactionTask(tx);
        Future<Transaction> future = TransactionExecutor.instance.submitTransaction(transactionTask);
        dialog.infoStatusMsg("Transaction sent to the network, waiting for approve");

        try {
            future.get(CONFIG.transactionApproveTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException toe) {
            logger.error(toe.getMessage(), toe);
            dialog.alertStatusMsg("Transaction wasn't approved, network timeout");
            return null;
        } catch (InterruptedException ie) {
        	logger.error(ie.getMessage(), ie);
            dialog.alertStatusMsg("Transaction wasn't approved");
            return null;
        } catch (ExecutionException ee) {
        	logger.error(ee.getMessage(), ee);
            dialog.alertStatusMsg("Transaction wasn't approved");
            return null;
        } finally {
            future.cancel(true);
        }

        dialog.infoStatusMsg("Transaction got approved");
        MainData.instance.getWallet().applyTransaction(tx);
        return null;
    }
}
