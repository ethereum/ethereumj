package org.ethereum.db;

import org.ethereum.datasource.QueueDataSource;
import org.ethereum.datasource.mapdb.MapDBQueueDataSource;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public class HashStore {

    private static final String DEFAULT_NAME = "hashstore";

    private QueueDataSource hashes;

    private HashStore() {
    }

    public void add(byte[] hash) {
        hashes.offer(hash);
    }

    public byte[] peek() {
        return hashes.peek();
    }

    public byte[] poll() {
        return hashes.poll();
    }

    public boolean isEmpty() {
        return hashes.isEmpty();
    }

    public void close() {
        hashes.close();
    }

    static class Builder {
        private String name = DEFAULT_NAME;
        private Class<? extends QueueDataSource> dataSourceClass = MapDBQueueDataSource.class;

        public Builder withDataSource(Class<? extends QueueDataSource> dataSourceClass) {
            this.dataSourceClass = dataSourceClass;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public HashStore build() throws IllegalAccessException, InstantiationException {
            HashStore store = new HashStore();
            store.hashes = dataSourceClass.newInstance();
            store.hashes.setName(name);
            store.hashes.init();
            return store;
        }
    }
}
