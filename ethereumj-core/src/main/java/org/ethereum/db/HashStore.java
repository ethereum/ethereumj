package org.ethereum.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public interface HashStore extends DiskStore {

    void add(byte[] hash);

    void addFirst(byte[] hash);

    void addBatch(Collection<byte[]> hashes);

    void addFirstBatch(Collection<byte[]> hashes);

    byte[] peek();

    byte[] poll();

    List<byte[]> pollBatch(int qty);

    boolean isEmpty();

    Set<Long> getKeys();

    int size();

    void clear();

    void removeAll(Collection<byte[]> removing);
}
