package org.ethereum.config;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;

import static org.ethereum.config.KeysDefaults.*;


/**
 * To write a config plugin, override the {@link #getLocalOrNull},
 * and provide a public constructor that accepts a fallback ConfigPlugin
 * for its sole argument.
 *
 * Objects provided should be put into the types expected for the key.
 *
 * @see KeysDefaults#expectedTypes() and {@link #attemptCoerceValueForKey}
 */
public abstract class ConfigPlugin implements ConfigSource {

    protected final static Logger logger = KeysDefaults.getConfigPluginLogger();

    protected final static ConfigPlugin fromPluginPath( String pluginPath ) {
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
    protected static String vmPluginPath() {
	String replacePath = System.getProperty(SYSPROP_PLUGIN_PATH_REPLACE);
	String appendPath  = System.getProperty(SYSPROP_PLUGIN_PATH_APPEND);
	String prependPath = System.getProperty(SYSPROP_PLUGIN_PATH_PREPEND);
	String path = ( replacePath == null ? DEFAULT_PLUGIN_PATH : replacePath ); // base path
	if ( appendPath != null ) path = path + "," + appendPath;
	if ( prependPath != null ) path = prependPath + "," + path;
	return path;
    }

    final static String[] parsePluginPath( String pluginPath ) {
	return pluginPath.split("\\s*,\\s*");
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
	    protected Object getLocalOrNull( String key ) { return null; }

	    @Override 
	    boolean matchesTail( String[] classNames, int index ) { return classNames.length == index; }
	    
	    @Override
	    StringBuilder appendPathTail( StringBuilder in, boolean first ) { return in; }
    };

    private ConfigPlugin fallback;

    protected ConfigPlugin( ConfigPlugin fallback ) {
	this.fallback = fallback;
    }

    protected Object attemptCoerceValueForKey( String value, String key ) {
	Class<?> type = TYPES.get( key );
	Object out;
	if ( type == Boolean.class ) {
	    out = Boolean.valueOf( value );
	} else if ( type == Integer.class ) {
	    out = Integer.valueOf( value );
	} else if ( type == String.class ) {
	    out = value;
	} else {
	    if ( logger.isWarnEnabled() ) {
		logger.warn("Cannot coerce String to {} for key '{}'. Left as String. [{}]", type, key, this.getClass().getSimpleName());
	    }
	    out = value;
	}
	return out;
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
    protected abstract Object getLocalOrNull( String key );
    
    public final Object getOrNull( String key ) {
	Object out = getLocalOrNull( key );
	return ( out == null ? fallback.getLocalOrNull( key ) : out );
    }
}
