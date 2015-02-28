package org.ethereum.config;

import org.slf4j.Logger;

public abstract class ConfigPlugin {

    protected final static Logger logger = KeysDefaults.getConfigPluginLogger();

    /*
     *
     *  Abstract, protected methods
     *
     */
    
    /** May throw ClassCastExceptions */
    protected abstract Boolean getBooleanOrNull( String key );
    
    /** May throw ClassCastExceptions */
    protected abstract Integer getIntegerOrNull( String key );
    
    /** May throw ClassCastExceptions */
    
    protected abstract String getStringOrNull( String key );
    
    /** May NOT throw ClassCastExceptions */
    protected abstract String getCoerceToStringOrNull( String key );
}
