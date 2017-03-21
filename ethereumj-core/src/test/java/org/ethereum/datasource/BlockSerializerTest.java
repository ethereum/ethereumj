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
package org.ethereum.datasource;

import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.IndexedBlockStore.BlockInfo;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Test for {@link IndexedBlockStore.BLOCK_INFO_SERIALIZER}
 */
public class BlockSerializerTest {

    private static final Random rnd = new Random();

    private List<BlockInfo> generateBlockInfos(int count) {
        List<BlockInfo> blockInfos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BlockInfo blockInfo = new BlockInfo();
            blockInfo.setHash(sha3(ByteUtil.intToBytes(i)));
            blockInfo.setCummDifficulty(BigInteger.probablePrime(512, rnd));
            blockInfo.setMainChain(rnd.nextBoolean());
            blockInfos.add(blockInfo);
        }

        return blockInfos;
    }

    @Test
    public void testTest() {
        List<BlockInfo> blockInfoList = generateBlockInfos(100);
        byte[] data = IndexedBlockStore.BLOCK_INFO_SERIALIZER.serialize(blockInfoList);
        System.out.printf("Blocks total byte size: %s%n", data.length);
        List<BlockInfo> blockInfoList2  = IndexedBlockStore.BLOCK_INFO_SERIALIZER.deserialize(data);

        assert blockInfoList.size() == blockInfoList2.size();
        for (int i = 0; i < blockInfoList2.size(); i++) {
            assert FastByteComparisons.equal(blockInfoList2.get(i).getHash(), blockInfoList.get(i).getHash());
            assert blockInfoList2.get(i).getCummDifficulty().compareTo(blockInfoList.get(i).getCummDifficulty()) == 0;
            assert blockInfoList2.get(i).isMainChain() == blockInfoList.get(i).isMainChain();
        }
    }

    @Test
    @Ignore
    public void testTime() {
        int BLOCKS = 100;
        int PASSES = 10_000;
        List<BlockInfo> blockInfoList = generateBlockInfos(BLOCKS);

        long s = System.currentTimeMillis();
        for (int i = 0; i < PASSES; i++) {
            byte[] data = IndexedBlockStore.BLOCK_INFO_SERIALIZER.serialize(blockInfoList);
            List<BlockInfo> blockInfoList2 = IndexedBlockStore.BLOCK_INFO_SERIALIZER.deserialize(data);
        }
        long e = System.currentTimeMillis();

        System.out.printf("Serialize/deserialize blocks per 1 ms: %s%n", PASSES * BLOCKS / (e - s));
    }

    @Test(expected = RuntimeException.class)
    public void testNullCummDifficulty() {
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setMainChain(true);
        blockInfo.setCummDifficulty(null);
        blockInfo.setHash(new byte[0]);
        byte[] data = IndexedBlockStore.BLOCK_INFO_SERIALIZER.serialize(Collections.singletonList(blockInfo));
        List<BlockInfo> blockInfos = IndexedBlockStore.BLOCK_INFO_SERIALIZER.deserialize(data);
    }

    @Test(expected = RuntimeException.class)
    public void testNegativeCummDifficulty() {
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setMainChain(true);
        blockInfo.setCummDifficulty(BigInteger.valueOf(-1));
        blockInfo.setHash(new byte[0]);
        byte[] data = IndexedBlockStore.BLOCK_INFO_SERIALIZER.serialize(Collections.singletonList(blockInfo));
        List<BlockInfo> blockInfos = IndexedBlockStore.BLOCK_INFO_SERIALIZER.deserialize(data);
    }

    @Test
    public void testZeroCummDifficultyEmptyHash() {
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setMainChain(true);
        blockInfo.setCummDifficulty(BigInteger.ZERO);
        blockInfo.setHash(new byte[0]);
        byte[] data = IndexedBlockStore.BLOCK_INFO_SERIALIZER.serialize(Collections.singletonList(blockInfo));
        List<BlockInfo> blockInfos = IndexedBlockStore.BLOCK_INFO_SERIALIZER.deserialize(data);
        assert blockInfos.size() == 1;
        BlockInfo actualBlockInfo = blockInfos.get(0);
        assert actualBlockInfo.isMainChain();
        assert actualBlockInfo.getCummDifficulty().compareTo(BigInteger.ZERO) == 0;
        assert actualBlockInfo.getHash().length == 0;
    }
}
