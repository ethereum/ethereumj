package org.ethereum.datasource;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.slf4j.LoggerFactory.getLogger;

public class DataSourcePool {

    private static final Logger logger = getLogger("db");
    private static ConcurrentMap<String, DataSource> pool = new ConcurrentHashMap<>();

    public static KeyValueDataSource hashMapDBByName(String name){
        return (KeyValueDataSource) getDataSourceFromPool(name, new HashMapDB());
    }

    public static KeyValueDataSource levelDbByName(String name) {
        return (KeyValueDataSource) getDataSourceFromPool(name, new LevelDbDataSource());
    }

    private static DataSource getDataSourceFromPool(String name, @Nonnull DataSource dataSource) {
        dataSource.setName(name);
        DataSource result = pool.putIfAbsent(name, dataSource);
        if (result == null) {
            result = dataSource;
            logger.debug("Data source '{}' created and added to pool.", name);
        } else {
            logger.debug("Data source '{}' returned from pool.", name);
        }

        synchronized (result) {
            if (!result.isAlive()) result.init();
        }

        return result;
    }

    public static void closeDataSource(String name){

        DataSource dataSource = pool.remove(name);
        if (dataSource != null){
            synchronized (dataSource) {

                if (dataSource instanceof HashMapDB)
                    pool.put(name, dataSource);
                else
                    dataSource.close();

                logger.debug("Data source '%s' closed and removed from pool.\n", dataSource.getName());
            }
        }
    }
}
