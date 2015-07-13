package org.ethereum.datasource.mapdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.datasource.KeyValueDataSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;

@Component
public class MapDBFactoryImpl implements MapDBFactory {

    private final static String HASH_STORE_NAME = "hash_store";
    private final static String BLOCK_QUEUE_NAME = "block_queue";

    private Map<Integer, DB> allocated = new HashMap<>();

    @Override
    public KeyValueDataSource createDataSource() {
        return new MapDBDataSource();
    }

    @Override
    public Map<Long, byte[]> createHashStoreMap() {
        DB db = createDB(HASH_STORE_NAME);
        Map<Long, byte[]> map = db.hashMapCreate(HASH_STORE_NAME)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .makeOrGet();
        allocate(map, db);
        return map;
    }

    @Override
    public Map<Long, Block> createBlockQueueMap() {
        DB db = createDB(BLOCK_QUEUE_NAME);
        Map<Long, Block> map = db.hashMapCreate(BLOCK_QUEUE_NAME)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializers.BLOCK)
                .makeOrGet();
        allocate(map, db);
        return map;
    }

    @Override
    public void destroy(Object resource) {
        int hashCode = System.identityHashCode(resource);
        DB db = allocated.get(hashCode);
        if(db != null) {
            db.close();
            allocated.remove(hashCode);
        }
    }

    @Override
    public DB createDB(String name) {
        File dbFile = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/" + name);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        return DBMaker.fileDB(dbFile)
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();
    }

    private void allocate(Object resource, DB db) {
        allocated.put(System.identityHashCode(resource), db);
    }
}
