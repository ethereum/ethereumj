package org.ethereum.datasource;

import java.util.Map;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public interface KeyValueDataSource extends DataSource, BatchSource<byte[], byte[]> {

    byte[] get(byte[] key);

    void put(byte[] key, byte[] value);

    void delete(byte[] key);

    Set<byte[]> keys();

    void updateBatch(Map<byte[], byte[]> rows);
}
