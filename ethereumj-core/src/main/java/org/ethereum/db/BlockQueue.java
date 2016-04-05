package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
public interface BlockQueue extends DiskStore {

    void addOrReplaceAll(Collection<BlockWrapper> blockList);

    void add(BlockWrapper block);

    void returnBlock(BlockWrapper block);

    void addOrReplace(BlockWrapper block);

    BlockWrapper poll();

    BlockWrapper peek();

    BlockWrapper take();

    int size();

    boolean isEmpty();

    void clear();

    List<byte[]> filterExisting(Collection<byte[]> hashes);

    List<BlockHeader> filterExistingHeaders(Collection<BlockHeader> headers);

    boolean isBlockExist(byte[] hash);

    void drop(byte[] nodeId, int scanLimit);

    long getLastNumber();

    BlockWrapper peekLast();

    void remove(BlockWrapper block);
}
