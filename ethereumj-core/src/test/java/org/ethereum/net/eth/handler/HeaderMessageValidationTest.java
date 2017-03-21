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
package org.ethereum.net.eth.handler;

import org.ethereum.core.BlockHeader;
import org.ethereum.net.eth.message.BlockHeadersMessage;
import org.ethereum.net.eth.message.GetBlockHeadersMessage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Testing {@link org.ethereum.net.eth.handler.Eth62#isValid(BlockHeadersMessage, GetBlockHeadersMessageWrapper)}
 */
public class HeaderMessageValidationTest {

    private byte[] EMPTY_ARRAY = new byte[0];

    private class Eth62Tester extends Eth62 {

        boolean blockHeaderMessageValid(BlockHeadersMessage msg, GetBlockHeadersMessageWrapper request) {
            return super.isValid(msg, request);
        }
    }

    private Eth62Tester ethHandler;

    public HeaderMessageValidationTest() {
        ethHandler = new Eth62Tester();
    }


    @Test
    public void testSingleBlockResponse() {
        long blockNumber = 0L;
        BlockHeader blockHeader = new BlockHeader(new byte[] {11, 12}, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                EMPTY_ARRAY, blockNumber, EMPTY_ARRAY, 1L, 2L, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
        List<BlockHeader> blockHeaders = new ArrayList<>();
        blockHeaders.add(blockHeader);
        BlockHeadersMessage msg = new BlockHeadersMessage(blockHeaders);

        byte[] hash = blockHeader.getHash();
        // Block number doesn't matter when hash is provided in request
        GetBlockHeadersMessage requestHash = new GetBlockHeadersMessage(123L, hash, 1, 0, false);
        GetBlockHeadersMessageWrapper wrapperHash = new GetBlockHeadersMessageWrapper(requestHash);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperHash);

        // Getting same with block number request
        GetBlockHeadersMessage requestNumber = new GetBlockHeadersMessage(blockNumber, null, 1, 0, false);
        GetBlockHeadersMessageWrapper wrapperNumber = new GetBlockHeadersMessageWrapper(requestNumber);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperNumber);

        // Getting same with reverse request
        GetBlockHeadersMessage requestReverse = new GetBlockHeadersMessage(blockNumber, null, 1, 0, true);
        GetBlockHeadersMessageWrapper wrapperReverse = new GetBlockHeadersMessageWrapper(requestReverse);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperReverse);

        // Getting same with skip request
        GetBlockHeadersMessage requestSkip = new GetBlockHeadersMessage(blockNumber, null, 1, 10, false);
        GetBlockHeadersMessageWrapper wrapperSkip = new GetBlockHeadersMessageWrapper(requestSkip);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperSkip);
    }

    @Test
    public void testFewBlocksNoSkip() {
        List<BlockHeader> blockHeaders = new ArrayList<>();

        long blockNumber1 = 0L;
        BlockHeader blockHeader1 = new BlockHeader(new byte[] {11, 12}, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                EMPTY_ARRAY, blockNumber1, EMPTY_ARRAY, 1L, 2L, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
        byte[] hash1 = blockHeader1.getHash();
        blockHeaders.add(blockHeader1);

        long blockNumber2 = 1L;
        BlockHeader blockHeader2 = new BlockHeader(hash1, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                EMPTY_ARRAY, blockNumber2, EMPTY_ARRAY, 1L, 2L, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
        byte[] hash2 = blockHeader2.getHash();
        blockHeaders.add(blockHeader2);

        BlockHeadersMessage msg = new BlockHeadersMessage(blockHeaders);

        // Block number doesn't matter when hash is provided in request
        GetBlockHeadersMessage requestHash = new GetBlockHeadersMessage(123L, hash1, 2, 0, false);
        GetBlockHeadersMessageWrapper wrapperHash = new GetBlockHeadersMessageWrapper(requestHash);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperHash);

        // Getting same with block number request
        GetBlockHeadersMessage requestNumber = new GetBlockHeadersMessage(blockNumber1, null, 2, 0, false);
        GetBlockHeadersMessageWrapper wrapperNumber = new GetBlockHeadersMessageWrapper(requestNumber);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperNumber);

        // Reverse list
        Collections.reverse(blockHeaders);
        GetBlockHeadersMessage requestReverse = new GetBlockHeadersMessage(blockNumber2, null, 2, 0, true);
        GetBlockHeadersMessageWrapper wrapperReverse = new GetBlockHeadersMessageWrapper(requestReverse);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperReverse);
    }

    @Test
    public void testFewBlocksWithSkip() {
        List<BlockHeader> blockHeaders = new ArrayList<>();

        long blockNumber1 = 0L;
        BlockHeader blockHeader1 = new BlockHeader(new byte[] {11, 12}, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                EMPTY_ARRAY, blockNumber1, EMPTY_ARRAY, 1L, 2L, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
        blockHeaders.add(blockHeader1);

        long blockNumber2 = 16L;
        BlockHeader blockHeader2 = new BlockHeader(new byte[] {12, 13}, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                EMPTY_ARRAY, blockNumber2, EMPTY_ARRAY, 1L, 2L, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
        blockHeaders.add(blockHeader2);

        long blockNumber3 = 32L;
        BlockHeader blockHeader3 = new BlockHeader(new byte[] {14, 15}, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                EMPTY_ARRAY, blockNumber3, EMPTY_ARRAY, 1L, 2L, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
        blockHeaders.add(blockHeader3);

        BlockHeadersMessage msg = new BlockHeadersMessage(blockHeaders);

        GetBlockHeadersMessage requestNumber = new GetBlockHeadersMessage(blockNumber1, null, 3, 15, false);
        GetBlockHeadersMessageWrapper wrapperNumber = new GetBlockHeadersMessageWrapper(requestNumber);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperNumber);

        // Requesting more than we have
        GetBlockHeadersMessage requestMore = new GetBlockHeadersMessage(blockNumber1, null, 4, 15, false);
        GetBlockHeadersMessageWrapper wrapperMore = new GetBlockHeadersMessageWrapper(requestMore);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperMore);

        // Reverse list
        Collections.reverse(blockHeaders);
        GetBlockHeadersMessage requestReverse = new GetBlockHeadersMessage(blockNumber3, null, 3, 15, true);
        GetBlockHeadersMessageWrapper wrapperReverse = new GetBlockHeadersMessageWrapper(requestReverse);
        assert ethHandler.blockHeaderMessageValid(msg, wrapperReverse);
    }
}
