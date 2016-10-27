package org.ethereum.sync;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.core.Blockchain;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.Functional;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 27.05.2016.
 */
public class SyncQueueImpl implements SyncQueueIfc {
    static int MAX_CHAIN_LEN = 192;

    class HeadersRequestImpl implements HeadersRequest {
        public HeadersRequestImpl(long start, int count, boolean reverse) {
            this.start = start;
            this.count = count;
            this.reverse = reverse;
        }

        private long start;
        private int count;

        private boolean reverse;

        @Override
        public String toString() {
            return "HeadersRequest{" +
                    "start=" + getStart() +
                    ", count=" + getCount() +
                    ", reverse=" + isReverse() +
                    '}';
        }

        @Override
        public long getStart() {
            return start;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isReverse() {
            return reverse;
        }
    }

    class BlocksRequestImpl implements BlocksRequest {
        private List<BlockHeaderWrapper> blockHeaders = new ArrayList<>();

        public BlocksRequestImpl() {
        }

        public BlocksRequestImpl(List<BlockHeaderWrapper> blockHeaders) {
            this.blockHeaders = blockHeaders;
        }

        @Override
        public List<BlocksRequest> split(int count) {
            List<BlocksRequest> ret = new ArrayList<>();
            int start = 0;
            while(start < getBlockHeaders().size()) {
                count = Math.min(getBlockHeaders().size() - start, count);
                ret.add(new BlocksRequestImpl(getBlockHeaders().subList(start, start + count)));
                start += count;
            }
            return ret;
        }

        @Override
        public List<BlockHeaderWrapper> getBlockHeaders() {
            return blockHeaders;
        }
    }

    class HeaderElement {
        BlockHeaderWrapper header;
        Block block;
        boolean exported;

        public HeaderElement(BlockHeaderWrapper header) {
            this.header = header;
        }

        public HeaderElement getParent() {
            Map<ByteArrayWrapper, HeaderElement> genHeaders = headers.get(header.getNumber() - 1);
            if (genHeaders == null) return null;
            return genHeaders.get(new ByteArrayWrapper(header.getHeader().getParentHash()));
        }

        public List<HeaderElement> getChildren() {
            List<HeaderElement> ret = new ArrayList<>();
            Map<ByteArrayWrapper, HeaderElement> childGenHeaders = headers.get(header.getNumber() + 1);
            if (childGenHeaders != null) {
                for (HeaderElement child : childGenHeaders.values()) {
                    if (Arrays.equals(child.header.getHeader().getParentHash(), header.getHash())) {
                        ret.add(child);
                    }
                }
            }
            return ret;
        }
    }

    Map<Long, Map<ByteArrayWrapper, HeaderElement>> headers = new HashMap<>();

    long minNum = Integer.MAX_VALUE;
    long maxNum = 0;
    long darkZoneNum = 0;

    Random rnd = new Random(); // ;)

    public SyncQueueImpl(List<Block> initBlocks) {
        init(initBlocks);
    }

    public SyncQueueImpl(Blockchain bc) {
        Block bestBlock = bc.getBestBlock();
        long start = bestBlock.getNumber() - 0;
        start = start < 0 ? 0 : start;
        List<Block> initBlocks = new ArrayList<>();
        for (long i = start; i <= bestBlock.getNumber(); i++) {
            initBlocks.add(bc.getBlockByNumber(i));
        }
        init(initBlocks);
    }

    private void init(List<Block> initBlocks) {
//        if (initBlocks.size() < MAX_CHAIN_LEN && initBlocks.get(0).getNumber() != 0) {
//            throw new RuntimeException("Queue should be initialized with a chain of at least " + MAX_CHAIN_LEN + " size or with the first genesis block");
//        }
        for (Block block : initBlocks) {
            addHeaderPriv(new BlockHeaderWrapper(block.getHeader(), null));
            addBlock(block).exported = true;
        }
        darkZoneNum = initBlocks.get(0).getNumber();
    }

    private void putGenHeaders(long num, Map<ByteArrayWrapper, HeaderElement> genHeaders) {
        minNum = Math.min(minNum, num);
        maxNum = Math.max(maxNum, num);
        headers.put(num, genHeaders);
    }

    private List<HeaderElement> getLongestChain() {
        Map<ByteArrayWrapper, HeaderElement> lastValidatedGen = headers.get(darkZoneNum);
        assert lastValidatedGen.size() == 1;
        return getLongestChain(lastValidatedGen.values().iterator().next());
    }

    private List<HeaderElement> getLongestChain(HeaderElement parent) {
        Map<ByteArrayWrapper, HeaderElement> gen = headers.get(parent.header.getNumber() + 1);
        List<HeaderElement> longest = new ArrayList<>();
        if (gen != null) {
            for (HeaderElement header : gen.values()) {
                if (header.getParent() == parent) {
                    List<HeaderElement> childLongest = getLongestChain(header);
                    if (childLongest.size() > longest.size()) {
                        longest = childLongest;
                    }
                }
            }
        }
        List<HeaderElement> ret = new ArrayList<>();
        ret.add(parent);
        ret.addAll(longest);
        return ret;
    }

    private boolean hasGaps() {
        List<HeaderElement> longestChain = getLongestChain();
        return longestChain.get(longestChain.size() - 1).header.getNumber() < maxNum;
    }

    private void trimChain() {
        List<HeaderElement> longestChain = getLongestChain();
        if (longestChain.size() > MAX_CHAIN_LEN) {
            long newTrimNum = getLongestChain().get(longestChain.size() - MAX_CHAIN_LEN).header.getNumber();
            for (int i = 0; darkZoneNum < newTrimNum; darkZoneNum++, i++) {
                ByteArrayWrapper wHash = new ByteArrayWrapper(longestChain.get(i).header.getHash());
                putGenHeaders(darkZoneNum, Collections.singletonMap(wHash, longestChain.get(i)));
            }
            darkZoneNum--;
        }
    }

    private void trimExported() {
        for (; minNum < darkZoneNum; minNum++) {
            Map<ByteArrayWrapper, HeaderElement> genHeaders = headers.get(minNum);
            assert genHeaders.size() == 1;
            HeaderElement headerElement = genHeaders.values().iterator().next();
            if (headerElement.exported) {
                headers.remove(minNum);
            } else {
                break;
            }
        }
    }

    private boolean addHeader(BlockHeaderWrapper header) {
        long num = header.getNumber();
        if (num <= darkZoneNum || num > maxNum + MAX_CHAIN_LEN * 2) {
            // dropping too distant headers
            return false;
        }
        return addHeaderPriv(header);
    }

    private boolean addHeaderPriv(BlockHeaderWrapper header) {
        long num = header.getNumber();
        Map<ByteArrayWrapper, HeaderElement> genHeaders = headers.get(num);
        if (genHeaders == null) {
            genHeaders = new HashMap<>();
            putGenHeaders(num, genHeaders);
        }
        ByteArrayWrapper wHash = new ByteArrayWrapper(header.getHash());
        HeaderElement headerElement = genHeaders.get(wHash);
        if (headerElement != null) return false;

        headerElement = new HeaderElement(header);
        genHeaders.put(wHash, headerElement);

        return true;
    }

    @Override
    public synchronized HeadersRequest requestHeaders() {
        if (!hasGaps()) {
            return new HeadersRequestImpl(maxNum + 1, MAX_CHAIN_LEN, false);
        } else {
            List<HeaderElement> longestChain = getLongestChain();
            if (rnd.nextBoolean()) {
                return new HeadersRequestImpl(longestChain.get(longestChain.size() - 1).header.getNumber(), MAX_CHAIN_LEN, false);
            } else {
                return new HeadersRequestImpl(longestChain.get(longestChain.size() - 1).header.getNumber(), MAX_CHAIN_LEN, true);
            }
        }
    }

    @Override
    public synchronized void addHeaders(Collection<BlockHeaderWrapper> headers) {
        for (BlockHeaderWrapper header : headers) {
            addHeader(header);
        }
        trimChain();
    }

    @Override
    public synchronized int getHeadersCount() {
        return (int) (maxNum - minNum);
    }

    @Override
    public synchronized BlocksRequest requestBlocks(int maxSize) {
        BlocksRequest ret = new BlocksRequestImpl();

        outer:
        for (long i = minNum; i <= maxNum; i++) {
            Map<ByteArrayWrapper, HeaderElement> gen = headers.get(i);
            if (gen != null) {
                for (HeaderElement element : gen.values()) {
                    if (element.block == null) {
                        ret.getBlockHeaders().add(element.header);
                        if (ret.getBlockHeaders().size() >= maxSize) break outer;
                    }
                }
            }
        }
        return ret;
    }

    HeaderElement findHeaderElement(BlockHeader bh) {
        Map<ByteArrayWrapper, HeaderElement> genHeaders = headers.get(bh.getNumber());
        if (genHeaders == null) return null;
        return genHeaders.get(new ByteArrayWrapper(bh.getHash()));
    }

    private HeaderElement addBlock(Block block) {
        HeaderElement headerElement = findHeaderElement(block.getHeader());
        if (headerElement != null) {
            headerElement.block = block;
        }
        return headerElement;
    }

    @Override
    public synchronized List<Block> addBlocks(Collection<Block> blocks) {
        for (Block block : blocks) {
            addBlock(block);
        }
        return exportBlocks();
    }

    private List<Block> exportBlocks() {
        List<Block> ret = new ArrayList<>();
        for (long i = minNum; i <= maxNum; i++) {
            Map<ByteArrayWrapper, HeaderElement> gen = headers.get(i);
            if (gen == null) break;

            boolean hasAny = false;
            for (HeaderElement element : gen.values()) {
                HeaderElement parent = element.getParent();
                if (element.block != null && (i == minNum || parent != null && parent.exported)) {
                    if (!element.exported) {
                        exportNewBlock(element.block);
                        ret.add(element.block);
                        element.exported = true;
                    }
                    hasAny = true;
                }
            }
            if (!hasAny) break;
        }
        trimExported();
        return ret;
    }

    protected void exportNewBlock(Block block) {

    }

    public synchronized List<Block> pollBlocks() {
        return null;
    }


    interface Visitor<T> {
        T visit(HeaderElement el, List<T> childrenRes);
    }

    class ChildVisitor<T> {
        private Visitor<T> handler;
        boolean downUp = true;

        public ChildVisitor(Functional.Function<HeaderElement, List<T>> handler) {
//            this.handler = handler;
        }

        public T traverse(HeaderElement el) {
            List<T> childrenRet = new ArrayList<>();
            for (HeaderElement child : el.getChildren()) {
                T res = traverse(child);
                childrenRet.add(res);
            }
            return handler.visit(el, childrenRet);
        }
    }
}
