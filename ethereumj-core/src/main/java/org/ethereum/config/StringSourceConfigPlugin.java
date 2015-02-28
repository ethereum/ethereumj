package org.ethereum.config;

public abstract class StringSourceConfigPlugin extends ConfigPlugin {

    StringSourceConfigPlugin( ConfigPlugin fallback ) {
	super( fallback );
    }

    /**
     *
     * The only method concrete sublasses must implement.
     *
     */
    protected abstract String getLocalStringOrNull( String key );

    /*
     *
     *  Abstract method implementations
     *
     */
    protected Boolean getLocalBooleanOrNull( String key ) {
	String value = getLocalStringOrNull( key );
	return ( value != null ? Boolean.parseBoolean( value ) : null );
    }
    protected Integer getLocalIntegerOrNull( String key ) {
	String value = getLocalStringOrNull( key );
	return ( value != null ? Integer.parseInt( value ) : null );
    }
    protected String getLocalCoerceToStringOrNull( String key ) {
	return getLocalStringOrNull( key );
    }
}
