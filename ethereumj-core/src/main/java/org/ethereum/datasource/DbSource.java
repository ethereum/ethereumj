package org.ethereum.datasource;

import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public interface DbSource extends BatchSource<byte[], byte[]> {
    void setName(String name);

    String getName();

    void init();

    boolean isAlive();

    void close();

    Set<byte[]> keys();
}
