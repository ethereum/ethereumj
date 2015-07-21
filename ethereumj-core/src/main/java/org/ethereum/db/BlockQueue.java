package org.ethereum.db;

import org.ethereum.core.Block;

import java.util.Collection;
import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public interface BlockQueue extends DiskStore {

    void addAll(Collection<Block> blockList);

    void add(Block block);

    Block poll();

    Block peek();

    int size();

    boolean isEmpty();

    void clear();

    List<byte[]> filterExisting(Collection<byte[]> hashes);
}
