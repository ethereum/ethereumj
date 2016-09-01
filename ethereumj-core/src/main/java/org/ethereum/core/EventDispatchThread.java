package org.ethereum.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * The class intended to serve as an 'Event Bus' where all EthereumJ events are
 * dispatched asynchronously from component to component or from components to
 * the user event handlers.
 *
 * This made for decoupling different components which are intended to work
 * asynchronously and to avoid complex synchronisation and deadlocks between them
 *
 * Created by Anton Nashatyrev on 29.12.2015.
 */
@Component
public class EventDispatchThread {
    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private final BlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
    private final ExecutorService executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS, executorQueue, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "EDT");
        }
    });

    private static EventDispatchThread eventDispatchThread;

    /**
     * Returns the default instance for initialization of Autowired instances
     * to be used in tests
     */
    public static EventDispatchThread getDefault() {
        if (eventDispatchThread == null) {
            eventDispatchThread = new EventDispatchThread() {
                @Override
                public void invokeLater(Runnable r) {
                    r.run();
                }
            };
        }
        return eventDispatchThread;
    }

    public void invokeLater(final Runnable r) {
        if (executor.isShutdown()) return;
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    long s = System.nanoTime();
                    r.run();
                    long t = (System.nanoTime() - s) / 1_000_000;
                    if (t > 1000) {
                        logger.warn("EDT task executed in more than 1 sec: " + r + " ms, " + this +
                        ". Executor queue size: " + executorQueue.size());

                    }
                } catch (Exception e) {
                    logger.error("EDT task exception", e);
                }
            }
        });
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
