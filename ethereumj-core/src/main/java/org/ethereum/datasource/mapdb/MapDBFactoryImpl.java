package org.ethereum.datasource.mapdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.KeyValueDataSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.stereotype.Component;

import java.io.File;

import static java.lang.System.getProperty;

@Component
public class MapDBFactoryImpl implements MapDBFactory {

    @Override
    public KeyValueDataSource createDataSource() {
        return new MapDBDataSource();
    }

    @Override
    public DB createDB(String name) {
        return createDB(name, false);
    }

    @Override
    public DB createTransactionalDB(String name) {
        return createDB(name, true);
    }

    private DB createDB(String name, boolean transactional) {
        File dbFile = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/" + name);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        DBMaker.Maker dbMaker = DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown();
        if (!transactional) {
            dbMaker.transactionDisable();
        }
        return dbMaker.make();
    }
}
