package org.ethereum.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.FastByteComparisons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
public class SyncQueueForwardImpl implements SyncQueueIfc {

    byte[] curHeaderHash;
    byte[] endHash;
    Long lastHeaderNumber = 0L;
    Set<BlockHeaderWrapper> headers = new LinkedHashSet<>();
    ByteArrayMap<Block> blocks = new ByteArrayMap<>();

    public SyncQueueForwardImpl(byte[] startHash, byte[] endHash) {
        this.curHeaderHash = startHash;
        this.endHash = endHash;
    }

    @Override
    public synchronized HeadersRequest requestHeaders() {
        return new SyncQueueImpl.HeadersRequestImpl(curHeaderHash, curHeaderHash == null ? 0 : 192, false);
    }

    @Override
    public synchronized void addHeaders(Collection<BlockHeaderWrapper> newHeaders) {
        for (BlockHeaderWrapper header : newHeaders) {
            if (curHeaderHash != null && !FastByteComparisons.equal(curHeaderHash, header.getHash())
                    && header.getNumber() > lastHeaderNumber) {
                headers.add(header);
                lastHeaderNumber = header.getNumber();
                curHeaderHash = Arrays.equals(header.getHash(), endHash) ? null : header.getHash();
            }
        }
    }

    @Override
    public synchronized BlocksRequest requestBlocks(int maxSize) {
        List<BlockHeaderWrapper> reqHeaders = new ArrayList<>();
        for (BlockHeaderWrapper header : headers) {
            if (maxSize == 0) break;
            if (blocks.get(header.getHash()) == null) {
                reqHeaders.add(header);
                maxSize--;
            }
        }
        return new SyncQueueImpl.BlocksRequestImpl(reqHeaders);
    }

    @Override
    public synchronized List<Block> addBlocks(Collection<Block> newBlocks) {
        for (Block block : newBlocks) {
            blocks.put(block.getHash(), block);
        }
        List<Block> ret = new ArrayList<>();
        while(!headers.isEmpty()) {
            BlockHeaderWrapper first = headers.iterator().next();
            Block block = blocks.get(first.getHash());
            if (block == null) break;
            ret.add(block);
            blocks.remove(first.getHash());
            headers.remove(first);
        }
        return ret;
    }

    @Override
    public synchronized int getHeadersCount() {
        return headers.size();
    }
}
