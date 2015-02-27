package org.ethereum.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Properties;

import com.typesafe.config.*;

import static org.ethereum.config.KeysDefaults.*;
import static org.ethereum.config.ConfigUtils.*;


public class TypesafeConfigSystemProperties extends SystemProperties {

    final static Config ACTIVE;
    
    static {
	ClassLoader cl = SystemProperties.class.getClassLoader(); 

	ConfigParseOptions referenceDefaultsOptions = ConfigParseOptions.defaults()
	    .setSyntax( ConfigSyntax.CONF )
	    .setAllowMissing( true );

	// we don't just use ConfigFactor.defaultReference() because we don't want System Property overrides applied at this point.
	Config referenceDefaults = ConfigFactory.parseResourcesAnySyntax( cl, "reference", referenceDefaultsOptions );

	Config traditionalPropertiesConfigResource;
	try {
	    URL url = SystemProperties.class.getClassLoader().getResource( TRADITIONAL_PROPS_RESOURCE );
	    if (url == null) {
		traditionalPropertiesConfigResource = ConfigFactory.empty();
	    } else {
		Properties props = ensurePrefixedProperties( loadPropertiesURL( url ) );
		traditionalPropertiesConfigResource = ConfigFactory.parseProperties( props );
	    }
	} catch ( IOException e ) {
	    logger.warn( "IOException while trying to read resource " + TRADITIONAL_PROPS_RESOURCE + ", skipping...", e );
	    traditionalPropertiesConfigResource = ConfigFactory.empty();
	}

	Config applicationSettings = applicationOrStandardSubstitute( cl );

	Config traditionalPropertiesConfigFile;
	try {
	    File f = new File( TRADITIONAL_PROPS_FILENAME );
	    if (! f.exists()) {
		traditionalPropertiesConfigFile = ConfigFactory.empty();
	    } else {
		Properties props = ensurePrefixedProperties( loadPropertiesFile( f ) );
		traditionalPropertiesConfigFile = ConfigFactory.parseProperties( props );
	    }
	} catch ( IOException e ) {
	    logger.warn( "IOException while trying to read file " + TRADITIONAL_PROPS_FILENAME + ", skipping...", e );
	    traditionalPropertiesConfigFile = ConfigFactory.empty();
	}
	
	Config sysPropOverrides = ConfigFactory.defaultOverrides();

	ACTIVE = sysPropOverrides
	    .withFallback( traditionalPropertiesConfigFile )
	    .withFallback( applicationSettings )
	    .withFallback( traditionalPropertiesConfigResource )
	    .withFallback( referenceDefaults )
	    .resolve();

	// note the reversed order of referenceDefaults and sysPropertyOverrides.
	// we really want to just check out referenceDefaults, but we need to resolve ${user.conf}
	Config referenceDefaultsForComparison = referenceDefaults.withFallback( sysPropOverrides ).resolve();
	logCompareToDefaults( referenceDefaultsForComparison, "reference.conf" );
	
	logCompare( referenceDefaultsForComparison, traditionalPropertiesConfigResource, "reference.conf", "resource:system.properties" );
    }
    
    static void logCompare( Config config0, Config config1, String configName0, String configName1 ) {
	for ( String key : Keys.all() ) {
	    
	    String cfgValue0;
	    if ( config0.hasPath( key ) ) cfgValue0 = config0.getString( key );
	    else cfgValue0 = null;
	    
	    String cfgValue1;
	    if ( config1.hasPath( key ) ) cfgValue1 = config1.getString( key );
	    else cfgValue1 = null;
	    
	    if ( ( cfgValue0 == null && cfgValue1 != null ) || (! cfgValue0.equals( cfgValue1 ) ) ) {
		logger.debug("{} key {} differs from {}, {} vs. {}", configName0, key, configName1, cfgValue0, cfgValue1);
	    }
	}
    }

    static void logCompareToDefaults( Config config, String configName ) {
	for ( String key : Keys.all() ) {

	    String cfgValue;
	    if ( config.hasPath( key ) ) cfgValue = config.getString( key );
	    else cfgValue = null;

	    String defaultValue = String.valueOf( DEFAULTS.get( key ) );
	    if ( ! defaultValue.equals( cfgValue ) ) {
		logger.debug("{} key {} differs from default, {} vs. {}", configName, key, cfgValue, defaultValue);
	    }
	}
    }

    // it'd be great to implement this once with lambdas,
    // but i think ethereum-core wants to remain Java 7
    // compatible for now.

    /** May throw ClassCastExceptions */
    protected Boolean getBooleanOrNull( String key ) { 
	try {
	    if ( ACTIVE.hasPath( key ) ) 
		return ACTIVE.getBoolean( key ); 
	    else
		return null;
	} catch (ConfigException.WrongType e) {
	    return forceClassCastException( Boolean.class, e, key );
	}
    }

    /** May throw ClassCastExceptions */
    protected Integer getIntegerOrNull( String key ) {
	try {
	    if ( ACTIVE.hasPath( key ) ) 
		return ACTIVE.getInt( key ); 
	    else
		return null;
	} catch (ConfigException.WrongType e) {
	    return forceClassCastException( Integer.class, e, key );
	}
    }

    /** May throw ClassCastExceptions */
    protected String  getStringOrNull( String key ) {
	try {
	    if ( ACTIVE.hasPath( key ) ) 
		return ACTIVE.getString( key ); 
	    else
		return null;
	} catch (ConfigException.WrongType e) {
	    return forceClassCastException( String.class, e, key );
	}
    }

    /** May NOT throw ClassCastExceptions */
    protected String  getCoerceToStringOrNull( String key ) {
	if ( ACTIVE.hasPath( key ) ) 
	    return String.valueOf( ACTIVE.getValue( key ).unwrapped() );
	else
	    return null;
    }

    private <T> T forceClassCastException( Class<T> clz, ConfigException.WrongType e, String key ) {
	logger.debug( "Converting ConfigException.WrongType to ClassCastException.", e );
	return (T) ACTIVE.getValue( key ).unwrapped();
    }

    // applicationOrStandardSubstitute is copied/pasted/modified from the com.mchange.v3.hocon of mchange-commons-java.
    // Copied and pasted in to avoid a dependency on that package for now.
    /**
     *  For when you don't want all the extras of ConfigFactory.load() [ backing up to reference.conf, System property overrides, etc. ]
     */
    private static Config applicationOrStandardSubstitute(ClassLoader cl)  {
	String resourcePath = "application";

	Config out = null;
	
	// following typesafe-config standard behavior, we override
	// the standard identifier "application" if System properties
	// "config.resource", "config.file", or "config.url" are set.

	String check; 
	if ( ( check = System.getProperty("config.resource") ) != null ) {
	    resourcePath = check;
	} else if ( ( check = System.getProperty("config.file") ) != null ) {
	    File f = new File( check );
	    if ( f.exists() ) {
		if ( f.canRead() ) out = ConfigFactory.parseFile( f );
		else {
		    String msgTemplate = "config.file '%s' (specified as a System property) is not readable. Falling back to standard application.(conf|json|properties).";
		    logger.warn( String.format( msgTemplate, f.getAbsolutePath() ) );
		}
	    }
	    else { 
		String msgTemplate = "Specified config.file '%s' (specified as a System property) does not exist. Falling back to standard application.(conf|json|properties).";
		logger.warn( String.format("Specified config.file '%s' (specified as a System property) does not exist.", f.getAbsolutePath()) ); 
	    }
	} else if ( ( check = System.getProperty("config.url") ) != null ) {
	    try { out = ConfigFactory.parseURL( new URL( check ) ); }
	    catch ( MalformedURLException e ) {
		String msgTemplate = "Specified config.url '%s' (specified as a System property) could not be parsed. Falling back to standard application.(conf|json|properties).";
		logger.warn( String.format("Specified config.url '%s' (specified as a System property) could not be parsed.", check ) ); 
	    }
	}
	if ( out == null ) out = ConfigFactory.parseResourcesAnySyntax( cl, resourcePath );
	return out;
    }
}
