package org.ethereum.config;

import java.util.Collections;
import java.util.Map;

import org.ethereum.cli.CLIInterface;

public final class CLIConfigPlugin extends ConfigPlugin {

    Map<String,Object> overrides;

    public CLIConfigPlugin(ConfigPlugin fallback) {
	super( fallback );
	this.overrides = CLIInterface.getConfigOverrides();
	if ( this.overrides == null ) this.overrides = Collections.emptyMap();
	for ( Map.Entry<String,Object> entry : overrides.entrySet() ) {
	    logger.debug("Found command line config parameter: {} -> {} [{}]", entry.getKey(), entry.getValue(), this.getClass().getSimpleName());
	}
    }

    /** May throw ClassCastExceptions */
    protected Boolean getLocalBooleanOrNull( String key ) { return (Boolean) overrides.get( key ); }

    /** May throw ClassCastExceptions */
    protected Integer getLocalIntegerOrNull( String key ) { return (Integer) overrides.get( key ); }

    /** May throw ClassCastExceptions */
    protected String getLocalStringOrNull( String key ) { return (String) overrides.get( key ); }

    /** May NOT throw ClassCastExceptions */
    protected String getLocalCoerceToStringOrNull( String key ) { 
	Object value = overrides.get( key );
	return ( value == null ? null : String.valueOf( key ) );
    }
}
