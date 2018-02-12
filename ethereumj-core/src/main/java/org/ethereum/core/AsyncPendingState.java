package org.ethereum.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Processes pending state in asynchronous fashion.
 *
 * @author Mikhail Kalinin
 * @since 09.02.2018
 */
@Lazy @Component(value = "asyncPendingState")
public class AsyncPendingState implements PendingState {

    private static final Logger logger = LoggerFactory.getLogger("pending");

    PendingState delegate;

    private final BlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>() {
        @Override
        public void put(Runnable runnable) throws InterruptedException {
            super.clear();
            super.put(runnable);
        }
    };
    private final ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.MILLISECONDS, executorQueue, r -> new Thread(r, "AsyncPendingStateThread")
    );

    @Autowired
    public AsyncPendingState(@Qualifier("pendingStateImpl") PendingState delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Transaction> addPendingTransactions(List<Transaction> transactions) {
        return delegate.addPendingTransactions(transactions);
    }

    @Override
    public void addPendingTransaction(Transaction tx) {
        delegate.addPendingTransaction(tx);
    }

    @Override
    public void processBest(Block block, List<TransactionReceipt> receipts) {
        logger.debug("Queue: " + block.getShortDescr());
        executor.submit(() -> delegate.processBest(block, receipts));
    }

    @Override
    public Repository getRepository() {
        return delegate.getRepository();
    }

    @Override
    public List<Transaction> getPendingTransactions() {
        return delegate.getPendingTransactions();
    }

    public void shutdown() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("shutdown: executor interrupted: {}", e.getMessage());
        }
    }
}
