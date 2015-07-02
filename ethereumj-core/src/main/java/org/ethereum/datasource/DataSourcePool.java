package org.ethereum.datasource;

import org.ethereum.datasource.mapdb.MapDBDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourcePool {
    
    private static Map<String, KeyValueDataSource> pool = new ConcurrentHashMap<>();

    public static KeyValueDataSource levelDbByName(String name) {
        KeyValueDataSource dataSource = pool.get(name);
        if (dataSource == null) {
            dataSource = new LevelDbDataSource(name);
            dataSource.init();
            
            pool.put(name, dataSource);
        }
        
        return dataSource;
    }

    public static KeyValueDataSource mapDbByName(String name) {
        KeyValueDataSource dataSource = pool.get(name);
        if (dataSource == null) {
            dataSource = new MapDBDataSource();
            dataSource.setName(name);
            dataSource.init();
            
            pool.put(name, dataSource);
        }
        
        return dataSource;
    }

}
