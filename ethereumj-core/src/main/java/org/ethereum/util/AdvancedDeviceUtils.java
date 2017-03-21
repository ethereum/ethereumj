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
package org.ethereum.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import org.ethereum.config.SystemProperties;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author Roman Mandeleil
 * @since 25.07.2014
 */
public class AdvancedDeviceUtils {

    public static void adjustDetailedTracing(SystemProperties config, long blockNum) {
        // here we can turn on the detail tracing in the middle of the chain
        if (blockNum >= config.traceStartBlock() && config.traceStartBlock() != -1) {
            final URL configFile = ClassLoader.getSystemResource("logback-detailed.xml");
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final ContextInitializer ci = new ContextInitializer(loggerContext);

            loggerContext.reset();
            try {
                ci.configureByResource(configFile);
            } catch (Exception e) {
                System.out.println("Error applying new config " + e.getMessage());
            }
        }
    }
}
