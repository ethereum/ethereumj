package org.ethereum.datasource.mapdb;

import org.ethereum.datasource.KeyValueDataSource;
import org.springframework.stereotype.Component;

@Component
public class MapDBFactoryImpl implements MapDBFactory {

    @Override
    public KeyValueDataSource createDataSource() {
        return new MapDBDataSource();
    }
}
