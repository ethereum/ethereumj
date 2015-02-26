package org.ethereum.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 * Note: This class should never, directly or indirectly,
 *       touch (cause initialization-on-load of)
 *
 *        org.ethereum.config.SystemProperties
 *
 *       The static initializer for the SystemProperties
 *       expects the static field CONFIG_OVERRIDES to
 *       have already been set when that class is 
 *       initialized.
 *
 *       It is (by construction) safe for this class to
 *       touch the class KeysDefaults in the config package.
 */
import org.ethereum.config.KeysDefaults.Keys;

/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class CLIInterface {

    private static final Logger logger = LoggerFactory.getLogger("cli");

    private static Map<String,Object> CONFIG_OVERRIDES = null;

    public static synchronized Map<String,Object> getConfigOverrides() {
	return CONFIG_OVERRIDES;
    }
    public static synchronized void setConfigOverrides( Map<String,Object> overrides ) {
	if ( CONFIG_OVERRIDES == null ) {
	    CONFIG_OVERRIDES = overrides;
	} else {
	    logger.error( "A command line application should only begin once! " +
			  "CLIInterface's call(...) method called multiple times. " +
			  "Any attempts to alter the (immutable) configuration will be ignored." );
	}
    }

    public static void call(String[] args) {

        try {
	    Map<String,Object> overrides = new HashMap<>();

            for (int i = 0; i < args.length; ++i) {

                // prints help message
                if (args[i].equals("--help")) {

                    printHelp();
                    System.exit(1);
                }

                // override the db directory
                if (args[i].equals("-db") && i + 1 < args.length) {
                    String db = args[i + 1];
                    logger.info("DB directory set to [{}]", db);
		    overrides.put( Keys.databaseDir(), db );
                }

                // override the listen port directory
                if (args[i].equals("-listen") && i + 1 < args.length) {
                    String port = args[i + 1];
                    logger.info("Listen port set to [{}]", port);
		    overrides.put( Keys.peerListenPort(), Integer.valueOf( port ) );
                }

                // override the connect host:port directory
                if (args[i].equals("-connect") && i + 1 < args.length) {
                    String connectStr = args[i + 1];
                    logger.info("Connect host:port set to [{}]", connectStr);
                    String[] params = connectStr.split(":");
                    String host = params[0];
                    String port = params[1];
		    overrides.put( Keys.peerActiveIP(), host );
		    overrides.put( Keys.peerActivePort(), Integer.valueOf( port ) );
                }

                // override whether the database should be reset
                if (args[i].equals("-reset") && i + 1 < args.length) {
                    Boolean reset = interpret(args[i + 1]);
                    logger.info("Resetting db set to [{}]", reset);
		    overrides.put( Keys.databaseReset(), reset );
                }
            }

	    CONFIG_OVERRIDES = Collections.unmodifiableMap( overrides );

            logger.info("");
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
