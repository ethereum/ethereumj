package org.ethereum.datasource;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 21.10.2016.
 */
public interface CachedSource<Key, Value> extends Source<Key, Value> {

    Source<Key, Value> getSrc();

    Collection<Key> getModified();

    Map<Key, Value> getCache();

    interface BytesKey<Value> extends CachedSource<byte[], Value> {}
}
