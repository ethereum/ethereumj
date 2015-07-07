package org.ethereum.datasource.mapdb;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.QueueDataSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Queue;

import static java.lang.System.getProperty;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public class MapDBQueueDataSource implements QueueDataSource {

    private DB db;
    private Queue<byte[]> queue;
    private String name;

    @Override
    public void init() {
        File dbFile = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/" + name);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();

        db = DBMaker.fileDB(dbFile)
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();

        this.queue = db.getQueue(name);
        if(this.queue == null) {
            this.queue = db.createQueue(name, Serializer.BYTE_ARRAY, true);
        }
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
    public boolean offer(byte[] e) {
        return queue.offer(e);
    }

    @Override
    public byte[] poll() {
        return queue.poll();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public byte[] peek() {
        return queue.peek();
    }
}
