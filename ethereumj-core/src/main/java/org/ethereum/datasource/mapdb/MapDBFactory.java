package org.ethereum.datasource.mapdb;

import org.ethereum.datasource.KeyValueDataSource;
import org.mapdb.DB;

public interface MapDBFactory {

    KeyValueDataSource createDataSource();

    DB createDB(String name);

    DB createTransactionalDB(String name);
}
