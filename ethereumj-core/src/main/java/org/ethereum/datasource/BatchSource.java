package org.ethereum.datasource;

import java.util.Map;

/**
 * Created by Anton Nashatyrev on 01.11.2016.
 */
public interface BatchSource<K, V> extends Source<K, V> {

    void updateBatch(Map<K, V> rows);
}
