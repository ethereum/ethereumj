package org.ethereum.datasource.mapdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.QueueDataSource;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Map;

import static java.lang.System.getProperty;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public class MapDBQueueDataSource implements QueueDataSource {

    private DB db;
    private BTreeMap<Long, byte[]> map;
    private String name;

    @Override
    public void init() {
        File dbFile = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/" + name);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();

        db = DBMaker.fileDB(dbFile)
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();

        map = db.treeMapCreate(name)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .makeOrGet();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void offerFirst(byte[] e) {
        if(map.isEmpty()) {
            offerEmpty(e);
        } else {
            map.put(map.firstKey() - 1, e);
        }
    }

    @Override
    public byte[] peekFirst() {
        if(map.isEmpty()) {
            return null;
        } else {
            return map.firstEntry().getValue();
        }
    }

    @Override
    public byte[] pollFirst() {
        if(map.isEmpty()) {
            return null;
        } else {
            return map.pollFirstEntry().getValue();
        }
    }

    @Override
    public void offerLast(byte[] e) {
        if(map.isEmpty()) {
            offerEmpty(e);
        } else {
            map.put(map.lastKey() + 1, e);
        }
    }

    @Override
    public byte[] peekLast() {
        if(map.isEmpty()) {
            return null;
        } else {
            return map.lastEntry().getValue();
        }
    }

    @Override
    public byte[] pollLast() {
        if(map.isEmpty()) {
            return null;
        } else {
            return map.pollLastEntry().getValue();
        }
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    private void offerEmpty(byte[] e) {
        map.put(0L, e);
    }
}
