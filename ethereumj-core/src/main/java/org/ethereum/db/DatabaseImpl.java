package org.ethereum.db;

import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private KeyValueDataSource keyValueDataSource;

    public DatabaseImpl(KeyValueDataSource keyValueDataSource) {
        this.keyValueDataSource = keyValueDataSource;
    }


    public DatabaseImpl(String name) {

        keyValueDataSource.setName(name);
        keyValueDataSource.init();
    }


    @Override
    public byte[] get(byte[] key) {
        return keyValueDataSource.get(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {

        if (logger.isDebugEnabled())
            logger.debug("put: key: [{}], value: [{}]",
                    Hex.toHexString(key),
                    Hex.toHexString(value));
        keyValueDataSource.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        if (logger.isDebugEnabled())
            logger.debug("delete: key: [{}]");

        keyValueDataSource.delete(key);
    }

    public KeyValueDataSource getDb() {
        return this.keyValueDataSource;
    }

    @Override
    public void init(){
        keyValueDataSource.init();
    }

    @Override
    public void close() {

        keyValueDataSource.close();
    }

    public List<ByteArrayWrapper> dumpKeys() {

        ArrayList<ByteArrayWrapper> keys = new ArrayList<>();

        for (byte[] key : keyValueDataSource.keys()) {
            keys.add(ByteUtil.wrap(key));
        }
        Collections.sort(keys);
        return keys;
    }
}