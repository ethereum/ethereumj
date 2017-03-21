/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
    private static EventDispatchThread eventDispatchThread;

    private static final int[] queueSizeWarnLevels = new int[]{0, 10_000, 50_000, 100_000, 250_000, 500_000, 1_000_000, 10_000_000};

    private final BlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
    private final ExecutorService executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS, executorQueue, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "EDT");
        }
    });

    private long taskStart;
    private Runnable lastTask;
    private int lastQueueSizeWarnLevel = 0;
    private int counter;

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
        if (counter++ % 1000 == 0) logStatus();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    lastTask = r;
                    taskStart = System.nanoTime();
                    r.run();
                    long t = (System.nanoTime() - taskStart) / 1_000_000;
                    taskStart = 0;
                    if (t > 1000) {
                        logger.warn("EDT task executed in more than 1 sec: " + t + "ms, " +
                        "Executor queue size: " + executorQueue.size());

                    }
                } catch (Exception e) {
                    logger.error("EDT task exception", e);
                }
            }
        });
    }

    // monitors EDT queue size and prints warning if exceeds thresholds
    private void logStatus() {
        int curLevel = getSizeWarnLevel(executorQueue.size());
        if (lastQueueSizeWarnLevel == curLevel) return;

        synchronized (this) {
            if (curLevel > lastQueueSizeWarnLevel) {
                long t = taskStart == 0 ? 0 : (System.nanoTime() - taskStart) / 1_000_000;
                String msg = "EDT size grown up to " + executorQueue.size() + " (last task executing for " + t + " ms: " + lastTask;
                if (curLevel < 3) {
                    logger.info(msg);
                } else {
                    logger.warn(msg);
                }
            } else if (curLevel < lastQueueSizeWarnLevel) {
                logger.info("EDT size shrunk down to " + executorQueue.size());
            }
            lastQueueSizeWarnLevel = curLevel;
        }
    }

    private static int getSizeWarnLevel(int size) {
        int idx = Arrays.binarySearch(queueSizeWarnLevels, size);
        return idx >= 0 ? idx : -(idx + 1) - 1;
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
