package org.ethereum.gui;

import org.ethereum.core.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 26.05.2014
 */
public class DialogWorker extends SwingWorker<Transaction, Object> {

    private static Logger logger = LoggerFactory.getLogger(DialogWorker.class);

    private Transaction tx;
    private MessageAwareDialog dialog;

    public DialogWorker(Transaction tx, MessageAwareDialog dialog) {
        this.tx = tx;
        this.dialog = dialog;
    }

    @Override
    protected Transaction doInBackground() throws Exception {

        Future<Transaction> future = UIEthereumManager.ethereum.submitTransaction(tx);
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
        return null;
    }
}
