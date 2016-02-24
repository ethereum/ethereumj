package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 16.09.2015
 */
public interface HeaderStore extends DiskStore {

    void add(BlockHeaderWrapper header);

    void addBatch(Collection<BlockHeaderWrapper> headers);

    BlockHeaderWrapper peek();

    BlockHeaderWrapper poll();

    List<BlockHeaderWrapper> pollBatch(int qty);

    boolean isEmpty();

    int size();

    void clear();

    void drop(byte[] nodeId);
}
