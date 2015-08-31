package org.ethereum.datasource;

import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.getProperty;
import static org.ethereum.config.SystemProperties.CONFIG;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class LevelDbDataSource implements KeyValueDataSource {

    private static final Logger logger = LoggerFactory.getLogger("db");

    String name;
    DB db;
    boolean alive;

    public LevelDbDataSource() {
    }

    public LevelDbDataSource(String name) {
        this.name = name;
    }

    @Override
    public void init() {
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

        try {
            logger.debug("Opening database");
            Path dbPath = Paths.get(getProperty("user.dir"), CONFIG.databaseDir(), name);
            Files.createDirectories(dbPath.getParent());

            logger.debug("Initializing new or existing database: '{}'", name);
            db = factory.open(dbPath.toFile(), options);

            alive = true;
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new RuntimeException("Can't initialize database");
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public void destroyDB(File fileLocation) {
        logger.debug("Destroying existing database");
        Options options = new Options();
        try {
            factory.destroy(fileLocation, options);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
        try {
            return db.get(key);
        } catch (DBException e) {
            return db.get(key);
        }
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        db.put(key, value);
        return value;
    }

    @Override
    public void delete(byte[] key) {
        db.delete(key);
    }

    @Override
    public Set<byte[]> keys() {
        try (DBIterator iterator = db.iterator()) {
            Set<byte[]> result = new HashSet<>();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                result.add(iterator.peekNext().getKey());
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws IOException {
        try (WriteBatch batch = db.createWriteBatch()) {
            for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
                batch.put(entry.getKey(), entry.getValue());
            }
            db.write(batch);
        }
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        try  {
            updateBatchInternal(rows);
        } catch (Exception e) {
            // try one more time
            try {
                updateBatchInternal(rows);
            } catch (Exception e1) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        if (!isAlive()) return;

        try {
            logger.debug("Close db: {}", name);
            db.close();

            alive = false;
        } catch (IOException e) {
            logger.error("Failed to find the db file on the close: {} ", name);
        }
    }
}
