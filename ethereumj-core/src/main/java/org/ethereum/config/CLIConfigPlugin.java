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

    protected Object getLocalOrNull( String key, Class<?> expectedType ) { return attemptCoerce( overrides.get( key ), expectedType, key ); }
}
