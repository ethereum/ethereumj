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

    private final static String BLOCK_QUEUE_NAME = "block_queue";

    private Map<Integer, DB> allocated = new HashMap<>();

    @Override
    public KeyValueDataSource createDataSource() {
        return new MapDBDataSource();
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
        return createDB(name, false);
    }

    @Override
    public DB createTransactionalDB(String name) {
        return createDB(name, true);
    }

    private DB createDB(String name, boolean transactional) {
        File dbFile = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/" + name);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        DBMaker.Maker dbMaker =  DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown();
        if(!transactional) {
            dbMaker.transactionDisable();
        }
        return dbMaker.make();
    }

    private void allocate(Object resource, DB db) {
        allocated.put(System.identityHashCode(resource), db);
    }
}
