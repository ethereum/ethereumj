package org.ethereum.datasource.test;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 21.10.2016.
 */
public interface CachedSource<Key, Value, SourceKey, SourceValue> extends Source<Key, Value> {

    Source<SourceKey, SourceValue> getSrc();

    Collection<Key> getModified();

    Map<Key, Value> getCache();

    interface BytesKey<Value, SourceValue> extends CachedSource<byte[], Value, byte[], SourceValue> {}

    interface Identity<Key, Value> extends CachedSource<Key, Value, Key, Value> {}

    interface SimpleBytesKey<Value> extends BytesKey<Value, Value>, Identity<byte[], Value> {}
}
