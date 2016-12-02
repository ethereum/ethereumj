package org.ethereum.sync;

import org.ethereum.TestUtils;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.db.ByteArrayWrapper;
import org.junit.Test;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 30.05.2016.
 */
public class SyncQueueImplTest {
    byte[] peer0 = new byte[32];
    private static final int DEFAULT_REQUEST_LEN = 192;

    @Test
    public void test1() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 1024);

        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain.subList(0, 32));

        SyncQueueIfc.HeadersRequest headersRequest = syncQueue.requestHeaders(DEFAULT_REQUEST_LEN);
        System.out.println(headersRequest);

        syncQueue.addHeaders(createHeadersFromBlocks(TestUtils.getRandomChain(randomChain.get(16).getHash(), 17, 64), peer0));

        syncQueue.addHeaders(createHeadersFromBlocks(randomChain.subList(32, 1024), peer0));
    }

    @Test
    public void test2() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 1024);

        Peer[] peers = new Peer[10];
        peers[0] = new Peer(randomChain);
        for (int i = 1; i < peers.length; i++) {
            peers[i] = new Peer(TestUtils.getRandomChain(TestUtils.randomBytes(32), 1, 1024));
        }

    }

    @Test
    public void testHeadersSplit() {
        // 1, 2, 3, 4, 5
        SyncQueueImpl.HeadersRequestImpl headersRequest = new SyncQueueImpl.HeadersRequestImpl(1, 5, false);
        List<SyncQueueIfc.HeadersRequest> requests = headersRequest.split(2);
        assert requests.size() == 3;

        // 1, 2
        assert requests.get(0).getStart() == 1;
        assert requests.get(0).getCount() == 2;

        // 3, 4
        assert requests.get(1).getStart() == 3;
        assert requests.get(1).getCount() == 2;

        // 5
        assert requests.get(2).getStart() == 5;
        assert requests.get(2).getCount() == 1;
    }

    public void test2Impl(List<Block> mainChain, List<Block> initChain, Peer[] peers) {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 1024);
        final Block[] maxExportedBlock = new Block[] {randomChain.get(31)};
        final Map<ByteArrayWrapper, Block> exportedBlocks = new HashMap<>();
        for (Block block : randomChain.subList(0, 32)) {
            exportedBlocks.put(new ByteArrayWrapper(block.getHash()), block);
        }

        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain.subList(0, 32)) {
            @Override
            protected void exportNewBlock(Block block) {
                exportedBlocks.put(new ByteArrayWrapper(block.getHash()), block);
                if (!exportedBlocks.containsKey(new ByteArrayWrapper(block.getParentHash()))) {
                    throw new RuntimeException("No parent for " + block);
                }
                if (block.getNumber() > maxExportedBlock[0].getNumber()) {
                    maxExportedBlock[0] = block;
                }
            }
        };


        Random rnd = new Random();

        int i = 0;
        for (; i < 1000; i++) {
            SyncQueueIfc.HeadersRequest headersRequest = syncQueue.requestHeaders(DEFAULT_REQUEST_LEN);
            List<BlockHeader> headers = peers[rnd.nextInt(peers.length)].getHeaders(headersRequest.getStart(), headersRequest.getCount(), headersRequest.isReverse());
            syncQueue.addHeaders(createHeadersFromHeaders(headers, peer0));
            SyncQueueIfc.BlocksRequest blocksRequest = syncQueue.requestBlocks(rnd.nextInt(128 + 1));
            List<Block> blocks = peers[rnd.nextInt(peers.length)].getBlocks(blocksRequest.getBlockHeaders());
            syncQueue.addBlocks(blocks);
            if (maxExportedBlock[0].getNumber() == randomChain.get(randomChain.size() - 1).getNumber()) {
                break;
            }
        }

        if (i == 1000) throw new RuntimeException("Exported only till block: " + maxExportedBlock[0]);
    }

    private static class Peer {
        Map<ByteArrayWrapper, Block> blocks = new HashMap<>();
        List<Block> chain;

        public Peer(List<Block> chain) {
            this.chain = chain;
            for (Block block : chain) {
                blocks.put(new ByteArrayWrapper(block.getHash()), block);
            }
        }

        public List<BlockHeader> getHeaders(long startBlockNum, int count, boolean reverse) {
            if (reverse) {
                startBlockNum = startBlockNum - count + 1;
            }
            startBlockNum = Math.max(startBlockNum, chain.get(0).getNumber());
            startBlockNum = Math.min(startBlockNum, chain.get(chain.size() - 1).getNumber());
            long endBlockNum = startBlockNum + count - 1;
            endBlockNum = Math.max(endBlockNum, chain.get(0).getNumber());
            endBlockNum = Math.min(endBlockNum, chain.get(chain.size() - 1).getNumber());
            List<BlockHeader> ret = new ArrayList<>();
            int startIdx = (int) (startBlockNum - chain.get(0).getNumber());
            for (int i = startIdx; i < startIdx + (endBlockNum - startBlockNum + 1); i++) {
                ret.add(chain.get(i).getHeader());
            }
            return ret;
        }

        public List<Block> getBlocks(Collection<BlockHeaderWrapper> hashes) {
            List<Block> ret = new ArrayList<>();
            for (BlockHeaderWrapper hash : hashes) {
                Block block = blocks.get(new ByteArrayWrapper(hash.getHash()));
                if (block != null) ret.add(block);
            }
            return ret;
        }
    }

    private List<BlockHeaderWrapper> createHeadersFromHeaders(List<BlockHeader> headers, byte[] peer) {
        List<BlockHeaderWrapper> ret = new ArrayList<>();
        for (BlockHeader header : headers) {
            ret.add(new BlockHeaderWrapper(header, peer));
        }
        return ret;
    }
    private List<BlockHeaderWrapper> createHeadersFromBlocks(List<Block> blocks, byte[] peer) {
        List<BlockHeaderWrapper> ret = new ArrayList<>();
        for (Block block : blocks) {
            ret.add(new BlockHeaderWrapper(block.getHeader(), peer));
        }
        return ret;
    }
}
