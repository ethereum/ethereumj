package org.ethereum.cli;

import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class CLIInterface {

    private static final Logger logger = LoggerFactory.getLogger("cli");


    public static void call(String[] args) {

        try {
            Map<String, String> cliOptions = new HashMap<>();
            for (int i = 0; i < args.length; ++i) {

                // override the db directory
                if (args[i].equals("--help")) {

                    printHelp();
                    System.exit(1);
                }

                // override the db directory
                if (args[i].equals("-db") && i + 1 < args.length) {
                    String db = args[i + 1];
                    logger.info("DB directory set to [{}]", db);
                    cliOptions.put(SystemProperties.PROPERTY_DB_DIR, db);
                }

                // override the listen port directory
                if (args[i].equals("-listen") && i + 1 < args.length) {
                    String port = args[i + 1];
                    logger.info("Listen port set to [{}]", port);
                    cliOptions.put(SystemProperties.PROPERTY_LISTEN_PORT, port);
                }

                // override the connect host:port directory
                if (args[i].equals("-connect") && i + 1 < args.length) {
                    String connectStr = args[i + 1];
                    logger.info("Connect URI set to [{}]", connectStr);
                    URI uri = new URI(connectStr);
                    if (!uri.getScheme().equals("enode"))
                        throw new RuntimeException("expecting URL in the format enode://PUBKEY@HOST:PORT");
                    cliOptions.put(SystemProperties.PROPERTY_PEER_ACTIVE, "[{url='" + connectStr + "'}]");
                }

                // override the listen port directory
                if (args[i].equals("-reset") && i + 1 < args.length) {
                    Boolean resetStr = interpret(args[i + 1]);
                    logger.info("Resetting db set to [{}]", resetStr);
                    cliOptions.put(SystemProperties.PROPERTY_DB_RESET, resetStr.toString());
                }
            }

            logger.info("Overriding config file with CLI options: " + cliOptions);
            SystemProperties.CONFIG.overrideParams(cliOptions);

        } catch (Throwable e) {
            logger.error("Error parsing command line: [{}]", e.getMessage());
            System.exit(1);
        }
    }

    private static Boolean interpret(String arg) {

        if (arg.equals("on") || arg.equals("true") || arg.equals("yes")) return true;
        if (arg.equals("off") || arg.equals("false") || arg.equals("no")) return false;

        throw new Error("Can't interpret the answer: " + arg);
    }

    private static void printHelp() {

        System.out.println("--help                -- this help message ");
        System.out.println("-reset <yes/no>       -- reset yes/no the all database ");
        System.out.println("-db <db>              -- to setup the path for the database directory ");
        System.out.println("-listen  <port>       -- port to listen on for incoming connections ");
        System.out.println("-connect <host:port>  -- address actively connect to  ");
        System.out.println("");
        System.out.println("e.g: cli -reset no -db db-1 -listen 20202 -connect poc-7.ethdev.com:30300 ");
        System.out.println("");

    }


}
