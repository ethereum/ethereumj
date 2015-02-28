package org.ethereum.config;

interface ConfigSource {
    /** May throw ClassCastExceptions */
    public Boolean getBooleanOrNull( String key );
    
    /** May throw ClassCastExceptions */
    public Integer getIntegerOrNull( String key );
    
    /** May throw ClassCastExceptions */
    public String getStringOrNull( String key );
    
    /** May NOT throw ClassCastExceptions */
    public String getCoerceToStringOrNull( String key );
}
