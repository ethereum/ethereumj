package org.ethereum.datasource.bangdb;

import bangdb.*;
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
 * NOTE: set -Dlibrary.path= to the dir with bangdbjava.dll & bangdbwin.dll
 *
 * Created by Anton Nashatyrev on 01.02.2017.
 */
public class BangdbDataSource implements DbSource<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger("db");

    static {
        System.loadLibrary("bangdbwin");
        System.loadLibrary("bangdbjava");
    }

    @Autowired
    SystemProperties config  = SystemProperties.getDefault(); // initialized for standalone test

    String name;
    boolean alive;

    static Database db;
    Table tbl;
    Connection conn;


    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    public BangdbDataSource() {
    }

    public BangdbDataSource(String name) {
        this.name = name;
        logger.debug("New LevelDbDataSource: " + name);
    }

    @Override
    public void init() {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> BangdbDataSource.init(): " + name);

            logger.debug("Opening database");
            final Path dbPath = getPath();

            if (!Files.isSymbolicLink(dbPath.getParent())) try {
                Files.createDirectories(dbPath.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (name == null) throw new NullPointerException("no name set to the db");

            if (db == null) {
                db = new DatabaseImpl("ethereumj", null, TransactionType.DB_OPTIMISTIC_TRANSACTION, dbPath.getParent().toString(), dbPath.getParent().toString());
            }
            tbl = db.getTable(name, DBAccess.OPENCREATE, null);
            conn = tbl.getConnection();

            alive = true;
            logger.debug("<~ BangdbDataSource.init(): " + name);
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
                byte[] ret = conn.get(key);
                if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
                return ret;
            } catch (DBException e) {
                throw new RuntimeException(e);
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
            conn.put(key, value, InsertOptions.INSERT_UPDATE);
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
            conn.del(key);
            if (logger.isTraceEnabled()) logger.trace("<~ LevelDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() {
        throw new RuntimeException("Not supported");
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws IOException {
        Transaction tx = new Transaction();
        db.beginTransaction(tx);
        try {
            for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
                if (entry.getValue() == null) {
                    conn.del(entry.getKey(), tx);
                } else {
                    conn.put(entry.getKey(), entry.getValue(), InsertOptions.INSERT_UPDATE, tx);
                }
            }
            db.commitTransaction(tx);
        } catch (Throwable t) {
            db.abortTransaction(tx);
            throw new RuntimeException(t);
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

            conn.closeConnection();
            db.closeTable(tbl, DBClose.CONSERVATIVE);

        } finally {
            resetDbLock.writeLock().unlock();
        }
    }
}
