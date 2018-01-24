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
package org.ethereum.cli;

import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class CLIInterface {

    private CLIInterface() {
    }

    private static final Logger logger = LoggerFactory.getLogger("general");


    public static void call(String[] args) {
        try {
            Map<String, Object> cliOptions = new HashMap<>();

            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];

                processHelp(arg);

                if (i + 1 < args.length) {
                    if (processDbDirectory(arg, args[i + 1], cliOptions))
                        continue;
                    if (processListenPort(arg, args[i + 1], cliOptions))
                        continue;
                    if (processConnect(arg, args[i + 1], cliOptions))
                        continue;
                }

                if ("-connectOnly".equals(arg)) {
                    cliOptions.put(SystemProperties.PROPERTY_PEER_DISCOVERY_ENABLED, false);
                }

                // override the listen port directory
                if ("-reset".equals(arg) && i + 1 < args.length) {
                    Boolean resetStr = interpret(args[i + 1]);
                    logger.info("Resetting db set to [{}]", resetStr);
                    cliOptions.put(SystemProperties.PROPERTY_DB_RESET, resetStr.toString());
                }
            }

            if (cliOptions.size() > 0) {
                logger.info("Overriding config file with CLI options: {}", cliOptions);
            }

            SystemProperties.getDefault().overrideParams(cliOptions);

        } catch (Throwable e) {
            logger.error("Error parsing command line: [{}]", e.getMessage());
            System.exit(1);
        }
    }

    // show help
    private static void processHelp(String arg) {
        if ("--help".equals(arg)) {
            printHelp();

            System.exit(1);
        }
    }

    // override the db directory
    private static boolean processDbDirectory(String arg, String db, Map<String, Object> cliOptions) {
        if (!"-db".equals(arg))
            return false;

        logger.info("DB directory set to [{}]", db);

        cliOptions.put(SystemProperties.PROPERTY_DB_DIR, db);

        return true;
    }

    // override the listen port directory
    private static boolean processListenPort(String arg, String port, Map<String, Object> cliOptions) {
        if (!"-listen".equals(arg))
            return false;

        logger.info("Listen port set to [{}]", port);

        cliOptions.put(SystemProperties.PROPERTY_LISTEN_PORT, port);

        return true;
    }

    // override the connect host:port directory
    private static boolean processConnect(String arg, String connectStr, Map<String, Object> cliOptions) {
        if (!arg.startsWith("-connect"))
            return false;

        logger.info("Connect URI set to [{}]", connectStr);
        URI uri = new URI(connectStr);

        if (!"enode".equals(uri.getScheme()))
            throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT");

        List<Map<String, String>> peerActiveList = Collections.singletonList(Collections.singletonMap("url", connectStr));

        cliOptions.put(SystemProperties.PROPERTY_PEER_ACTIVE, peerActiveList);

        return true;
    }

    private static Boolean interpret(String arg) {
        if ("on".equals(arg) || "true".equals(arg) || "yes".equals(arg)) return true;
        if ("off".equals(arg) || "false".equals(arg) || "no".equals(arg)) return false;

        throw new Error("Can't interpret the answer: " + arg);
    }

    private static void printHelp() {

        System.out.println("--help                -- this help message ");
        System.out.println("-reset <yes/no>       -- reset yes/no the all database ");
        System.out.println("-db <db>              -- to setup the path for the database directory ");
        System.out.println("-listen  <port>       -- port to listen on for incoming connections ");
        System.out.println("-connect <enode://pubKey@host:port>  -- address actively connect to  ");
        System.out.println("-connectOnly <enode://pubKey@host:port>  -- like 'connect', but will not attempt to connect to other peers  ");
        System.out.println("");
        System.out.println("e.g: cli -reset no -db db-1 -listen 20202 -connect enode://0be5b4@poc-7.ethdev.com:30300 ");
        System.out.println("");

    }


}
