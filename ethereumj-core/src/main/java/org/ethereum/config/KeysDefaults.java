package org.ethereum.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
 *
 *  To add new keys, be sure to update this file in three places:
 *     1) define the package-visible final static constant. (we hide to avoid publishing inline-able constants.)
 *     2) define a default, by adding that constant to the nascent DEFAULTS map in the static initializer
 *     3) define an accessor to the key as a static method of the Keys class.
 *
 *  Initializing this class should NOT force initialization of the SystemProperties class! Clients may rely
 *  upon the accessibility of the class prior to the materialization of the application's immutable config.
 *
 */
public final class KeysDefaults {
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

    // publish access to constants outside the config package only via methods
    
    public static Map<String,Object> defaultConfig()     { return DEFAULTS; }

    public static class Keys {
	public static Set<String> all()                  { return ORDERED_KEYS; }

	public static String blockchainOnly()            { return K_BLOCKCHAIN_ONLY; }
	public static String coinbaseSecret()            { return K_COINBASE_SECRET; }
	public static String dumpBlock()                 { return K_DUMP_BLOCK; }
	public static String dumpCleanOnRestart()        { return K_DUMP_CLEAN_ON_RESTART; } 
	public static String dumpDir()                   { return K_DUMP_DIR; }
	public static String dumpFull()                  { return K_DUMP_FULL; }
	public static String dumpStyle()                 { return K_DUMP_STYLE; }
	public static String databaseDir()               { return K_DATABASE_DIR; }
	public static String databaseReset()             { return K_DATABASE_RESET; }
	public static String helloPhrase()               { return K_HELLO_PHRASE; }
	public static String keyvalueDatasource()        { return K_KEYVALUE_DATASOURCE; }
	public static String maxBlocksAsk()              { return K_MAX_BLOCKS_ASK; }
	public static String maxHashesAsk()              { return K_MAX_HASHES_ASK; }
	public static String maxBlocksQueued()           { return K_MAX_BLOCKS_QUEUED; }
	public static String peerActiveIP()              { return K_PEER_ACTIVE_IP; }
	public static String peerActivePort()            { return K_PEER_ACTIVE_PORT; }
	public static String peerCapabilities()          { return K_PEER_CAPABILITIES; }
	public static String peerChannelReadTimeout()    { return K_PEER_CHANNEL_READ_TIMEOUT; }
	public static String peerConnectionTimeout()     { return K_PEER_CONNECTION_TIMEOUT; }
	public static String peerDiscoveryEnabled()      { return K_PEER_DISCOVERY_ENABLED; }
	public static String peerDiscoveryWorkers()      { return K_PEER_DISCOVERY_WORKERS; }
	public static String peerDiscoveryIPList()       { return K_PEER_DISCOVERY_IP_LIST; }
	public static String peerListenPort()            { return K_PEER_LISTEN_PORT; }
	public static String playVM()                    { return K_PLAY_VM; }
	public static String projectVersion()            { return K_PROJECT_VERSION; }
	public static String recordBlocks()              { return K_RECORD_BLOCKS; }
	public static String rootHashStart()             { return K_ROOT_HASH_START; }
	public static String samplesDir()                { return K_SAMPLES_DIR; }
	public static String transactionApproveTimeout() { return K_TRANSACTION_APPROVE_TIMEOUT; }
	public static String traceStartblock()           { return K_TRACE_STARTBLOCK; }
	public static String vmStructuredDir()           { return K_VM_STRUCTURED_DIR; }
	public static String vmStructuredTrace()         { return K_VM_STRUCTURED_TRACE; }
	public static String vmTestLoadLocal()           { return K_VM_TEST_LOAD_LOCAL; }
    }
}
