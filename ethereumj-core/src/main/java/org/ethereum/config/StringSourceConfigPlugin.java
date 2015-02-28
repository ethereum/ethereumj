package org.ethereum.config;

public abstract class StringSourceConfigPlugin extends ConfigPlugin {

    /**
     *
     * The only method concrete sublasses must implement.
     *
     */
    protected abstract String getStringOrNull( String key );

    /*
     *
     *  Abstract method implementations
     *
     */
    protected Boolean getBooleanOrNull( String key ) {
	String value = getStringOrNull( key );
	return ( value != null ? Boolean.parseBoolean( value ) : null );
    }
    protected Integer getIntegerOrNull( String key ) {
	String value = getStringOrNull( key );
	return ( value != null ? Integer.parseInt( value ) : null );
    }
    protected String getCoerceToStringOrNull( String key ) {
	return getStringOrNull( key );
    }
}
