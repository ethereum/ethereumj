package org.ethereum.config;

import java.util.Map;
import java.util.HashMap;

class CachingConfigSource implements ConfigSource {

    private final static Object NULL_TOKEN = new Object();

    ConfigSource inner;
    Map<String,Object> cache = new HashMap();

    CachingConfigSource( ConfigSource inner ) {
	this.inner = inner;
    }

    /** May throw ClassCastExceptions */
    public Boolean getBooleanOrNull( String key ) {
	Object out = cache.get( key );
	if ( out == null ) {
	    out = inner.getBooleanOrNull( key );
	    cache.put( key, out == null ? NULL_TOKEN : out );
	}
	return (Boolean) ( out == NULL_TOKEN ? null : out );
    }
    
    /** May throw ClassCastExceptions */
    public Integer getIntegerOrNull( String key ) {
	Object out = cache.get( key );
	if ( out == null ) {
	    out = inner.getIntegerOrNull( key );
	    cache.put( key, out == null ? NULL_TOKEN : out );
	}
	return (Integer) ( out == NULL_TOKEN ? null : out );
    }
    
    /** May throw ClassCastExceptions */
    public String getStringOrNull( String key ) {
	Object out = cache.get( key );
	if ( out == null ) {
	    out = inner.getStringOrNull( key );
	    cache.put( key, out == null ? NULL_TOKEN : out );
	}
	return (String) ( out == NULL_TOKEN ? null : out );
    }
    
    /** May NOT throw ClassCastExceptions */

    // does not populate cache, but uses previously cached values if available
    public String getCoerceToStringOrNull( String key ) {
	Object out = cache.get( key );
	return ( out == null ? inner.getCoerceToStringOrNull( key ) : String.valueOf( out ) );
    }
}
