package org.ethereum.datasource.mapdb;

import org.ethereum.datasource.KeyValueDataSource;
import org.mapdb.DB;

import java.util.Map;

public interface MapDBFactory {

    KeyValueDataSource createDataSource();

    Map<Long, byte[]> createHashStoreMap();

    void destroy(Object resource);

    DB createDB(String name);
}
