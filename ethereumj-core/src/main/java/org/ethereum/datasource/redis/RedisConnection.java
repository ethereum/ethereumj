package org.ethereum.datasource.redis;

import org.ethereum.core.Transaction;
import org.ethereum.datasource.KeyValueDataSource;

import java.util.Set;

public interface RedisConnection {
    
    boolean isAvailable();

    <T> Set<T> createSetFor(Class<T> clazz, String name);

    Set<Transaction> createTransactionSet(String name);

    KeyValueDataSource createKeyValueDataSource(String name);
}
