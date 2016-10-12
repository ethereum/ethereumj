package org.ethereum.datasource.test;

import org.ethereum.datasource.Flushable;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public interface Source<K, V> extends Flushable {

    void put(K key, V val);

    V get(K key);

    void delete(K key);

    void flush();

}
