package org.ethereum.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.MinMaxMap;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 27.10.2016.
 */
public class SyncQueueReverseImpl implements SyncQueueIfc {

    byte[] curHeaderHash;
//    List<BlockHeaderWrapper> headers = new ArrayList<>();
    MinMaxMap<BlockHeaderWrapper> headers = new MinMaxMap<>();
    long minValidated = -1;

    ByteArrayMap<Block> blocks = new ByteArrayMap<>();

    boolean headersOnly;

    public SyncQueueReverseImpl(byte[] startHash) {
        this.curHeaderHash = startHash;
    }

    public SyncQueueReverseImpl(byte[] startHash, boolean headersOnly) {
        this.curHeaderHash = startHash;
        this.headersOnly = headersOnly;
    }

    @Override
    public synchronized List<HeadersRequest> requestHeaders(int maxSize, int maxRequests) {
        List<HeadersRequest> ret = new ArrayList<>();
        if (minValidated < 0) {
            ret.add(new SyncQueueImpl.HeadersRequestImpl(curHeaderHash, maxSize, true, maxSize - 1));
        } else if (minValidated == 0) {
            // genesis reached
        } else {
            if (minValidated - headers.getMin() < maxSize * maxSize && minValidated > maxSize) {
                ret.add(new SyncQueueImpl.HeadersRequestImpl(
                        headers.get(headers.getMin()).getHash(), maxSize, true, maxSize - 1));
                maxRequests--;
            }

            Set<Map.Entry<Long, BlockHeaderWrapper>> entries =
                    headers.descendingMap().subMap(minValidated, true, headers.getMin(), true).entrySet();
            Iterator<Map.Entry<Long, BlockHeaderWrapper>> it = entries.iterator();
            BlockHeaderWrapper prevEntry = it.next().getValue();
            while(maxRequests > 0 && it.hasNext()) {
                BlockHeaderWrapper entry = it.next().getValue();
                if (prevEntry.getNumber() - entry.getNumber() > 1) {
                    ret.add(new SyncQueueImpl.HeadersRequestImpl(prevEntry.getHash(), maxSize, true));
                    maxRequests--;
                }
                prevEntry = entry;
            }
            if (maxRequests > 0) {
                ret.add(new SyncQueueImpl.HeadersRequestImpl(prevEntry.getHash(), maxSize, true));
            }
        }

        return ret;
    }

    @Override
    public synchronized List<BlockHeaderWrapper> addHeaders(Collection<BlockHeaderWrapper> newHeaders) {
        if (minValidated < 0) {
            // need to fetch initial header
            for (BlockHeaderWrapper header : newHeaders) {
                if (FastByteComparisons.equal(curHeaderHash, header.getHash())) {
                    minValidated = header.getNumber();
                    headers.put(header.getNumber(), header);
                }
            }
        }

        if (minValidated < 0) return Collections.emptyList(); // start header not found

        for (BlockHeaderWrapper header : newHeaders) {
            if (header.getNumber() < minValidated) {
                headers.put(header.getNumber(), header);
            }
        }


        if (minValidated == -1) minValidated = headers.getMax();
        for (; minValidated >= headers.getMin() ; minValidated--) {
            BlockHeaderWrapper header = headers.get(minValidated);
            BlockHeaderWrapper parent = headers.get(minValidated - 1);
            if (parent == null) break;
            if (!FastByteComparisons.equal(header.getHeader().getParentHash(), parent.getHash())) {
                // chain is broken here (unlikely) - refetch the rest
                headers.clearAllBefore(header.getNumber());
                break;
            }
        }
        if (headersOnly) {
            List<BlockHeaderWrapper> ret = new ArrayList<>();
            for (long i = headers.getMax(); i > minValidated; i--) {
                ret.add(headers.remove(i));
            }
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public synchronized BlocksRequest requestBlocks(int maxSize) {
        List<BlockHeaderWrapper> reqHeaders = new ArrayList<>();
        for (BlockHeaderWrapper header : headers.descendingMap().values()) {
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
        for (long i = headers.getMax(); i > minValidated; i--) {
            Block block = blocks.get(headers.get(i).getHash());
            if (block == null) break;
            ret.add(block);
            blocks.remove(headers.get(i).getHash());
            headers.remove(i);
        }
        return ret;
    }

    @Override
    public synchronized int getHeadersCount() {
        return headers.size();
    }
}
