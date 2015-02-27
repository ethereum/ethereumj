package org.ethereum.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.ethereum.cli.CLIInterface;

import static org.ethereum.config.KeysDefaults.*;
import static org.ethereum.config.ConfigUtils.*;

public final class PropertiesSystemProperties extends StringSourceSystemProperties {
    private final Properties prop;

    public PropertiesSystemProperties() throws IOException {
	File file = new File( TRADITIONAL_PROPS_FILENAME );
	if (file.exists()) {
	    this.prop = ensurePrefixedProperties( loadPropertiesFile( file ) );
	    logger.debug("{}: config loaded from {}", this.getClass().getName(), TRADITIONAL_PROPS_FILENAME );
	} else {
	    URL url = SystemProperties.class.getClassLoader().getResource( TRADITIONAL_PROPS_RESOURCE );
	    if (url == null) {
		logger.error("{}: Sorry, unable to find file {} or resource {}", this.getClass().getName(), TRADITIONAL_PROPS_FILENAME, TRADITIONAL_PROPS_RESOURCE);
		this.prop = new Properties();
	    } else {
		this.prop = ensurePrefixedProperties( loadPropertiesURL( url ) );
		logger.debug("{}: config loaded from resource {}", this.getClass().getName(), TRADITIONAL_PROPS_FILENAME );
	    }
	}
	applyCommandLineOverrides();
    }

    /*
     *
     *  Private utilities
     *
     */
    private void applyCommandLineOverrides() {
	Map<String,Object> overrides = CLIInterface.getConfigOverrides();
	if ( overrides != null ) {
	    for ( Map.Entry<String,Object> entry : overrides.entrySet() ) {
		String k = entry.getKey();
		Object v = entry.getValue();
		prop.setProperty( k, String.valueOf( v ) );
		logger.debug("Applied command line config override: {} -> {}", k, v);
	    }
	} else {
	    logger.debug("Command-line overrides have not been set, not even to an empty Map. Presumably this application was not run as a command-line application");
	}
    }

    /*
     *
     *  Abstract method implementations
     *
     */
    protected String getStringOrNull( String key ) {
	String value = System.getProperty( key );  // accept System.property overrides
	if ( value == null ) value = prop.getProperty( key );
	return value;
    }
}
