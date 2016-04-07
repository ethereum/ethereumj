package org.ethereum.net.submit;

import org.ethereum.core.Transaction;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

/**
 * @author Roman Mandeleil
 * @since 23.05.2014
 */
public class TransactionTask implements Callable<List<Transaction>> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private final List<Transaction> tx;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public TransactionTask(Transaction tx, ChannelManager channelManager) {
        this(Collections.singletonList(tx), channelManager);
    }

    public TransactionTask(List<Transaction> tx, ChannelManager channelManager) {
        this(tx, channelManager, null);
    }

    public TransactionTask(List<Transaction> tx, ChannelManager channelManager, Channel receivedFrom) {
        this.tx = tx;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public List<Transaction> call() throws Exception {

        try {
            logger.info("submit tx: {}", tx.toString());
            channelManager.sendTransaction(tx, receivedFrom);
            return tx;

        } catch (Throwable th) {
            logger.warn("Exception caught: {}", th);
        }
        return null;
    }
}
