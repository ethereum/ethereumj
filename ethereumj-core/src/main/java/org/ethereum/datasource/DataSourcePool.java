package org.ethereum.datasource;

import org.ethereum.config.CommonConfig;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DataSourcePool {

    private static final Logger logger = getLogger("db");
    private ConcurrentMap<String, DataSource> pool = new ConcurrentHashMap<>();

    public KeyValueDataSource hashMapDBByName(String name){
        return (KeyValueDataSource) getDataSourceFromPool(name, new HashMapDB());
    }

    public KeyValueDataSource dbByName(CommonConfig commonConfig, String name) {
        return (KeyValueDataSource) getDataSourceFromPool(name, commonConfig.keyValueDataSource());
    }

    private DataSource getDataSourceFromPool(String name, @Nonnull DataSource dataSource) {
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

    public void closeDataSource(String name){

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
