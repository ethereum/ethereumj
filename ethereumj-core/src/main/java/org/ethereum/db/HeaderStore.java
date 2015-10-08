package org.ethereum.db;

import org.ethereum.core.BlockHeader;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 16.09.2015
 */
public interface HeaderStore extends DiskStore {

    void add(BlockHeader header);

    void addBatch(Collection<BlockHeader> headers);

    BlockHeader peek();

    BlockHeader poll();

    List<BlockHeader> pollBatch(int qty);

    boolean isEmpty();

    int size();

    void clear();
}
