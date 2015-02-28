package org.ethereum.config;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import static org.ethereum.config.KeysDefaults.*;

public final class PathFollowingConfigPlugin extends ConfigPlugin {

    private final static List<ConfigPlugin> INSTANCE_PATH;

    static List<ConfigPlugin> createInstancePath( String classnamePath ) {
	String[] classNames = classnamePath.split("\\s*,\\s*");
	List<ConfigPlugin> tmp = new LinkedList<>();
	for ( String fqcn : classNames ) {
	    try {
		if ( "".equals( fqcn ) ) continue; //don't bother trying to load a class whose name is the empty String

		ConfigPlugin instance = (ConfigPlugin) Class.forName( fqcn ).newInstance();
		tmp.add( instance );
		logger.info( "Loaded config plugin '{}'.", fqcn );
	    } catch (Exception e ) {
		logger.warn("Could not create an instance of config plug-in '" + fqcn + "'.", e);
	    }
	}
	return Collections.unmodifiableList( tmp );
    }

    private static String lookupPathKey( String sysprop ) {
	String out = System.getProperty( ETHEREUMJ_PREFIX + sysprop );
	if ( out == null ) out = System.getProperty( sysprop );
	return out;
    }

    static String configPluginPath() {
	String replacePath = lookupPathKey(SYSPROP_PLUGIN_PATH_REPLACE);
	String appendPath  = lookupPathKey(SYSPROP_PLUGIN_PATH_APPEND);
	String prependPath = lookupPathKey(SYSPROP_PLUGIN_PATH_PREPEND);

	String path = ( replacePath == null ? DEFAULT_PLUGIN_PATH : replacePath ); // base path
	if ( appendPath != null ) path = path + "," + appendPath;
	if ( prependPath != null ) path = prependPath + "," + path;
	return path;
    }

    static {
	String pluginPath = configPluginPath();
	//logger.info("config plugin path: {}", pluginPath);
	if (! DEFAULT_PLUGIN_PATH.equals( pluginPath ) ) logger.info("Using configuration plugins, path: {}", pluginPath);
	INSTANCE_PATH = createInstancePath( pluginPath );
    }

    /*
     *
     *  Abstract, protected methods
     *
     */
    protected Boolean getBooleanOrNull( String key ) {
	for( ConfigPlugin instance : INSTANCE_PATH ) {
	    Boolean maybe = instance.getBooleanOrNull( key );
	    if ( maybe != null ) return maybe;
	}
	return null;
    }
    protected Integer getIntegerOrNull( String key ) {
	for( ConfigPlugin instance : INSTANCE_PATH ) {
	    Integer maybe = instance.getIntegerOrNull( key );
	    if ( maybe != null ) return maybe;
	}
	return null;
    }
    protected String  getStringOrNull( String key ) {
	for( ConfigPlugin instance : INSTANCE_PATH ) {
	    String maybe = instance.getStringOrNull( key );
	    if ( maybe != null ) return maybe;
	}
	return null;
    }
    protected String  getCoerceToStringOrNull( String key ) {
	for( ConfigPlugin instance : INSTANCE_PATH ) {
	    String maybe = instance.getCoerceToStringOrNull( key );
	    if ( maybe != null ) return maybe;
	}
	return null;
    }
}
