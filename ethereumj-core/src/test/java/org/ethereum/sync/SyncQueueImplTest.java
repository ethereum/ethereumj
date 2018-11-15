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

import org.ethereum.TestUtils;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockHeaderWrapper;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.validator.DependentBlockHeaderRule;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.ethereum.crypto.HashUtil.randomPeerId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

        SyncQueueIfc.HeadersRequest headersRequest = syncQueue.requestHeaders(DEFAULT_REQUEST_LEN, 1, Integer.MAX_VALUE).iterator().next();
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

    @Test
    public void testReverseHeaders1() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 699);
        List<Block> randomChain1 = TestUtils.getRandomChain(new byte[32], 0, 699);
        Peer[] peers = new Peer[]{new Peer(randomChain), new Peer(randomChain, false), new Peer(randomChain1)};
        SyncQueueReverseImpl syncQueue = new SyncQueueReverseImpl(randomChain.get(randomChain.size() - 1).getHash(), true);
        List<BlockHeaderWrapper> result = new ArrayList<>();
        int peerIdx = 1;
        Random rnd = new Random();
        int cnt = 0;
        while (cnt < 1000) {
            System.out.println("Cnt: " + cnt++);
            Collection<SyncQueueIfc.HeadersRequest> headersRequests = syncQueue.requestHeaders(20, 5, Integer.MAX_VALUE);
            if (headersRequests == null) break;
            for (SyncQueueIfc.HeadersRequest request : headersRequests) {
                System.out.println("Req: " + request);
                List<BlockHeader> headers = rnd.nextBoolean() ? peers[peerIdx].getHeaders(request)
                        : peers[peerIdx].getRandomHeaders(10);
                //                List<BlockHeader> headers = peers[0].getHeaders(request);

                peerIdx = (peerIdx + 1) % peers.length;
                List<BlockHeaderWrapper> ret = syncQueue.addHeaders(createHeadersFromHeaders(headers, peer0));
                result.addAll(ret);
                System.out.println("Result length: " + result.size());
            }
        }

        List<BlockHeaderWrapper> extraHeaders =
                syncQueue.addHeaders(createHeadersFromHeaders(peers[0].getRandomHeaders(10), peer0));
        assert extraHeaders.isEmpty();

        assert cnt != 1000;
        assert result.size() == randomChain.size() - 1;
        for (int i = 0; i < result.size() - 1; i++) {
            assert Arrays.equals(result.get(i + 1).getHash(), result.get(i).getHeader().getParentHash());
        }
        assert Arrays.equals(randomChain.get(0).getHash(), result.get(result.size() - 1).getHeader().getParentHash());
    }

    @Test
    public void testReverseHeaders2() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 194);
        Peer[] peers = new Peer[]{new Peer(randomChain), new Peer(randomChain)};
        SyncQueueReverseImpl syncQueue = new SyncQueueReverseImpl(randomChain.get(randomChain.size() - 1).getHash(), true);
        List<BlockHeaderWrapper> result = new ArrayList<>();
        int peerIdx = 1;
        int cnt = 0;
        while (cnt < 100) {
            System.out.println("Cnt: " + cnt++);
            Collection<SyncQueueIfc.HeadersRequest> headersRequests = syncQueue.requestHeaders(192, 10, Integer.MAX_VALUE);
            if (headersRequests == null) break;
            for (SyncQueueIfc.HeadersRequest request : headersRequests) {
                System.out.println("Req: " + request);
                List<BlockHeader> headers = peers[peerIdx].getHeaders(request);

                // Removing genesis header, which we will not get from real peers
                Iterator<BlockHeader> it = headers.iterator();
                while (it.hasNext()) {
                    if (FastByteComparisons.equal(it.next().getHash(), randomChain.get(0).getHash())) it.remove();
                }

                peerIdx = (peerIdx + 1) % 2;
                List<BlockHeaderWrapper> ret = syncQueue.addHeaders(createHeadersFromHeaders(headers, peer0));
                result.addAll(ret);
                System.out.println("Result length: " + result.size());
            }
        }

        assert cnt != 100;
        assert result.size() == randomChain.size() - 1; // - genesis
        for (int  i = 0; i < result.size() - 1; i++) {
            assert Arrays.equals(result.get(i + 1).getHash(), result.get(i).getHeader().getParentHash());
        }
        assert Arrays.equals(randomChain.get(0).getHash(), result.get(result.size() - 1).getHeader().getParentHash());
    }

    @Test
    // a copy of testReverseHeaders2 with #addHeadersAndValidate() instead #addHeaders(),
    // makes sure that nothing is broken
    public void testReverseHeaders3() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 194);
        Peer[] peers = new Peer[]{new Peer(randomChain), new Peer(randomChain)};
        SyncQueueReverseImpl syncQueue = new SyncQueueReverseImpl(randomChain.get(randomChain.size() - 1).getHash(), true);
        List<BlockHeaderWrapper> result = new ArrayList<>();
        int peerIdx = 1;
        int cnt = 0;
        while (cnt < 100) {
            System.out.println("Cnt: " + cnt++);
            Collection<SyncQueueIfc.HeadersRequest> headersRequests = syncQueue.requestHeaders(192, 10, Integer.MAX_VALUE);
            if (headersRequests == null) break;
            for (SyncQueueIfc.HeadersRequest request : headersRequests) {
                System.out.println("Req: " + request);
                List<BlockHeader> headers = peers[peerIdx].getHeaders(request);

                // Removing genesis header, which we will not get from real peers
                Iterator<BlockHeader> it = headers.iterator();
                while (it.hasNext()) {
                    if (FastByteComparisons.equal(it.next().getHash(), randomChain.get(0).getHash())) it.remove();
                }

                peerIdx = (peerIdx + 1) % 2;
                SyncQueueIfc.ValidatedHeaders ret = syncQueue.addHeadersAndValidate(createHeadersFromHeaders(headers, peer0));
                assert ret.isValid();
                result.addAll(ret.getHeaders());
                System.out.println("Result length: " + result.size());
            }
        }

        assert cnt != 100;
        assert result.size() == randomChain.size() - 1; // - genesis
        for (int  i = 0; i < result.size() - 1; i++) {
            assert Arrays.equals(result.get(i + 1).getHash(), result.get(i).getHeader().getParentHash());
        }
        assert Arrays.equals(randomChain.get(0).getHash(), result.get(result.size() - 1).getHeader().getParentHash());
    }

    @Test
    public void testLongLongestChain() {
        List<Block> randomChain = TestUtils.getRandomAltChain(new byte[32], 0, 10500, 3);
        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain);
        assert syncQueue.getLongestChain().size() == 10500;
    }

    @Test
    public void testWideLongestChain() {
        List<Block> randomChain = TestUtils.getRandomAltChain(new byte[32], 0, 100, 100);
        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain);
        assert syncQueue.getLongestChain().size() == 100;
    }

    @Test
    public void testGapedLongestChain() {
        List<Block> randomChain = TestUtils.getRandomAltChain(new byte[32], 0, 100, 5);
        Iterator<Block> it = randomChain.iterator();
        while (it.hasNext()) {
            if (it.next().getHeader().getNumber() == 15) it.remove();
        }
        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain);
        assert syncQueue.getLongestChain().size() == 15; // 0 .. 14
    }

    @Test
    public void testFirstBlockGapedLongestChain() {
        List<Block> randomChain = TestUtils.getRandomAltChain(new byte[32], 0, 100, 5);
        Iterator<Block> it = randomChain.iterator();
        while (it.hasNext()) {
            if (it.next().getHeader().getNumber() == 1) it.remove();
        }
        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain);
        assert syncQueue.getLongestChain().size() == 1; // 0
    }

    @Test(expected = AssertionError.class)
    public void testZeroBlockGapedLongestChain() {
        List<Block> randomChain = TestUtils.getRandomAltChain(new byte[32], 0, 100, 5);
        Iterator<Block> it = randomChain.iterator();
        while (it.hasNext()) {
            if (it.next().getHeader().getNumber() == 0) it.remove();
        }
        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain);
        syncQueue.getLongestChain().size();
    }

    @Test
    public void testNoParentGapeLongestChain() {
        List<Block> randomChain = TestUtils.getRandomAltChain(new byte[32], 0, 100, 5);

        // Moving #15 blocks to the end to be sure it didn't trick SyncQueue
        Iterator<Block> it = randomChain.iterator();
        List<Block> blockSaver = new ArrayList<>();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getHeader().getNumber() == 15) {
                blockSaver.add(block);
                it.remove();
            }
        }
        randomChain.addAll(blockSaver);

        SyncQueueImpl syncQueue = new SyncQueueImpl(randomChain);
        // We still have linked chain
        assert syncQueue.getLongestChain().size() == 100;


        List<Block> randomChain2 = TestUtils.getRandomAltChain(new byte[32], 0, 100, 5);

        Iterator<Block> it2 = randomChain2.iterator();
        List<Block> blockSaver2 = new ArrayList<>();
        while (it2.hasNext()) {
            Block block = it2.next();
            if (block.getHeader().getNumber() == 15) {
                blockSaver2.add(block);
            }
        }

        // Removing #15 blocks
        for (int i = 0; i < 5; ++i) {
            randomChain.remove(randomChain.size() - 1);
        }
        // Adding wrong #15 blocks
        assert blockSaver2.size() == 5;
        randomChain.addAll(blockSaver2);

        assert new SyncQueueImpl(randomChain).getLongestChain().size() == 15; // 0 .. 14
    }

    @Test
    public void testValidateChain() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 100);
        SyncQueueImpl queue = new SyncQueueImpl(randomChain);
        byte[] nodeId = randomPeerId();

        List<Block> chain = TestUtils.getRandomChain(randomChain.get(randomChain.size() - 1).getHash(),
                100, SyncQueueImpl.MAX_CHAIN_LEN - 100 - 1);
        queue.addHeaders(createHeadersFromBlocks(chain, nodeId));

        List<SyncQueueImpl.HeaderElement> longestChain = queue.getLongestChain();

        // no validator is set
        assertEquals(SyncQueueIfc.ValidatedHeaders.Empty, queue.validateChain(longestChain));

        chain = TestUtils.getRandomChain(chain.get(chain.size() - 1).getHash(),
                SyncQueueImpl.MAX_CHAIN_LEN - 1, SyncQueueImpl.MAX_CHAIN_LEN);
        queue.addHeaders(createHeadersFromBlocks(chain, nodeId));

        chain = TestUtils.getRandomChain(chain.get(chain.size() - 1).getHash(),
                2 * SyncQueueImpl.MAX_CHAIN_LEN - 1, SyncQueueImpl.MAX_CHAIN_LEN);

        // the chain is invalid
        queue.withParentHeaderValidator(RedRule);
        SyncQueueIfc.ValidatedHeaders ret = queue.addHeadersAndValidate(createHeadersFromBlocks(chain, nodeId));
        assertFalse(ret.isValid());
        assertArrayEquals(nodeId, ret.getNodeId());

        // the chain is valid
        queue.withParentHeaderValidator(GreenRule);
        ret = queue.addHeadersAndValidate(createHeadersFromBlocks(chain, nodeId));
        assertEquals(SyncQueueIfc.ValidatedHeaders.Empty, ret);
    }

    @Test
    public void testEraseChain() {
        List<Block> randomChain = TestUtils.getRandomChain(new byte[32], 0, 1024);
        SyncQueueImpl queue = new SyncQueueImpl(randomChain);

        List<Block> chain1 = TestUtils.getRandomChain(randomChain.get(randomChain.size() - 1).getHash(),
                1024, SyncQueueImpl.MAX_CHAIN_LEN / 2);
        queue.addHeaders(createHeadersFromBlocks(chain1, randomPeerId()));

        List<Block> chain2 = TestUtils.getRandomChain(randomChain.get(randomChain.size() - 1).getHash(),
                1024, SyncQueueImpl.MAX_CHAIN_LEN / 2 - 1);
        queue.addHeaders(createHeadersFromBlocks(chain2, randomPeerId()));

        List<SyncQueueImpl.HeaderElement> longestChain = queue.getLongestChain();
        long maxNum = longestChain.get(longestChain.size() - 1).header.getNumber();
        assertEquals(1024 + SyncQueueImpl.MAX_CHAIN_LEN / 2 - 1, maxNum);
        assertEquals(1024 + SyncQueueImpl.MAX_CHAIN_LEN / 2 - 1, queue.getHeadersCount());

        List<Block> chain3 = TestUtils.getRandomChain(chain1.get(chain1.size() - 1).getHash(),
                1024 + SyncQueueImpl.MAX_CHAIN_LEN / 2, SyncQueueImpl.MAX_CHAIN_LEN / 10);
        // the chain is invalid and must be erased
        queue.withParentHeaderValidator(new DependentBlockHeaderRule() {
            @Override
            public boolean validate(BlockHeader header, BlockHeader dependency) {
                // chain2 should become best after erasing
                return header.getNumber() < chain2.get(chain2.size() - 2).getNumber();
            }
        });
        queue.addHeadersAndValidate(createHeadersFromBlocks(chain3, randomPeerId()));

        longestChain = queue.getLongestChain();
        assertEquals(maxNum - 1, queue.getHeadersCount());
        assertEquals(chain2.get(chain2.size() - 1).getHeader(),
                longestChain.get(longestChain.size() - 1).header.getHeader());
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
            SyncQueueIfc.HeadersRequest headersRequest = syncQueue.requestHeaders(DEFAULT_REQUEST_LEN, 1, Integer.MAX_VALUE).iterator().next();
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
        boolean returnGenesis;

        public Peer(List<Block> chain) {
            this(chain, true);
        }

        public Peer(List<Block> chain, boolean returnGenesis) {
            this.returnGenesis = returnGenesis;
            this.chain = chain;
            for (Block block : chain) {
                blocks.put(new ByteArrayWrapper(block.getHash()), block);
            }
        }

        public List<BlockHeader> getHeaders(long startBlockNum, int count, boolean reverse) {
            return getHeaders(startBlockNum, count, reverse, 0);
        }

        public List<BlockHeader> getHeaders(SyncQueueIfc.HeadersRequest req) {
            if (req.getHash() == null) {
                return getHeaders(req.getStart(), req.getCount(), req.isReverse(), req.getStep());
            } else {
                Block block = blocks.get(new ByteArrayWrapper(req.getHash()));
                if (block == null) return Collections.emptyList();
                return getHeaders(block.getNumber(), req.getCount(), req.isReverse(), req.getStep());
            }
        }

        public List<BlockHeader> getRandomHeaders(int count) {
            List<BlockHeader> ret = new ArrayList<>();
            Random rnd = new Random();
            for (int i = 0; i < count; i++) {
                ret.add(chain.get(rnd.nextInt(chain.size())).getHeader());
            }
            return ret;
        }


        public List<BlockHeader> getHeaders(long startBlockNum, int count, boolean reverse, int step) {
            step = step == 0 ? 1 : step;

            List<BlockHeader> ret = new ArrayList<>();
            int i = (int) startBlockNum;
            for(; count-- > 0 && i >= (returnGenesis ? 0 : 1)  && i <= chain.get(chain.size() - 1).getNumber();
                i += reverse ? -step : step) {

                ret.add(chain.get(i).getHeader());

            }

//            step = step == 0 ? 1 : step;
//
//            if (reverse) {
//                startBlockNum = startBlockNum - (count - 1 ) * step;
//            }
//
//            startBlockNum = Math.max(startBlockNum, chain.get(0).getNumber());
//            startBlockNum = Math.min(startBlockNum, chain.get(chain.size() - 1).getNumber());
//            long endBlockNum = startBlockNum + (count - 1) * step;
//            endBlockNum = Math.max(endBlockNum, chain.get(0).getNumber());
//            endBlockNum = Math.min(endBlockNum, chain.get(chain.size() - 1).getNumber());
//            List<BlockHeader> ret = new ArrayList<>();
//            int startIdx = (int) (startBlockNum - chain.get(0).getNumber());
//            for (int i = startIdx; i < startIdx + (endBlockNum - startBlockNum + 1); i+=step) {
//                ret.add(chain.get(i).getHeader());
//            }
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

    static final DependentBlockHeaderRule RedRule = new DependentBlockHeaderRule() {
        @Override
        public boolean validate(BlockHeader header, BlockHeader dependency) {
            return false;
        }
    };

    static final DependentBlockHeaderRule GreenRule = new DependentBlockHeaderRule() {
        @Override
        public boolean validate(BlockHeader header, BlockHeader dependency) {
            return true;
        }
    };
}
