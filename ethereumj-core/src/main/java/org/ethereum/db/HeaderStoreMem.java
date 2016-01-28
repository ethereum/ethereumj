package org.ethereum.db;

import org.ethereum.core.BlockHeader;
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

    private Map<Long, BlockHeader> headers = Collections.synchronizedMap(new HashMap<Long, BlockHeader>());
    private Index index = new ArrayListIndex(Collections.<Long>emptySet());

    private final Object mutex = new Object();

    @Override
    public void open() {
        logger.info("Header store opened");
    }

    @Override
    public void close() {
    }

    @Override
    public void add(BlockHeader header) {

        synchronized (mutex) {
            if (index.contains(header.getNumber())) {
                return;
            }
            headers.put(header.getNumber(), header);
            index.add(header.getNumber());
        }
    }

    @Override
    public void addBatch(Collection<BlockHeader> headers) {

        synchronized (mutex) {
            List<Long> numbers = new ArrayList<>(headers.size());
            for (BlockHeader b : headers) {
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
    public BlockHeader peek() {

        synchronized (mutex) {
            if(index.isEmpty()) {
                return null;
            }

            Long idx = index.peek();
            return headers.get(idx);
        }
    }

    @Override
    public BlockHeader poll() {
        return pollInner();
    }

    @Override
    public List<BlockHeader> pollBatch(int qty) {

        if (index.isEmpty()) {
            return Collections.emptyList();
        }

        List<BlockHeader> headers = new ArrayList<>(qty > size() ? qty : size());
        while (headers.size() < qty) {
            BlockHeader header = pollInner();
            if(header == null) {
                break;
            }
            headers.add(header);
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

    private BlockHeader pollInner() {
        synchronized (mutex) {
            if (index.isEmpty()) {
                return null;
            }

            Long idx = index.poll();
            BlockHeader header = headers.get(idx);
            headers.remove(idx);

            if (header == null) {
                logger.error("Header for index {} is null", idx);
            }

            return header;
        }
    }
}
