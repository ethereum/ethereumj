package org.ethereum.gui;

import org.ethereum.core.Transaction;
import org.ethereum.manager.MainData;
import org.ethereum.net.submit.TransactionExecutor;
import org.ethereum.net.submit.TransactionTask;

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

    Transaction tx;
    MessageAwareDialog dialog;

    DialogWorker(Transaction tx, MessageAwareDialog dialog) {
        this.tx = tx;
        this.dialog = dialog;
    }

    @Override
    protected Object doInBackground() throws Exception {
        TransactionTask transactionTask = new TransactionTask(tx);
        Future future = TransactionExecutor.instance.submitTransaction(transactionTask);
        dialog.infoStatusMsg("Transaction sent to the network, waiting for approve");

        try {
            future.get(CONFIG.transactionApproveTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException e1) {
            e1.printStackTrace();
            dialog.alertStatusMsg("Transaction wasn't approved, network timeout");
            return null;
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            dialog.alertStatusMsg("Transaction wasn't approved");
            return null;
        } catch (ExecutionException e1) {
            e1.printStackTrace();
            dialog.alertStatusMsg("Transaction wasn't approved");
            return null;
        }

        dialog.infoStatusMsg("Transaction got approved");
        MainData.instance.getWallet().applyTransaction(tx);
        return null;
    }
}
