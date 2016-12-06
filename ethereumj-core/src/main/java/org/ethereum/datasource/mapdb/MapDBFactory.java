package org.ethereum.datasource.mapdb;

import org.ethereum.datasource.DbSource;
import org.mapdb.DB;

public interface MapDBFactory {

    DbSource createDataSource();

    DB createDB(String name);

    DB createTransactionalDB(String name);
}
