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
package org.ethereum.datasource.leveldb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSource;
import org.ethereum.util.FileUtil;
import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class LevelDbDataSource implements DbSource<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger("db");

    @Autowired
    SystemProperties config  = SystemProperties.getDefault(); // initialized for standalone test

    String name;
    DB db;
    boolean alive;

    // The native LevelDB insert/update/delete are normally thread-safe
    // However close operation is not thread-safe and may lead to a native crash when
    // accessing a closed DB.
    // The leveldbJNI lib has a protection over accessing closed DB but it is not synchronized
    // This ReadWriteLock still permits concurrent execution of insert/delete/update operations
    // however blocks them on init/close/delete operations
    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    public LevelDbDataSource() {
    }

    public LevelDbDataSource(String name) {
        this.name = name;
        logger.debug("New LevelDbDataSource: " + name);
    }

    @Override
    public void init() {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> LevelDbDataSource.init(): " + name);

            if (isAlive()) return;

            if (name == null) throw new NullPointerException("no name set to the db");

            Options options = new Options();
            options.createIfMissing(true);
            options.compressionType(CompressionType.NONE);
            options.blockSize(10 * 1024 * 1024);
            options.writeBufferSize(10 * 1024 * 1024);
            options.cacheSize(0);
            options.paranoidChecks(true);
            options.verifyChecksums(true);
            options.maxOpenFiles(32);

            try {
                logger.debug("Opening database");
                final Path dbPath = getPath();
                if (!Files.isSymbolicLink(dbPath.getParent())) Files.createDirectories(dbPath.getParent());

                logger.debug("Initializing new or existing database: '{}'", name);
                try {
                    db = factory.open(dbPath.toFile(), options);
                } catch (IOException e) {
                    // database could be corrupted
                    // exception in std out may look:
                    // org.fusesource.leveldbjni.internal.NativeDB$DBException: Corruption: 16 missing files; e.g.: /Users/stan/ethereumj/database-test/block/000026.ldb
                    // org.fusesource.leveldbjni.internal.NativeDB$DBException: Corruption: checksum mismatch
                    if (e.getMessage().contains("Corruption:")) {
                        logger.warn("Problem initializing database.", e);
                        logger.info("LevelDB database must be corrupted. Trying to repair. Could take some time.");
                        factory.repair(dbPath.toFile(), options);
                        logger.info("Repair finished. Opening database again.");
                        db = factory.open(dbPath.toFile(), options);
                    } else {
                        // must be db lock
                        // org.fusesource.leveldbjni.internal.NativeDB$DBException: IO error: lock /Users/stan/ethereumj/database-test/state/LOCK: Resource temporarily unavailable
                        throw e;
                    }
                }

                alive = true;
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                throw new RuntimeException("Can't initialize database", ioe);
            }
            logger.debug("<~ LevelDbDataSource.init(): " + name);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    private Path getPath() {
        return Paths.get(config.databaseDir(), name);
    }

    public void reset() {
        close();
        FileUtil.recursiveDelete(getPath().toString());
        init();
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public void destroyDB(File fileLocation) {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("Destroying existing database: " + fileLocation);
            Options options = new Options();
            try {
                factory.destroy(fileLocation, options);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
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
    public byte[] get(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key));
            try {
                byte[] ret = db.get(key);
                if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                return ret;
            } catch (DBException e) {
                logger.warn("Exception. Retrying again...", e);
                byte[] ret = db.get(key);
                if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                return ret;
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] value) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> LevelDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (value == null ? "null" : value.length));
            db.put(key, value);
            if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (value == null ? "null" : value.length));
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> LevelDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
            db.delete(key);
            if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> LevelDbDataSource.keys(): " + name);
            try (DBIterator iterator = db.iterator()) {
                Set<byte[]> result = new HashSet<>();
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    result.add(iterator.peekNext().getKey());
                }
                if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.keys(): " + name + ", " + result.size());
                return result;
            } catch (IOException e) {
                logger.error("Unexpected", e);
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws IOException {
        try (WriteBatch batch = db.createWriteBatch()) {
            for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
                if (entry.getValue() == null) {
                    batch.delete(entry.getKey());
                } else {
                    batch.put(entry.getKey(), entry.getValue());
                }
            }
            db.write(batch);
        }
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) logger.trace("~> LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
            try {
                updateBatchInternal(rows);
                if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
            } catch (Exception e) {
                logger.error("Error, retrying one more time...", e);
                // try one more time
                try {
                    updateBatchInternal(rows);
                    if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
                } catch (Exception e1) {
                    logger.error("Error", e);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean flush() {
        return false;
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) return;

            try {
                logger.debug("Close db: {}", name);
                db.close();

                alive = false;
            } catch (IOException e) {
                logger.error("Failed to find the db file on the close: {} ", name);
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }
}
