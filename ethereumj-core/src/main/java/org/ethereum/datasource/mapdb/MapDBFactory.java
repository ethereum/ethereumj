package org.ethereum.datasource.mapdb;

import org.ethereum.datasource.KeyValueDataSource;

public interface MapDBFactory {

    KeyValueDataSource createDataSource();
}
