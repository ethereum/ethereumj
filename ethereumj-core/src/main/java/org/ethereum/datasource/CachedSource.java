package org.ethereum.datasource;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 21.10.2016.
 */
public interface CachedSource<Key, Value> extends Source<Key, Value> {

    Source<Key, Value> getSource();

    Collection<Key> getModified();

    long estimateCashSize();

    interface BytesKey<Value> extends CachedSource<byte[], Value> {}
}
