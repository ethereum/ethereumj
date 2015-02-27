package org.ethereum.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.ethereum.cli.CLIInterface;

import static org.ethereum.config.KeysDefaults.*;
import static org.ethereum.config.ConfigUtils.*;

/*
 *  This plugin replicates the original behavior of the SystemProperties class
 *  prior to its refactoring. (It was the first plug-in refactored out of that class.)
 *  TypesafeConfigSystemProperties provides a superset of its behavior, so it is
 *  no longer in the default plugin path.
 */
public final class PropertiesSystemProperties extends StringSourceSystemProperties {
    private final Properties prop;

    public PropertiesSystemProperties() throws IOException {
	File file = new File( TRADITIONAL_PROPS_FILENAME );
	if (file.exists()) {
	    this.prop = ensurePrefixedProperties( loadPropertiesFile( file ) );
	    logger.debug("config loaded from {} [{}]", TRADITIONAL_PROPS_FILENAME, this.getClass().getSimpleName() );
	} else {
	    URL url = SystemProperties.class.getClassLoader().getResource( TRADITIONAL_PROPS_RESOURCE );
	    if (url == null) {
		logger.error("Sorry, unable to find file {} or resource {} [{}]", TRADITIONAL_PROPS_FILENAME, TRADITIONAL_PROPS_RESOURCE, this.getClass().getSimpleName() );
		this.prop = new Properties();
	    } else {
		this.prop = ensurePrefixedProperties( loadPropertiesURL( url ) );
		logger.debug("config loaded from resource {} [{}]", TRADITIONAL_PROPS_FILENAME, this.getClass().getSimpleName());
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
