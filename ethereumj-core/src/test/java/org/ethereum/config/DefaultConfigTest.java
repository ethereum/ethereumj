/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
 *
 *
 */

package org.ethereum.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import org.ethereum.db.PruneManager;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class DefaultConfigTest {
    /**
     * TODO: For better testability, consider making setDefaultUncaughtExceptionHandler pluggable or Spring configurable as an autowired list
     */
    @Test
    public void testConstruction() throws InterruptedException {
        ListAppender<ILoggingEvent> inMemoryAppender = new ListAppender<>();
        inMemoryAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("general");
        try {
            logger.setLevel(Level.DEBUG);
            logger.addAppender(inMemoryAppender);

            // Registers the safety net
            new DefaultConfig();

            // Trigger an exception in the background
            Executors.newSingleThreadExecutor().execute(new ExceptionThrower());
            Thread.sleep(600);

            ILoggingEvent firstException = inMemoryAppender.list.get(0);
            assertEquals("Uncaught exception", firstException.getMessage());

            IThrowableProxy cause = firstException.getThrowableProxy();
            assertEquals("Unit test throwing an exception", cause.getMessage());
        } finally {
            inMemoryAppender.stop();
            logger.detachAppender(inMemoryAppender);
        }
    }

    @Test
    public void testNoopPruneManager() throws Exception {
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.config = new SystemProperties();
        defaultConfig.config.overrideParams("database.prune.enabled", "false");

        PruneManager noopPruneManager = defaultConfig.pruneManager();
        // Should throw exception unless this is a NOOP prune manager
        noopPruneManager.blockCommitted(null);
    }

    private static class ExceptionThrower implements Runnable {
        @Override
        public void run() {
            throw new IllegalStateException("Unit test throwing an exception");
        }
    }
}