/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.datasource.mapdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MapDBFactoryImpl implements MapDBFactory {

    @Autowired
    SystemProperties config = SystemProperties.getDefault(); // initialized for standalone test

    @Override
    public DbSource<byte[]> createDataSource() {
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
        File dbFile = new File(config.databaseDir() + "/" + name);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        DBMaker.Maker dbMaker = DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown();
        if (!transactional) {
            dbMaker.transactionDisable();
        }
        return dbMaker.make();
    }
}
