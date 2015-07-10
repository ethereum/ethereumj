package org.ethereum.db;

import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public interface HashStore {

    void add(byte[] hash);

    void addFirst(byte[] hash);

    byte[] peek();

    byte[] poll();

    boolean isEmpty();

    void close();

    Set<Long> getKeys();
}
