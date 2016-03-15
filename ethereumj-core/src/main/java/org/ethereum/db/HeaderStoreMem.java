package org.ethereum.db;

import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.db.index.ArrayListIndex;
import org.ethereum.db.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 16.09.2015
 */
public class HeaderStoreMem implements HeaderStore {

    private static final Logger logger = LoggerFactory.getLogger("blockqueue");

    private Map<Long, BlockHeaderWrapper> headers = Collections.synchronizedMap(new HashMap<Long, BlockHeaderWrapper>());
    private final Index index = new ArrayListIndex(Collections.<Long>emptySet());

    private final Object mutex = new Object();

    @Override
    public void open() {
        logger.info("Header store opened");
    }

    @Override
    public void close() {
    }

    @Override
    public void add(BlockHeaderWrapper header) {

        synchronized (mutex) {
            if (index.contains(header.getNumber())) {
                return;
            }
            headers.put(header.getNumber(), header);
            index.add(header.getNumber());
        }
    }

    @Override
    public void addBatch(Collection<BlockHeaderWrapper> headers) {

        synchronized (mutex) {
            List<Long> numbers = new ArrayList<>(headers.size());
            for (BlockHeaderWrapper b : headers) {
                if(!index.contains(b.getNumber()) &&
                        !numbers.contains(b.getNumber())) {

                    this.headers.put(b.getNumber(), b);
                    numbers.add(b.getNumber());
                }
            }

            index.addAll(numbers);
        }
    }

    @Override
    public BlockHeaderWrapper peek() {

        synchronized (mutex) {
            if(index.isEmpty()) {
                return null;
            }

            Long idx = index.peek();
            return headers.get(idx);
        }
    }

    @Override
    public BlockHeaderWrapper poll() {
        synchronized (mutex) {
            return pollInner();
        }
    }

    @Override
    public List<BlockHeaderWrapper> pollBatch(int qty) {

        if (index.isEmpty()) {
            return Collections.emptyList();
        }

        List<BlockHeaderWrapper> headers = new ArrayList<>(qty > size() ? qty : size());

        synchronized (mutex) {
            while (headers.size() < qty) {
                BlockHeaderWrapper header = pollInner();
                if (header == null) break;
                headers.add(header);
            }
        }

        return headers;
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public void clear() {
        headers.clear();
        index.clear();
    }

    @Override
    public void drop(byte[] nodeId) {

        List<Long> removed = new ArrayList<>();

        synchronized (index) {

            boolean hasSent = false;

            for (Long idx : index) {
                BlockHeaderWrapper h = headers.get(idx);
                if (!hasSent) {
                    hasSent = h.sentBy(nodeId);
                }
                if (hasSent) removed.add(idx);
            }

            headers.keySet().removeAll(removed);
            index.removeAll(removed);
        }

        if (logger.isDebugEnabled()) {
            if (removed.isEmpty()) {
                logger.debug("0 headers are dropped out");
            } else {
                logger.debug("{} headers [{}..{}] are dropped out", removed.size(), removed.get(0), removed.get(removed.size() - 1));
            }
        }
    }

    private BlockHeaderWrapper pollInner() {

        if (index.isEmpty()) {
            return null;
        }

        Long idx = index.poll();
        BlockHeaderWrapper header = headers.get(idx);
        headers.remove(idx);

        if (header == null) {
            logger.error("Header for index {} is null", idx);
        }

        return header;
    }
}
