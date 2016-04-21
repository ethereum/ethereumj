package org.ethereum.datasource;

import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.slf4j.LoggerFactory.getLogger;

public class DataSourcePool {

    private static final Logger logger = getLogger("db");
    private static ConcurrentMap<String, KeyValueDataSource> pool = new ConcurrentHashMap<>();

    public interface DataSourceMaker {
        KeyValueDataSource makeDataSource();
    }

    private static final DataSourceMaker defaultDataSourceMaker = new DataSourceMaker() {
        @Override
        public KeyValueDataSource makeDataSource() {
            return new LevelDbDataSource(SystemProperties.getDefault());
        }
    };

    // TODO: remove this global state
    private static DataSourceMaker dataSourceMaker = defaultDataSourceMaker;

    public static void setDataSourceMaker(DataSourceMaker dataSourceMaker) {
        DataSourcePool.dataSourceMaker = dataSourceMaker;
    }
    public static void setDefaultDataSourceMaker() {
        DataSourcePool.dataSourceMaker = defaultDataSourceMaker;
    }

    public static KeyValueDataSource getDataSourceFromPool(String name) {
        KeyValueDataSource dataSource = dataSourceMaker.makeDataSource();
        dataSource.setName(name);
        KeyValueDataSource result = pool.putIfAbsent(name, dataSource);
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

        KeyValueDataSource dataSource = pool.remove(name);
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
