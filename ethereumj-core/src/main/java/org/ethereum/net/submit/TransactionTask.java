package org.ethereum.net.submit;

import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.server.ChannelManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

/**
 * @author Roman Mandeleil
 * @since 23.05.2014
 */
public class TransactionTask implements Callable<Transaction> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private final Transaction tx;
    private final WorldManager worldManager;

    public TransactionTask(Transaction tx, WorldManager worldManager) {
        this.tx = tx;
        this.worldManager = worldManager;
    }

    @Override
    public Transaction call() throws Exception {

        try {
            logger.info("submit tx: {}", tx.toString());
            ChannelManager channelManager = worldManager.getChannelManager();
            channelManager.sendTransaction(tx);
            return tx;

        } catch (Throwable th) {
            logger.warn("Exception caught: {}", th);
        }
        return null;
    }
}
