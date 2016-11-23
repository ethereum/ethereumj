package org.ethereum.datasource;

/**
 * Indicator interface which narrows the Source contract:
 * the same Key always maps to the same Value,
 * there could be no put() with the same Key and different Value
 * Normally the Key is the hash of the Value
 * Usually such kind of sources are the Trie backing stores
 *
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public interface HashedKeySource<Key, Value> extends Source<Key, Value> {
}
