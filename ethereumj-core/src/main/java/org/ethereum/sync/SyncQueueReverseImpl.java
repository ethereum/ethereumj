package org.ethereum.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.FastByteComparisons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
public class SyncQueueReverseImpl implements SyncQueueIfc {

    byte[] curHeaderHash;
    List<BlockHeaderWrapper> headers = new ArrayList<>();
    ByteArrayMap<Block> blocks = new ByteArrayMap<>();

    public SyncQueueReverseImpl(byte[] startHash) {
        this.curHeaderHash = startHash;
    }

    @Override
    public synchronized HeadersRequest requestHeaders() {
        return new SyncQueueImpl.HeadersRequestImpl(curHeaderHash, curHeaderHash == null ? 0 : 192, true);
    }

    @Override
    public synchronized void addHeaders(Collection<BlockHeaderWrapper> newHeaders) {
        for (BlockHeaderWrapper header : newHeaders) {
            if (curHeaderHash != null && FastByteComparisons.equal(curHeaderHash, header.getHash())) {
                headers.add(header);
                curHeaderHash = header.getNumber() == 1 ? null : header.getHeader().getParentHash();
            } else {
                break;
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
            Block block = blocks.get(headers.get(0).getHash());
            if (block == null) break;
            ret.add(block);
            blocks.remove(headers.get(0).getHash());
            headers.remove(0);
        }
        return ret;
    }

    @Override
    public synchronized int getHeadersCount() {
        return headers.size();
    }
}
