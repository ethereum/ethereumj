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
package org.ethereum.datasource.rocksdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSource;
import org.ethereum.util.FileUtil;
import org.rocksdb.*;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Mikhail Kalinin
 * @since 28.11.2017
 */
public class RocksDbDataSource implements DbSource<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger("db");

    @Autowired
    SystemProperties config  = SystemProperties.getDefault(); // initialized for standalone test

    String name;
    RocksDB db;
    boolean alive;

    // The native RocksDB insert/update/delete are normally thread-safe
    // However close operation is not thread-safe.
    // This ReadWriteLock still permits concurrent execution of insert/delete/update operations
    // however blocks them on init/close/delete operations
    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    static {
        RocksDB.loadLibrary();
    }

    public RocksDbDataSource() {
    }

    public RocksDbDataSource(String name) {
        this.name = name;
        logger.debug("New RocksDbDataSource: " + name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init() {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> RocksDbDataSource.init(): " + name);

            if (isAlive()) return;

            if (name == null) throw new NullPointerException("no name set to the db");

            try (Options options = new Options()) {

                // most of these options are suggested by https://github.com/facebook/rocksdb/wiki/Set-Up-Options

                // general options
                options.setCreateIfMissing(true);
                options.setCompressionType(CompressionType.LZ4_COMPRESSION);
                options.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
                options.setLevelCompactionDynamicLevelBytes(true);
                options.setMaxBackgroundCompactions(4);
                options.setMaxBackgroundFlushes(2);
                options.setMaxOpenFiles(32);

                // table options
                final BlockBasedTableConfig tableCfg;
                options.setTableFormatConfig(tableCfg = new BlockBasedTableConfig());
                tableCfg.setBlockSize(16 * 1024);
                tableCfg.setCacheIndexAndFilterBlocks(true);
                tableCfg.setPinL0FilterAndIndexBlocksInCache(true);
                tableCfg.setFilter(new BloomFilter(10, false));

                try {
                    logger.debug("Opening database");
                    final Path dbPath = getPath();
                    if (!Files.isSymbolicLink(dbPath.getParent())) Files.createDirectories(dbPath.getParent());

                    logger.debug("Initializing new or existing database: '{}'", name);
                    try {
                        db = RocksDB.open(options, dbPath.toString());
                    } catch (RocksDBException e) {
                        logger.error(e.getMessage(), e);
                        throw new RuntimeException("Failed to initialize database", e);
                    }

                    alive = true;

                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);
                    throw new RuntimeException("Failed to initialize database", ioe);
                }

                logger.debug("<~ RocksDbDataSource.init(): " + name);
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) return;

            logger.debug("Close db: {}", name);
            db.close();

            alive = false;

        } catch (Exception e) {
            logger.error("Error closing db '{}'", name, e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() throws RuntimeException {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> RocksDbDataSource.keys(): " + name);
            try (RocksIterator iterator = db.newIterator()) {
                Set<byte[]> result = new HashSet<>();
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    result.add(iterator.key());
                }
                if (logger.isTraceEnabled()) logger.trace("<~ RocksDbDataSource.keys(): " + name + ", " + result.size());
                return result;
            } catch (Exception e) {
                logger.error("Error iterating db '{}'", name, e);
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void reset() {
        close();
        FileUtil.recursiveDelete(getPath().toString());
        init();
    }

    private Path getPath() {
        return Paths.get(config.databaseDir(), name);
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
            try {

                try (WriteBatch batch = new WriteBatch();
                     WriteOptions writeOptions = new WriteOptions()) {
                    for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
                        if (entry.getValue() == null) {
                            batch.remove(entry.getKey());
                        } else {
                            batch.put(entry.getKey(), entry.getValue());
                        }
                    }
                    db.write(writeOptions, batch);
                }

                if (logger.isTraceEnabled()) logger.trace("<~ RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
            } catch (RocksDBException e) {
                logger.error("Error in batch update on db '{}'", name, e);
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] val) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> RocksDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (val == null ? "null" : val.length));
            if (val != null) {
                db.put(key, val);
            } else {
                db.delete(key);
            }
            if (logger.isTraceEnabled()) logger.trace("<~ RocksDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (val == null ? "null" : val.length));
        } catch (RocksDBException e) {
            logger.error("Failed to put into db '{}'", name, e);
            throw new RuntimeException(e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] get(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> RocksDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key));
            byte[] ret = db.get(key);
            if (logger.isTraceEnabled()) logger.trace("<~ RocksDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
            return ret;
        } catch (RocksDBException e) {
            logger.error("Failed to get from db '{}'", name, e);
            throw new RuntimeException(e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> RocksDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
            db.delete(key);
            if (logger.isTraceEnabled()) logger.trace("<~ RocksDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
        } catch (RocksDBException e) {
            logger.error("Failed to delete from db '{}'", name, e);
            throw new RuntimeException(e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean flush() {
        return false;
    }
}
