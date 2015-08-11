package org.ethereum.datasource.redis;

import org.ethereum.core.PendingTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.datasource.KeyValueDataSource;

import java.util.Map;
import java.util.Set;

public interface RedisConnection {
    
    public static final String REDISCLOUD_URL = "REDISCLOUD_URL";
    
    boolean isAvailable();

    <T> Set<T> createSetFor(Class<T> clazz, String name);

    <K,V> Map<K, V> createMapFor(Class<K> keyClass, Class<V> valueClass, String name);

    Set<Transaction> createTransactionSet(String name);

    Set<PendingTransaction> createPendingTransactionSet(String name);

    KeyValueDataSource createDataSource(String name);
}
