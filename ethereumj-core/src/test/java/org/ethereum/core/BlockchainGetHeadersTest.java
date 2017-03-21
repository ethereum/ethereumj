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
package org.ethereum.core;

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.BlockStoreDummy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Testing {@link BlockchainImpl#getListOfHeadersStartFrom(BlockIdentifier, int, int, boolean)}
 */
public class BlockchainGetHeadersTest {

    private class BlockStoreMock extends BlockStoreDummy {

        private List<Block> dummyBlocks = new ArrayList<>();

        public BlockStoreMock() {
            byte [] emptyArray = new byte[0];
            byte [] recentHash = emptyArray;

            for (long i = 0; i < 10; ++i) {
                BlockHeader blockHeader = new BlockHeader(recentHash, emptyArray, emptyArray, emptyArray, emptyArray,
                        i, emptyArray, 0L, 0L, emptyArray, emptyArray, emptyArray);
                recentHash = blockHeader.getHash();
                Block block = new Block(blockHeader, new ArrayList<Transaction>(), new ArrayList<BlockHeader>());
                dummyBlocks.add(block);
            }
        }

        @Override
        public Block getBlockByHash(byte[] hash) {
            for (Block block: dummyBlocks) {
                if (Arrays.equals(block.getHash(), hash)) {
                    return block;
                }
            }

            return null;
        }

        @Override
        public Block getChainBlockByNumber(long blockNumber) {
            return blockNumber < dummyBlocks.size() ? dummyBlocks.get((int) blockNumber) : null;
        }

        @Override
        public List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty) {
            List<BlockHeader> headers = new ArrayList<>();
            Block start = getBlockByHash(hash);
            if (start != null) {
                long i = start.getNumber();
                while (i >= 0 && headers.size() < qty) {
                    headers.add(getChainBlockByNumber(i).getHeader());
                    --i;
                }
            }

            return headers;
        }

        @Override
        public Block getBestBlock() {
            return dummyBlocks.get(dummyBlocks.size() - 1);
        }
    }


    private class BlockchainImplTester extends BlockchainImpl {

        public BlockchainImplTester() {
            blockStore = new BlockStoreMock();
            setRepository(new RepositoryRoot(new HashMapDB<byte[]>()));
            setBestBlock(blockStore.getChainBlockByNumber(9));
        }
    }


    private BlockchainImpl blockchain;

    public BlockchainGetHeadersTest() {
        blockchain = new BlockchainImplTester();
    }

    @Test
    public void singleHeader() {
        // Get by number
        long blockNumber = 2L;
        BlockIdentifier identifier = new BlockIdentifier(null, blockNumber);
        List<BlockHeader> headers = blockchain.getListOfHeadersStartFrom(identifier, 0, 1, false);

        assert headers.size() == 1;
        assert headers.get(0).getNumber() == blockNumber;

        // Get by hash
        byte[] hash = headers.get(0).getHash();
        BlockIdentifier hashIdentifier = new BlockIdentifier(hash, 0L);
        List<BlockHeader> headersByHash = blockchain.getListOfHeadersStartFrom(hashIdentifier, 0, 1, false);

        assert headersByHash.size() == 1;
        assert headersByHash.get(0).getNumber() == blockNumber;

        // Reverse doesn't matter for single block
        List<BlockHeader> headersReverse = blockchain.getListOfHeadersStartFrom(hashIdentifier, 0, 1, true);
        assert headersReverse.size() == 1;
        assert headersReverse.get(0).getNumber() == blockNumber;

        // Skip doesn't matter for single block
        List<BlockHeader> headersSkip = blockchain.getListOfHeadersStartFrom(hashIdentifier, 15, 1, false);
        assert headersReverse.size() == 1;
        assert headersReverse.get(0).getNumber() == blockNumber;
    }

    @Test
    public void continuousHeaders() {
        // Get by number
        long blockNumber = 2L;
        BlockIdentifier identifier = new BlockIdentifier(null, blockNumber);
        List<BlockHeader> headers = blockchain.getListOfHeadersStartFrom(identifier, 0, 3, false);

        assert headers.size() == 3;
        assert headers.get(0).getNumber() == blockNumber;
        assert headers.get(1).getNumber() == blockNumber + 1;
        assert headers.get(2).getNumber() == blockNumber + 2;

        List<BlockHeader> headersReverse = blockchain.getListOfHeadersStartFrom(identifier, 0, 3, true);
        assert headersReverse.size() == 3;
        assert headersReverse.get(0).getNumber() == blockNumber;
        assert headersReverse.get(1).getNumber() == blockNumber - 1;
        assert headersReverse.get(2).getNumber() == blockNumber - 2;

        // Requesting more than we have
        BlockIdentifier identifierMore = new BlockIdentifier(null, 8L);
        List<BlockHeader> headersMore = blockchain.getListOfHeadersStartFrom(identifierMore, 0, 3, false);
        assert headersMore.size() == 2;
        assert headersMore.get(0).getNumber() == 8L;
        assert headersMore.get(1).getNumber() == 9L;
    }

    @Test
    public void gapedHeaders() {
        int skip = 2;
        BlockIdentifier identifier = new BlockIdentifier(null, 2L);
        List<BlockHeader> headers = blockchain.getListOfHeadersStartFrom(identifier, skip, 3, false);

        assert headers.size() == 3;
        assert headers.get(0).getNumber() == 2L;
        assert headers.get(1).getNumber() == 5L; // 2, [3, 4], 5 - skipping []
        assert headers.get(2).getNumber() == 8L; // 5, [6, 7], 8 - skipping []

        // Same for reverse
        BlockIdentifier identifierReverse = new BlockIdentifier(null, 8L);
        List<BlockHeader> headersReverse = blockchain.getListOfHeadersStartFrom(identifierReverse, skip, 3, true);
        assert headersReverse.size() == 3;
        assert headersReverse.get(0).getNumber() == 8L;
        assert headersReverse.get(1).getNumber() == 5L;
        assert headersReverse.get(2).getNumber() == 2L;

        // Requesting more than we have
        BlockIdentifier identifierMore = new BlockIdentifier(null, 8L);
        List<BlockHeader> headersMore = blockchain.getListOfHeadersStartFrom(identifierMore, skip, 3, false);
        assert headersMore.size() == 1;
        assert headersMore.get(0).getNumber() == 8L;
    }
}
