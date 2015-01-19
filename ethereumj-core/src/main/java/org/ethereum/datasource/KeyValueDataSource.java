package org.ethereum.datasource;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 18/01/2015 21:40
 */

public interface KeyValueDataSource {

    public void init();
    public void setName(String name);
    
    public byte[] get(byte[] key);
    public void put(byte[] key, byte[] value);
    public void delete(byte[] key);
    public Set<byte[]> keys();
    public void updateBatch( Map<byte[], byte[]> rows);

    public void close();
}
