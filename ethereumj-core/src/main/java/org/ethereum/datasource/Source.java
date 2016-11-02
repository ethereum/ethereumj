package org.ethereum.datasource;

import org.ethereum.datasource.Flushable;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public interface Source<K, V> {

    void put(K key, V val);

    V get(K key);

    void delete(K key);

    boolean flush();

}
