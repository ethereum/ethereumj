package org.ethereum.config;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;

import static org.ethereum.config.KeysDefaults.*;

public abstract class ConfigPlugin implements ConfigSource {

    protected final static Logger logger = KeysDefaults.getConfigPluginLogger();

    final static String[] parsePluginPath( String pluginPath ) {
	return pluginPath.split("\\s*,\\s*");
    }

    public final static ConfigPlugin fromPluginPath( String pluginPath ) {
	String[] classNames = parsePluginPath( pluginPath );
	ConfigPlugin head = ConfigPlugin.NULL;
	for ( int i = classNames.length; --i >= 0; ) {
	    String className = classNames[i];
	    try { 
		head = instantiatePlugin( className, head ); 
		logger.info( "Loaded config plugin '{}'.", className );
	    }
	    catch ( Exception e ) {
		if ( logger.isWarnEnabled() ) {
		    logger.warn("Could not instantiate a plugin of type '" + className + "'. Skipping...", e);
		}
	    }
	}
	return head;
    }

    public static String vmPluginPath() {
	String replacePath = System.getProperty(SYSPROP_PLUGIN_PATH_REPLACE);
	String appendPath  = System.getProperty(SYSPROP_PLUGIN_PATH_APPEND);
	String prependPath = System.getProperty(SYSPROP_PLUGIN_PATH_PREPEND);
	String path = ( replacePath == null ? DEFAULT_PLUGIN_PATH : replacePath ); // base path
	if ( appendPath != null ) path = path + "," + appendPath;
	if ( prependPath != null ) path = prependPath + "," + path;
	return path;
    }

    final static ConfigPlugin instantiatePlugin( String fqcn, ConfigPlugin fallback ) throws Exception {
	Class clz = Class.forName( fqcn );
	return instantiatePlugin( clz, fallback );
    }

    final static ConfigPlugin instantiatePlugin( Class clz, ConfigPlugin fallback ) throws Exception {
	Constructor<ConfigPlugin> ctor = clz.getConstructor( ConfigPlugin.class );
	return ctor.newInstance( fallback );
    }

    private final static ConfigPlugin NULL = new ConfigPlugin(null) {
	    protected Boolean getLocalBooleanOrNull( String key )        { return null; }
	    protected Integer getLocalIntegerOrNull( String key )        { return null; }
	    protected String  getLocalStringOrNull( String key )         { return null; }
	    protected String  getLocalCoerceToStringOrNull( String key ) { return null; }

	    @Override 
	    boolean matchesTail( String[] classNames, int index ) { return classNames.length == index; }
	    
	    @Override
	    StringBuilder appendPathTail( StringBuilder in, boolean first ) { return in; }
    };

    private ConfigPlugin fallback;

    protected ConfigPlugin( ConfigPlugin fallback ) {
	this.fallback = fallback;
    }

    boolean matchesTail( String[] classNames, int index ) {
	if ( index < classNames.length )
	    return this.getClass().getName().equals( classNames[index] ) && fallback.matchesTail( classNames, index + 1 );
	else
	    return false;
    }

    boolean matches( String[] classNames ) {
	return matchesTail( classNames, 0 );
    }

    StringBuilder appendPathTail( StringBuilder in, boolean first ) {
	if (! first) in.append(", ");
	in.append( this.getClass().getName() );
	return fallback.appendPathTail( in, false );
    }

    String path() {
	StringBuilder sb = new StringBuilder();
	return appendPathTail( sb, true ).toString();
    }

    /*
     *
     *  Abstract, protected methods
     *
     */
    /** May throw ClassCastExceptions */
    protected abstract Boolean getLocalBooleanOrNull( String key );
    
    /** May throw ClassCastExceptions */
    protected abstract Integer getLocalIntegerOrNull( String key );
    
    /** May throw ClassCastExceptions */
    protected abstract String getLocalStringOrNull( String key );
    
    /** May NOT throw ClassCastExceptions */
    protected abstract String getLocalCoerceToStringOrNull( String key );

    /** May throw ClassCastExceptions */
    public final Boolean getBooleanOrNull( String key ) {
	Boolean out = getLocalBooleanOrNull( key );
	return ( out == null ? fallback.getLocalBooleanOrNull( key ) : out );
    }
    
    /** May throw ClassCastExceptions */
    public final Integer getIntegerOrNull( String key ) {
	Integer out = getLocalIntegerOrNull( key );
	return ( out == null ? fallback.getLocalIntegerOrNull( key ) : out );
    }
    
    /** May throw ClassCastExceptions */
    public final String getStringOrNull( String key ) {
	String out = getLocalStringOrNull( key );
	return ( out == null ? fallback.getLocalStringOrNull( key ) : out );
    }
    
    /** May NOT throw ClassCastExceptions */
    public final String getCoerceToStringOrNull( String key ) {
	String out = getLocalCoerceToStringOrNull( key );
	return ( out == null ? fallback.getLocalCoerceToStringOrNull( key ) : out );
    }
}
