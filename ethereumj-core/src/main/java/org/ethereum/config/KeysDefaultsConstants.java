package org.ethereum.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class KeysDefaultsConstants {
    final static String K_BLOCKCHAIN_ONLY             = "blockchain.only";
    final static String K_COINBASE_SECRET             = "coinbase.secret";
    final static String K_DUMP_BLOCK                  = "dump.block";
    final static String K_DUMP_CLEAN_ON_RESTART       = "dump.clean.on.restart";
    final static String K_DUMP_DIR                    = "dump.dir";
    final static String K_DUMP_FULL                   = "dump.full";
    final static String K_DUMP_STYLE                  = "dump.style";
    final static String K_DATABASE_DIR                = "database.dir";
    final static String K_DATABASE_RESET              = "database.reset";
    final static String K_HELLO_PHRASE                = "hello.phrase";
    final static String K_KEYVALUE_DATASOURCE         = "keyvalue.datasource";
    final static String K_MAX_BLOCKS_ASK              = "max.blocks.ask";
    final static String K_MAX_HASHES_ASK              = "max.hashes.ask";
    final static String K_MAX_BLOCKS_QUEUED           = "max.blocks.queued";
    final static String K_PEER_ACTIVE_IP              = "peer.active.ip";
    final static String K_PEER_ACTIVE_PORT            = "peer.active.port";
    final static String K_PEER_CAPABILITIES           = "peer.capabilities";
    final static String K_PEER_CHANNEL_READ_TIMEOUT   = "peer.channel.read.timeout";
    final static String K_PEER_CONNECTION_TIMEOUT     = "peer.connection.timeout";
    final static String K_PEER_DISCOVERY_ENABLED      = "peer.discovery.enabled";
    final static String K_PEER_DISCOVERY_WORKERS      = "peer.discovery.workers";
    final static String K_PEER_DISCOVERY_IP_LIST      = "peer.discovery.ip.list";
    final static String K_PEER_LISTEN_PORT            = "peer.listen.port";
    final static String K_PLAY_VM                     = "play.vm";
    final static String K_PROJECT_VERSION             = "project.version";
    final static String K_RECORD_BLOCKS               = "record.blocks";
    final static String K_ROOT_HASH_START             = "root.hash.start";
    final static String K_SAMPLES_DIR                 = "samples.dir";
    final static String K_TRANSACTION_APPROVE_TIMEOUT = "transaction.approve.timeout";
    final static String K_TRACE_STARTBLOCK            = "trace.startblock";
    final static String K_VM_STRUCTURED_DIR           = "vm.structured.dir";
    final static String K_VM_STRUCTURED_TRACE         = "vm.structured.trace";

    //testing, this is an odd key
    final static String K_VM_TEST_LOAD_LOCAL = "GitHubTests.VMTest.loadLocal";

    final static Map<String,Object> DEFAULTS;

    // utilities for implementations in this package
    final static String TRADITIONAL_PROPS_FILENAME;
    final static String TRADITIONAL_PROPS_RESOURCE;

    final static Set<String> ORDERED_KEYS;

    static {
	String userDir = System.getProperty( "user.dir" );

	TRADITIONAL_PROPS_FILENAME = userDir + "/config/system.properties";
	TRADITIONAL_PROPS_RESOURCE = "system.properties";

	// the values below are the fallback, hard-coded default values
	// of config params.
	Map<String,Object> tmpDefaults = new HashMap<>();
	tmpDefaults.put( K_BLOCKCHAIN_ONLY,             false                    );
	tmpDefaults.put( K_COINBASE_SECRET,             "monkey"                 );
	tmpDefaults.put( K_DUMP_BLOCK,                  0                        );
	tmpDefaults.put( K_DUMP_CLEAN_ON_RESTART,       true                     );
	tmpDefaults.put( K_DUMP_DIR,                    "dmp"                    );
	tmpDefaults.put( K_DUMP_FULL,                   false                    );
	tmpDefaults.put( K_DUMP_STYLE,                  "standard+"              );
	tmpDefaults.put( K_DATABASE_DIR,                "database"               );
	tmpDefaults.put( K_DATABASE_RESET,              false                    );
	tmpDefaults.put( K_HELLO_PHRASE,                "Dev"                    );
	tmpDefaults.put( K_KEYVALUE_DATASOURCE,         "leveldb"                );
	tmpDefaults.put( K_MAX_BLOCKS_ASK,              10                       );
	tmpDefaults.put( K_MAX_HASHES_ASK,              -1                       );
	tmpDefaults.put( K_MAX_BLOCKS_QUEUED,           300                      );
	tmpDefaults.put( K_PEER_ACTIVE_IP,              "poc-7.ethdev.com"       );
	tmpDefaults.put( K_PEER_ACTIVE_PORT,            30303                    );
	tmpDefaults.put( K_PEER_CAPABILITIES, "         eth,shh"                 );
	tmpDefaults.put( K_PEER_CHANNEL_READ_TIMEOUT,   5                        );
	tmpDefaults.put( K_PEER_CONNECTION_TIMEOUT,     10                       );
	tmpDefaults.put( K_PEER_DISCOVERY_ENABLED,      true                     );
	tmpDefaults.put( K_PEER_DISCOVERY_WORKERS,      2                        );
	tmpDefaults.put( K_PEER_DISCOVERY_IP_LIST,      "poc-7.ethdev.com:30303" );
	tmpDefaults.put( K_PEER_LISTEN_PORT,            30303                    );
	tmpDefaults.put( K_PLAY_VM,                     true                     );
	tmpDefaults.put( K_PROJECT_VERSION,             ""                       );
	tmpDefaults.put( K_RECORD_BLOCKS,               false                    );
	tmpDefaults.put( K_ROOT_HASH_START,             -1                       );
	tmpDefaults.put( K_SAMPLES_DIR,                 "samples"                );
	tmpDefaults.put( K_TRANSACTION_APPROVE_TIMEOUT, 10                       );
	tmpDefaults.put( K_TRACE_STARTBLOCK,            -1                       );
	tmpDefaults.put( K_VM_STRUCTURED_DIR,           "dmp"                    );
	tmpDefaults.put( K_VM_STRUCTURED_TRACE,         false                    );
	tmpDefaults.put( K_VM_TEST_LOAD_LOCAL,          false                    );

	DEFAULTS = Collections.unmodifiableMap( tmpDefaults );

	ORDERED_KEYS = Collections.unmodifiableSet( new TreeSet<String>( DEFAULTS.keySet() ) );
    }
}
