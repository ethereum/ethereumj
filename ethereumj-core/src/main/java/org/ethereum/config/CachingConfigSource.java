package org.ethereum.config;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import static org.ethereum.config.KeysDefaults.*;

class CachingConfigSource implements ConfigSource {

    private final static Object NULL_TOKEN = new Object();

    ConfigSource inner;
    Map<String,Object> cache;

    CachingConfigSource( ConfigSource inner ) {
	this.inner = inner;
	Map<String,Object> map = new HashMap<>();
	for ( String key : Keys.all() ) {
	    Object value = inner.getOrNull( key );
	    map.put( key, ( value == null ? NULL_TOKEN : value ) );
	}
	this.cache = Collections.unmodifiableMap( map );
    }

    public Object getOrNull( String key ) {
	Object out = cache.get( key );
	return ( out == NULL_TOKEN ? null : out );
    }

}
