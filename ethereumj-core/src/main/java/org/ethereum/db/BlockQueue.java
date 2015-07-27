package org.ethereum.db;

import org.ethereum.core.BlockWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public interface BlockQueue extends DiskStore {

    void addAll(Collection<BlockWrapper> blockList);

    void add(BlockWrapper block);

    BlockWrapper poll();

    BlockWrapper peek();

    BlockWrapper take();

    int size();

    boolean isEmpty();

    void clear();

    List<byte[]> filterExisting(Collection<byte[]> hashes);

    Set<byte[]> getHashes();
}
