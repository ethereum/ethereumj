package org.ethereum.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class EventDispatchThread {
    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private final static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void invokeLater(final Runnable r) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    r.run();
                } catch (Exception e) {
                    logger.error("EDT task exception", e);
                }
            }
        });
    }
}
