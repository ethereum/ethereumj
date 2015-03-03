package org.ethereum.config;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import static org.ethereum.config.KeysDefaults.*;

class CachingConfigSource implements ConfigSource {

    private final static Object NULL_TOKEN = new Object();

    ConfigSource inner;
    Map<CacheKey,Object> cache;

    CachingConfigSource( ConfigSource inner ) {
	this.inner = inner;
	Map<CacheKey,Object> map = new HashMap<>();
	for ( String key : Keys.all() ) {
	    Class<?> type = TYPES.get( key );
	    Object value = inner.getOrNull( key, type );
	    map.put( new CacheKey( key, type), ( value == null ? NULL_TOKEN : value ) );
	}
	this.cache = Collections.unmodifiableMap( map );
    }

    public Object getOrNull( String key, Class<?> expectedType ) {
	Object out = cache.get( new CacheKey( key, expectedType ) );
	if ( out == null /* not NULL_TOKEN! */ ) out = inner.getOrNull( key, expectedType );
	return ( out == NULL_TOKEN ? null : out );
    }

    final static class CacheKey {
	private String key;
	private Class<?> expectedType;

	CacheKey( String key, Class<?> expectedType ) {
	    this.key = key;
	    this.expectedType = expectedType;
	}

	public boolean equals( Object o ) {
	    if ( o == null || ! (o instanceof CacheKey) ) {
		return false;
	    } else {
		CacheKey oo = (CacheKey) o;
		return this.key.equals( oo.key ) && this.expectedType == oo.expectedType;
	    }
	}
	public int hashCode() { return key.hashCode() ^ expectedType.hashCode(); }
    }
}
