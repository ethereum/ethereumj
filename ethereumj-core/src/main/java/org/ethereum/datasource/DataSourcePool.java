package org.ethereum.datasource;

import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.slf4j.LoggerFactory.getLogger;

public class DataSourcePool {

    private static Logger logger = getLogger("db");
    private static ConcurrentMap<String, DataSource> pool = new ConcurrentHashMap<>();

    public static KeyValueDataSource levelDbByName(String name) {
        return (KeyValueDataSource) getDataSourceFromPool(name, new LevelDbDataSource(name));
    }

    private static DataSource getDataSourceFromPool(String name, DataSource dataSource) {
        DataSource result = pool.putIfAbsent(name, dataSource);
        if (result == null) {
            synchronized (dataSource) {
                dataSource.init();
                result = dataSource;
            }
            logger.info("Data source '{}' created and added to pool.", dataSource.getName());
        } else {
            logger.info("Data source '{}' returned from pool.", dataSource.getName());
        }
        
        return result;
    }

    public static void closeDataSource(String name){
        DataSource dataSource = pool.remove(name);
        if (dataSource != null){
            synchronized (dataSource) {
                dataSource.close();
                logger.info("Data source '{}' closed and removed from pool.", dataSource.getName());
            }
        }
    }
}
