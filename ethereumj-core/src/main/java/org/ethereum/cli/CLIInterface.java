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

import org.apache.commons.lang3.BooleanUtils;
import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
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

                // process simple option
                if (processConnectOnly(arg, cliOptions))
                    continue;

                // possible additional parameter
                if (i + 1 >= args.length)
                    continue;

                // process options with additional parameter
                if (processDbDirectory(arg, args[i + 1], cliOptions))
                    continue;
                if (processListenPort(arg, args[i + 1], cliOptions))
                    continue;
                if (processConnect(arg, args[i + 1], cliOptions))
                    continue;
                if (processDbReset(arg, args[i + 1], cliOptions))
                    continue;
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

    private static boolean processConnectOnly(String arg, Map<String, Object> cliOptions) {
        if ("-connectOnly".equals(arg))
            return false;

        cliOptions.put(SystemProperties.PROPERTY_PEER_DISCOVERY_ENABLED, false);

        return true;
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
    private static boolean processConnect(String arg, String connectStr, Map<String, Object> cliOptions) throws URISyntaxException {
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

    // process database reset
    private static boolean processDbReset(String arg, String reset, Map<String, Object> cliOptions) {
        if (!"-reset".equals(arg))
            return false;

        Boolean resetFlag = interpret(reset);

        if (resetFlag == null) {
            throw new Error(String.format("Can't interpret DB reset arguments: %s %s", arg, reset));
        }

        logger.info("Resetting db set to [{}]", resetFlag);
        cliOptions.put(SystemProperties.PROPERTY_DB_RESET, resetFlag.toString());

        return true;
    }

    private static Boolean interpret(String arg) {
        return BooleanUtils.toBooleanObject(arg);
    }

    private static void printHelp() {

        System.out.println("--help                -- this help message ");
        System.out.println("-reset <yes/no>       -- reset yes/no the all database ");
        System.out.println("-db <db>              -- to setup the path for the database directory ");
        System.out.println("-listen  <port>       -- port to listen on for incoming connections ");
        System.out.println("-connect <enode://pubKey@host:port>  -- address actively connect to  ");
        System.out.println("-connectOnly <enode://pubKey@host:port>  -- like 'connect', but will not attempt to connect to other peers  ");
        System.out.println();
        System.out.println("e.g: cli -reset no -db db-1 -listen 20202 -connect enode://0be5b4@poc-7.ethdev.com:30300 ");
        System.out.println();

    }


}
