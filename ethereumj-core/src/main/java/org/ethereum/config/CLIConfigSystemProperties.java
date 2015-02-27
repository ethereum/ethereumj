package org.ethereum.config;

import java.util.Collections;
import java.util.Map;

import org.ethereum.cli.CLIInterface;

import org.slf4j.Logger;

public final class CLIConfigSystemProperties extends SystemProperties {

    private final static Logger logger = KeysDefaults.getConfigPluginLogger();

    Map<String,Object> overrides;

    public CLIConfigSystemProperties() {
	this.overrides = CLIInterface.getConfigOverrides();
	if ( this.overrides == null ) this.overrides = Collections.emptyMap();
	for ( Map.Entry<String,Object> entry : overrides.entrySet() ) {
	    logger.debug("Found command line config parameter: {} -> {} [{}]", entry.getKey(), entry.getValue(), this.getClass().getSimpleName());
	}
    }

    /** May throw ClassCastExceptions */
    protected Boolean getBooleanOrNull( String key ) { return (Boolean) overrides.get( key ); }

    /** May throw ClassCastExceptions */
    protected Integer getIntegerOrNull( String key ) { return (Integer) overrides.get( key ); }

    /** May throw ClassCastExceptions */
    protected String getStringOrNull( String key ) { return (String) overrides.get( key ); }

    /** May NOT throw ClassCastExceptions */
    protected String getCoerceToStringOrNull( String key ) { 
	Object value = overrides.get( key );
	return ( value == null ? null : String.valueOf( key ) );
    }
}
