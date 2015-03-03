package org.ethereum.config;

import java.util.Arrays;
import java.util.List;

import static org.ethereum.config.KeysDefaults.*;

/**
 * Utility class to retrieve property values from the system.properties files
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {

    public final static SystemProperties CONFIG;

    static {
	ConfigPlugin head = ConfigPlugin.fromPluginPath( ConfigPlugin.vmPluginPath() );
	CONFIG = new SystemProperties( head );
	String[] defaultParsedPath = ConfigPlugin.parsePluginPath( DEFAULT_PLUGIN_PATH );
	if ( logger.isDebugEnabled() ) {
	    if (! head.matches( defaultParsedPath ) )
		logger.debug( "Using plugin path: {}", head.path() );
	    logger.debug( "main ethereumj configuration:" );
	    CONFIG.debugPrint();
	}
    }

    ConfigPlugin plugin;
    ConfigSource source; 

    SystemProperties( ConfigPlugin plugin )
    { 
	this.plugin = plugin; 
	this.source = new CachingConfigSource( plugin );
    }

    SystemProperties withOverrides( Class pluginClass ) throws Exception {
	return new SystemProperties( ConfigPlugin.instantiatePlugin( pluginClass, this.plugin ) );
    }

    /*
     *
     *  private utilities for fetching cached config values
     *
     */
    private void logClassCastError( String key, String expectedType, ClassCastException ick ) {
	logger.error( "Value for key '" + key + "' has an unexpected type. Should have been " + expectedType + '.', ick );
    }

    private String ensurePrefix( String key ) {
	if ( key.startsWith( ETHEREUMJ_PREFIX ) ) return key;
	else return ETHEREUMJ_PREFIX + key;
    }

    private boolean getBoolean( String key ) {
	try {
	    Object out = source.getOrNull( key, Boolean.class );
	    return ( (Boolean) ( out != null ? out : DEFAULTS.get( key ) ) ).booleanValue();
	} catch ( ClassCastException e ) {
	    logClassCastError( key, "boolean", e );
	    throw e;
	}
    }
    private int getInt( String key ) {
	try {
	    Object out = source.getOrNull( key, Integer.class );
	    return ( (Integer) ( out != null ? out : DEFAULTS.get( key ) ) ).intValue();
	} catch (ClassCastException e) {
	    logClassCastError( key, "int", e );
	    throw e;
	}
    }
    private String getString( String key ) {
	try {
	    Object out = source.getOrNull( key, String.class );
	    return (String) ( out != null ? out : DEFAULTS.get( key ) );
	} catch (ClassCastException e) {
	    logClassCastError( key, "int", e );
	    throw e;
	}
    }
    private String getCoerceToString( String key ) {
	Object raw = source.getOrNull( key, Object.class );
	return ( raw != null ? String.valueOf( raw ) : String.valueOf( DEFAULTS.get( key ) ) );
    }

    /*
     *
     *  Public accessors
     *
     */
    public Object getArbitrary( String key, Class<?> attemptCoerceTo ) {
	return source.getOrNull( key, attemptCoerceTo );
    }

    public String  activePeerIP()              { return getString( K_PEER_ACTIVE_IP ); }
    public int     activePeerPort()            { return getInt( K_PEER_ACTIVE_PORT ); }
    public boolean blockChainOnly()            { return getBoolean( K_BLOCKCHAIN_ONLY ); }
    public String  coinbaseSecret()            { return getString( K_COINBASE_SECRET ); }
    public String  databaseDir()               { return getString( K_DATABASE_DIR ); }
    public boolean databaseReset()             { return getBoolean( K_DATABASE_RESET ); }
    public int     dumpBlock()                 { return getInt( K_DUMP_BLOCK ); }
    public boolean dumpCleanOnRestart()        { return getBoolean( K_DUMP_CLEAN_ON_RESTART ); }
    public String  dumpDir()                   { return getString( K_DUMP_DIR ); }
    public boolean dumpFull()                  { return getBoolean( K_DUMP_FULL ); }
    public String  dumpStyle()                 { return getString( K_DUMP_STYLE ); }
    public String  helloPhrase()               { return getString( K_HELLO_PHRASE ); }
    public int     listenPort()                { return getInt( K_PEER_LISTEN_PORT ); }
    public int     maxBlocksAsk()              { return getInt( K_MAX_BLOCKS_ASK ); }
    public int     maxBlocksQueued()           { return getInt( K_MAX_BLOCKS_QUEUED ); }
    public int     maxHashesAsk()              { return getInt( K_MAX_HASHES_ASK ); }
    public int     peerChannelReadTimeout()    { return getInt( K_PEER_CHANNEL_READ_TIMEOUT ); }
    public boolean peerDiscoveryEnabled()      { return getBoolean( K_PEER_DISCOVERY_ENABLED ); }
    public String  peerDiscoveryIPList()       { return getString( K_PEER_DISCOVERY_IP_LIST ); }
    public int     peerDiscoveryWorkers()      { return getInt( K_PEER_DISCOVERY_WORKERS ); }
    public boolean playVM()                    { return getBoolean( K_PLAY_VM ); }
    public String  projectVersion()            { return getString( K_PROJECT_VERSION ); }
    public boolean recordBlocks()              { return getBoolean( K_RECORD_BLOCKS ); }
    public String  samplesDir()                { return getString( K_SAMPLES_DIR ); }
    public int     transactionApproveTimeout() { return getInt( K_TRANSACTION_APPROVE_TIMEOUT ); }
    public int     traceStartBlock()           { return getInt( K_TRACE_STARTBLOCK ); }
    public boolean vmTrace()                   { return getBoolean( K_VM_STRUCTURED_TRACE ); }
    public String  vmTraceDir()                { return getString( K_VM_STRUCTURED_DIR ); }
    public boolean vmTestLoadLocal()           { return getBoolean( K_VM_TEST_LOAD_LOCAL ); }

    public String  getKeyValueDataSource()     { return getString( K_KEYVALUE_DATASOURCE ); } 

    // special-case accessors
    public String rootHashStart() {
	int intVal = getInt( K_ROOT_HASH_START );
        return ( intVal == -1 ? null : String.valueOf( intVal ) );
    }

    public List<String> peerCapabilities() {
        String capabilitiesList = getString( K_PEER_CAPABILITIES );
        return Arrays.asList(capabilitiesList.split(","));
    }
    public int peerConnectionTimeout() { // configured in seconds, expected in millis
	return getInt( K_PEER_CONNECTION_TIMEOUT ) * 1000; 
    }

    interface LinePrinter {
	public void print( String key, Object value );
    }
    private static String kvline( String key, Object value ) { 
	return String.format( "    %-" + MAX_KEY_LEN + "s -> %s", key, value ); 
    }

    private LinePrinter infoPrinter = new LinePrinter() {
	    public void print( String key, Object value ) {
		if ( logger.isInfoEnabled() )
		    logger.info( kvline( key, value ) );
	    }
    };
    private LinePrinter debugPrinter = new LinePrinter() {
	    public void print( String key, Object value ) {
		if ( logger.isDebugEnabled() )
		    logger.debug( kvline( key, value ) );
	    }
    };

    private void print(LinePrinter lp) {
	for ( String key : ORDERED_KEYS ) {
            String value = getCoerceToString( key );
	    value = ( value == null ? "null" : value );
	    if ( K_COINBASE_SECRET.equals( key ) ) value = "[hidden]";
	    lp.print( key, value );
        }
    }

    public void debugPrint() { print( debugPrinter ); }
    public void infoPrint()  { print( infoPrinter ); }
    public void print()      { infoPrint(); }

    /*
     *
     * Testing
     *
     */
    public static void main(String args[]) {
        CONFIG.print();
    }
}
