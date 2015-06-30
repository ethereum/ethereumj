package org.ethereum.datasource.mapdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.KeyValueDataSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static java.lang.System.getProperty;

public class MapDBDataSource implements KeyValueDataSource {

    private static final int BATCH_SIZE = 1024 * 1000 * 10;

    private DB db;
    private Map<byte[], byte[]> map;
    private String name;

    @Override
    public void init() {
        File dbLocation = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/");
        if (!dbLocation.exists()) {
            dbLocation.mkdirs();
        }


        db = DBMaker.fileDB(new File(dbLocation, name))
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();

        this.map = db.hashMapCreate(name)
                .keySerializer(Serializer.BYTE_ARRAY)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .makeOrGet();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public byte[] get(byte[] key) {
        return map.get(key);
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        try {
            return map.put(key, value);
        } finally {
            db.commit();
        }
    }

    @Override
    public void delete(byte[] key) {
        try {
            map.remove(key);
        } finally {
            db.commit();
        }
    }

    @Override
    public Set<byte[]> keys() {
        return map.keySet();
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        int savedSize = 0;
        try {
            for (byte[] key : rows.keySet()) {
                byte[] value = rows.get(key);
                savedSize += value.length;

                map.put(key, value);
                if (savedSize > BATCH_SIZE) {
                    db.commit();
                    savedSize = 0;
                }
            }
        } finally {
            db.commit();
        }
    }

    @Override
    public void close() {
        db.close();
    }
}
