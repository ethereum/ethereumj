package org.ethereum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    final static String ETHEREUMJ_PREFIX     = "ethereumj.";
    final static int    ETHEREUMJ_PREFIX_LEN = ETHEREUMJ_PREFIX.length();

    final static String K_BLOCKCHAIN_ONLY             = ETHEREUMJ_PREFIX + "blockchain.only";
    final static String K_COINBASE_SECRET             = ETHEREUMJ_PREFIX + "coinbase.secret";
    final static String K_DUMP_BLOCK                  = ETHEREUMJ_PREFIX + "dump.block";
    final static String K_DUMP_CLEAN_ON_RESTART       = ETHEREUMJ_PREFIX + "dump.clean.on.restart";
    final static String K_DUMP_DIR                    = ETHEREUMJ_PREFIX + "dump.dir";
    final static String K_DUMP_FULL                   = ETHEREUMJ_PREFIX + "dump.full";
    final static String K_DUMP_STYLE                  = ETHEREUMJ_PREFIX + "dump.style";
    final static String K_DATABASE_DIR                = ETHEREUMJ_PREFIX + "database.dir";
    final static String K_DATABASE_RESET              = ETHEREUMJ_PREFIX + "database.reset";
    final static String K_HELLO_PHRASE                = ETHEREUMJ_PREFIX + "hello.phrase";
    final static String K_KEYVALUE_DATASOURCE         = ETHEREUMJ_PREFIX + "keyvalue.datasource";
    final static String K_MAX_BLOCKS_ASK              = ETHEREUMJ_PREFIX + "max.blocks.ask";
    final static String K_MAX_HASHES_ASK              = ETHEREUMJ_PREFIX + "max.hashes.ask";
    final static String K_MAX_BLOCKS_QUEUED           = ETHEREUMJ_PREFIX + "max.blocks.queued";
    final static String K_PEER_ACTIVE_IP              = ETHEREUMJ_PREFIX + "peer.active.ip";
    final static String K_PEER_ACTIVE_PORT            = ETHEREUMJ_PREFIX + "peer.active.port";
    final static String K_PEER_CAPABILITIES           = ETHEREUMJ_PREFIX + "peer.capabilities";
    final static String K_PEER_CHANNEL_READ_TIMEOUT   = ETHEREUMJ_PREFIX + "peer.channel.read.timeout";
    final static String K_PEER_CONNECTION_TIMEOUT     = ETHEREUMJ_PREFIX + "peer.connection.timeout";
    final static String K_PEER_DISCOVERY_ENABLED      = ETHEREUMJ_PREFIX + "peer.discovery.enabled";
    final static String K_PEER_DISCOVERY_WORKERS      = ETHEREUMJ_PREFIX + "peer.discovery.workers";
    final static String K_PEER_DISCOVERY_IP_LIST      = ETHEREUMJ_PREFIX + "peer.discovery.ip.list";
    final static String K_PEER_LISTEN_PORT            = ETHEREUMJ_PREFIX + "peer.listen.port";
    final static String K_PLAY_VM                     = ETHEREUMJ_PREFIX + "play.vm";
    final static String K_PROJECT_VERSION             = ETHEREUMJ_PREFIX + "project.version";
    final static String K_RECORD_BLOCKS               = ETHEREUMJ_PREFIX + "record.blocks";
    final static String K_ROOT_HASH_START             = ETHEREUMJ_PREFIX + "root.hash.start";
    final static String K_SAMPLES_DIR                 = ETHEREUMJ_PREFIX + "samples.dir";
    final static String K_TRANSACTION_APPROVE_TIMEOUT = ETHEREUMJ_PREFIX + "transaction.approve.timeout";
    final static String K_TRACE_STARTBLOCK            = ETHEREUMJ_PREFIX + "trace.startblock";
    final static String K_VM_STRUCTURED_DIR           = ETHEREUMJ_PREFIX + "vm.structured.dir";
    final static String K_VM_STRUCTURED_TRACE         = ETHEREUMJ_PREFIX + "vm.structured.trace";
    final static String K_VM_TEST_LOAD_LOCAL          = ETHEREUMJ_PREFIX + "GitHubTests.VMTest.loadLocal"; //testing, this is an odd key


    final static String SYSPROP_PLUGIN_PATH_APPEND  = "config.plugin.path.append";
    final static String SYSPROP_PLUGIN_PATH_PREPEND = "config.plugin.path.prepend";
    final static String SYSPROP_PLUGIN_PATH_REPLACE = "config.plugin.path.replace";

    // utilities for implementations in this package
    final static String DEFAULT_PLUGIN_PATH="org.ethereum.config.TypesafeConfigSystemProperties,org.ethereum.config.PropertiesSystemProperties";

    // stuff to be set-up in the static initializer
    final static Map<String,Object> DEFAULTS;

    final static String TRADITIONAL_PROPS_FILENAME;
    final static String TRADITIONAL_PROPS_RESOURCE_BASENAME;
    final static String TRADITIONAL_PROPS_RESOURCE;

    final static Set<String> ORDERED_KEYS;

    final static int MAX_KEY_LEN;

    static {
	String userDir = System.getProperty( "user.dir" );

	TRADITIONAL_PROPS_FILENAME          = userDir + "/config/system.properties";
	TRADITIONAL_PROPS_RESOURCE_BASENAME = "system";
	TRADITIONAL_PROPS_RESOURCE          = TRADITIONAL_PROPS_RESOURCE_BASENAME + ".properties";

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

	Set tmpOrderedKeys = new TreeSet<String>( String.CASE_INSENSITIVE_ORDER ); // to deal with one ugly mixed-case key...
	tmpOrderedKeys.addAll( DEFAULTS.keySet() );
	ORDERED_KEYS = Collections.unmodifiableSet( tmpOrderedKeys );

	int maxLen = -1;
	for ( String key : ORDERED_KEYS ) maxLen = Math.max( maxLen, key.length() );
	MAX_KEY_LEN = maxLen;
    }

    // publish access to constants outside the config package only via methods
    public static Map<String,Object> defaultConfig()     { return DEFAULTS; }

    public static Logger getConfigPluginLogger()         { return configPluginLogger; }

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

    // used by other classes in this package, not used by this class, not published outside the package
    final static Logger logger = LoggerFactory.getLogger("config");

    // to be used by plug-in writers outside the package, via public accessor
    private final static Logger configPluginLogger = LoggerFactory.getLogger("config-plugin");
}
