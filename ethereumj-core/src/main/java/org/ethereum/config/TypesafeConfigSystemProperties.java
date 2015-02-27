package org.ethereum.config;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

import com.typesafe.config.*;

import static org.ethereum.config.KeysDefaults.*;

public class TypesafeConfigSystemProperties extends SystemProperties {

    final static Config ACTIVE;
    
    static {
	ClassLoader cl = SystemProperties.class.getClassLoader(); 

	ConfigParseOptions resourceDefaultsOptions = ConfigParseOptions.defaults()
	    .setSyntax( ConfigSyntax.CONF )
	    .setAllowMissing( true );

	ConfigParseOptions propertiesParseOptions = ConfigParseOptions.defaults()
	    .setSyntax( ConfigSyntax.PROPERTIES )
	    .setAllowMissing( true );


	// we don't just use ConfigFactor.defaultReference() because we don't want System Property overrides applied at this point.
	Config resourceDefaults = ConfigFactory.parseResourcesAnySyntax( cl, "reference", resourceDefaultsOptions );

	Config traditionalPropertiesConfigResource = ConfigFactory.parseResourcesAnySyntax( cl, TRADITIONAL_PROPS_RESOURCE_BASENAME, propertiesParseOptions );

	Config applicationSettings = applicationOrStandardSubstitute( cl );

	Config traditionalPropertiesConfigFile = ConfigFactory.parseFileAnySyntax( new File(TRADITIONAL_PROPS_FILENAME), propertiesParseOptions );
	
	Config sysPropOverrides = ConfigFactory.defaultOverrides();

	ACTIVE = sysPropOverrides
	    .withFallback( traditionalPropertiesConfigFile )
	    .withFallback( applicationSettings )
	    .withFallback( traditionalPropertiesConfigResource )
	    .withFallback( resourceDefaults )
	    .resolve();
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
