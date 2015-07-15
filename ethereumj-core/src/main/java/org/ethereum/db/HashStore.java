package org.ethereum.db;

import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public interface HashStore extends DiskStore {

    void add(byte[] hash);

    void addFirst(byte[] hash);

    byte[] peek();

    byte[] poll();

    boolean isEmpty();

    Set<Long> getKeys();

    int size();

    void clear();
}
