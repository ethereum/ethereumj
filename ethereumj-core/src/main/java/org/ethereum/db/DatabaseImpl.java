package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.RedisDataSource;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * Generic interface for Ethereum database
 *
 * LevelDB key/value pair DB implementation will be used.
 * Choice must be made between:
 * Pure Java: https://github.com/dain/leveldb
 * JNI binding: https://github.com/fusesource/leveldbjni
 */
public class DatabaseImpl implements Database {

    private static final Logger logger = LoggerFactory.getLogger("db");
    private String name;
    private KeyValueDataSource dataSource;

    public DatabaseImpl(String name) {

        if (CONFIG.getKeyValueDataSource().equals("redis") ){
            dataSource  = new RedisDataSource();
            dataSource.setName(name);
            dataSource.init();
            return;
        }

        if (CONFIG.getKeyValueDataSource().equals("leveldb") ){
            dataSource  = new LevelDbDataSource();
            dataSource.setName(name);
            dataSource.init();
            return;
        }

        logger.info("Key/Value datasource was not configured.");
        System.exit(-1);
    }


    @Override
    public byte[] get(byte[] key) {
        return dataSource.get(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {

        if (logger.isDebugEnabled())
            logger.debug("put: key: [{}], value: [{}]",
                    Hex.toHexString(key),
                    Hex.toHexString(value));
        dataSource.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        if (logger.isDebugEnabled())
            logger.debug("delete: key: [{}]");

        dataSource.delete(key);
    }

    public KeyValueDataSource getDb() {
        return this.dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }

    public List<ByteArrayWrapper> dumpKeys() {

        ArrayList<ByteArrayWrapper> keys = new ArrayList<>();

        for (byte[] key : dataSource.keys()) {
            keys.add(ByteUtil.wrap(key));
        }
        Collections.sort(keys);
        return keys;
    }
}