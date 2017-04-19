/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
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

        int getStep();
    }

    /**
     * Wanted blocks
     */
    interface BlocksRequest {
        List<BlocksRequest> split(int count);

        List<BlockHeaderWrapper> getBlockHeaders();
    }

    /**
     * Returns wanted headers requests
     * @param maxSize Maximum number of headers in a singles request
     * @param maxRequests Maximum number of requests
     * @param maxTotalHeaders The total maximum of cached headers in the implementation
     * @return null if the end of headers reached (e.g. when download is limited with a block number)
     *   empty list if no headers for now (e.g. max allowed number of cached headers reached)
     */
    List<HeadersRequest> requestHeaders(int maxSize, int maxRequests, int maxTotalHeaders);

    /**
     * Adds received headers.
     * Headers themselves need to be verified (except parent hash)
     * The list can be in any order and shouldn't correspond to prior headers request
     * @return If this is 'header-only' SyncQueue then the next chain of headers
     * is popped from SyncQueue and returned
     * The reverse implementation should return headers in revers order (N, N-1, ...)
     * If this instance is for headers+blocks downloading then null returned
     */
    List<BlockHeaderWrapper> addHeaders(Collection<BlockHeaderWrapper> headers);

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
