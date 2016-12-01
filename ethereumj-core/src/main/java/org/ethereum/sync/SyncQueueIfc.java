package org.ethereum.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeaderWrapper;

import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 27.05.2016.
 */
public interface SyncQueueIfc {

    /**
     * Wanted headers
     */
    interface HeadersRequest {

        long getStart();

        byte[] getHash();

        int getCount();

        boolean isReverse();

        List<HeadersRequest> split(int maxCount);
    }

    /**
     * Wanted blocks
     */
    interface BlocksRequest {
        List<BlocksRequest> split(int count);

        List<BlockHeaderWrapper> getBlockHeaders();
    }

    /**
     * Returns wanted headers request
     */
    HeadersRequest requestHeaders();

    /**
     * Returns wanted headers request with defined size
     */
    HeadersRequest requestHeaders(int maxSize);

    /**
     * Adds received headers.
     * Headers need to verified.
     * The list can be in any order and shouldn't correspond to prior headers request
     */
    void addHeaders(Collection<BlockHeaderWrapper> headers);

    /**
     * Returns wanted blocks hashes
     */
    BlocksRequest requestBlocks(int maxSize);

    /**
     * Adds new received blocks to the queue
     * The blocks need to be verified but can be passed in any order and need not correspond
     * to prior returned block request
     * @return  blocks ready to be imported in the valid import order.
     */
    List<Block> addBlocks(Collection<Block> blocks);

    /**
     * Returns approximate header count waiting for their blocks
     */
    int getHeadersCount();
}
