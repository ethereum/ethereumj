/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
 *
 *
 */

package org.ethereum.config.blockchain;

import org.ethereum.config.Constants;
import org.ethereum.config.ConstantsAdapter;
import org.ethereum.core.BlockHeader;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.*;

@SuppressWarnings("SameParameterValue")
public class ByzantiumConfigTest {

    private static final byte[] FAKE_HASH = {11, 12};

    @Test
    public void testPredefinedChainId() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());
        assertEquals(1, (int) byzantiumConfig.getChainId());
    }

    @Test
    public void testRelatedEip() throws Exception {
        TestBlockchainConfig parent = new TestBlockchainConfig();

        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(parent);
        // Inherited from parent
        assertTrue(byzantiumConfig.eip198());
        assertTrue(byzantiumConfig.eip206());
        assertTrue(byzantiumConfig.eip211());
        assertTrue(byzantiumConfig.eip212());
        assertTrue(byzantiumConfig.eip213());
        assertTrue(byzantiumConfig.eip214());
        assertTrue(byzantiumConfig.eip658());

        // Always false
        assertTrue(byzantiumConfig.eip161());
    }


    @Test
    public void testDifficultyWithoutExplosion() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());

        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 0L, 1_000_000).build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, -1).build();

        BigInteger difficulty = byzantiumConfig.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(1_000_976), difficulty);
    }

    @Test
    public void testDifficultyAdjustedForParentBlockHavingUncles() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());
        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 0L, 0)
                .withTimestamp(0L)
                .withUncles(new byte[]{1, 2})
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, 0)
                .withTimestamp(9L)
                .build();
        assertEquals(1, byzantiumConfig.getCalcDifficultyMultiplier(current, parent).intValue());
    }

    @Test
    @Ignore
    public void testEtherscanIoBlock4490790() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());

        // https://etherscan.io/block/4490788
        String parentHash = "fd9d7467e933ff2975c33ea3045ddf8773c87c4cec4e7da8de1bcc015361b38b";
        BlockHeader parent = new BlockHeaderBuilder(parentHash.getBytes(), 4490788, "1,377,255,445,606,146")
                .withTimestamp(1509827488)
                // Actually an empty list hash, so _no_ uncles
                .withUncles(Hex.decode("1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347"))
                .build();

        // https://etherscan.io/block/4490789
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 4490789, BigInteger.ZERO)
                .withTimestamp(1509827494)
                .build();

        BigInteger minimumDifficulty = byzantiumConfig.calcDifficulty(current, parent);
        assertEquals(BlockHeaderBuilder.parse("1,378,600,421,631,340"), minimumDifficulty);

        BigInteger actualDifficultyOnEtherscan = BlockHeaderBuilder.parse("1,377,927,933,620,791");
        assertTrue(actualDifficultyOnEtherscan.compareTo(minimumDifficulty) > -1);
    }

    @Test
    public void testDifficultyWithExplosionShouldBeImpactedByBlockTimestamp() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());

        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 8_388_608)
                .withTimestamp(0)
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 2_500_001, 8_388_608)
                .withTimestamp(10 * 60) // 10 minutes later, longer time: lowers difficulty
                .build();

        BigInteger difficulty = byzantiumConfig.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(8126464), difficulty);


        parent = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 8_388_608)
                .withTimestamp(0)
                .build();
        current = new BlockHeaderBuilder(parent.getHash(), 2_500_001, 8_388_608)
                .withTimestamp(5) // 5 seconds later, shorter time: higher difficulty
                .build();

        difficulty = byzantiumConfig.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(8396800), difficulty);
    }

    @Test
    public void testDifficultyAboveBlock3MShouldTriggerExplosion() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());

        int parentDifficulty = 268_435_456;
        BlockHeader parent = new BlockHeaderBuilder(FAKE_HASH, 4_000_000, parentDifficulty).build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 4_000_001, -1).build();
        int actualDifficulty = byzantiumConfig.calcDifficulty(current, parent).intValue();
        int differenceWithoutExplosion = actualDifficulty - parentDifficulty;
        assertEquals(262_400, differenceWithoutExplosion);

        parent = new BlockHeaderBuilder(FAKE_HASH, 5_000_000, parentDifficulty).build();
        current = new BlockHeaderBuilder(parent.getHash(), 5_000_001, -1).build();
        actualDifficulty = byzantiumConfig.calcDifficulty(current, parent).intValue();
        differenceWithoutExplosion = actualDifficulty - parentDifficulty;
        assertEquals(524_288, differenceWithoutExplosion);

        parent = new BlockHeaderBuilder(FAKE_HASH, 6_000_000, parentDifficulty).build();
        current = new BlockHeaderBuilder(parent.getHash(), 6_000_001, -1).build();
        actualDifficulty = byzantiumConfig.calcDifficulty(current, parent).intValue();
        differenceWithoutExplosion = actualDifficulty - parentDifficulty;
        assertEquals(268_697_600, differenceWithoutExplosion);
    }

    @Test
    @SuppressWarnings("PointlessArithmeticExpression")
    public void testCalcDifficultyMultiplier() throws Exception {
        // Note; timestamps are in seconds
        assertCalcDifficultyMultiplier(0L, 1L, 2);
        assertCalcDifficultyMultiplier(0L, 5, 2); // 5 seconds
        assertCalcDifficultyMultiplier(0L, 1 * 10, 1); // 10 seconds
        assertCalcDifficultyMultiplier(0L, 2 * 10, -0); // 20 seconds
        assertCalcDifficultyMultiplier(0L, 10 * 10, -9); // 100 seconds
        assertCalcDifficultyMultiplier(0L, 60 * 10, -64); // 10 mins
        assertCalcDifficultyMultiplier(0L, 60 * 12, -78); // 12 mins
    }

    private void assertCalcDifficultyMultiplier(long parentBlockTimestamp, long curBlockTimestamp, int expectedMultiplier) {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());
        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 0L, 0)
                .withTimestamp(parentBlockTimestamp)
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, 0)
                .withTimestamp(curBlockTimestamp)
                .build();
        assertEquals(expectedMultiplier, byzantiumConfig.getCalcDifficultyMultiplier(current, parent).intValue());
    }


    @Test
    public void testExplosionChanges() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());

        BlockHeader beforePauseBlock = new BlockHeaderBuilder(FAKE_HASH, 2_000_000, 0).build();
        assertEquals(-2, byzantiumConfig.getExplosion(beforePauseBlock, null));

        BlockHeader endOfIceAge = new BlockHeaderBuilder(FAKE_HASH, 3_000_000, 0).build();
        assertEquals(-2, byzantiumConfig.getExplosion(endOfIceAge, null));

        BlockHeader startExplodingBlock = new BlockHeaderBuilder(FAKE_HASH, 3_200_000, 0).build();
        assertEquals(0, byzantiumConfig.getExplosion(startExplodingBlock, null));

        startExplodingBlock = new BlockHeaderBuilder(FAKE_HASH, 4_000_000, 0).build();
        assertEquals(8, byzantiumConfig.getExplosion(startExplodingBlock, null));

        startExplodingBlock = new BlockHeaderBuilder(FAKE_HASH, 6_000_000, 0).build();
        assertEquals(28, byzantiumConfig.getExplosion(startExplodingBlock, null));
    }

    @Test
    public void testBlockReward() throws Exception {
        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig() {
            @Override
            public Constants getConstants() {
                return new ConstantsAdapter(super.getConstants()) {
                    @Override
                    public BigInteger getBLOCK_REWARD() {
                        // Make sure ByzantiumConfig is not using parent's block reward
                        return BigInteger.TEN;
                    }
                };
            }
        });
        assertEquals(new BigInteger("3000000000000000000"), byzantiumConfig.getConstants().getBLOCK_REWARD());
    }
}