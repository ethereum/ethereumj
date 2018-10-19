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
import org.ethereum.util.blockchain.EtherUtil;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SameParameterValue")
public class ConstantinopleConfigTest {

    private static final byte[] FAKE_HASH = {11, 12};
    private static final ConstantinopleConfig constantinopleConfig = new ConstantinopleConfig(new TestBlockchainConfig());

    @Test
    public void testRelatedEip() throws Exception {
        // Byzantium
        assertTrue(constantinopleConfig.eip198());
        assertTrue(constantinopleConfig.eip206());
        assertTrue(constantinopleConfig.eip211());
        assertTrue(constantinopleConfig.eip212());
        assertTrue(constantinopleConfig.eip213());
        assertTrue(constantinopleConfig.eip214());
        assertTrue(constantinopleConfig.eip658());

        // Constantinople
        assertTrue(constantinopleConfig.eip145());
        assertTrue(constantinopleConfig.eip1014());
        assertTrue(constantinopleConfig.eip1052());
        assertTrue(constantinopleConfig.eip1283());

        ByzantiumConfig byzantiumConfig = new ByzantiumConfig(new TestBlockchainConfig());

        // Constantinople eips in Byzantium
        assertFalse(byzantiumConfig.eip145());
        assertFalse(byzantiumConfig.eip1014());
        assertFalse(byzantiumConfig.eip1052());
        assertFalse(byzantiumConfig.eip1283());
    }


    @Test
    public void testDifficultyWithoutExplosion() throws Exception {
        BlockHeader parent = new BlockHeaderBuilder(FAKE_HASH, 0L, 1_000_000).build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, -1).build();

        BigInteger difficulty = constantinopleConfig.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(1_000_976), difficulty);
    }

    @Test
    public void testDifficultyAdjustedForParentBlockHavingUncles() throws Exception {
        BlockHeader parent = new BlockHeaderBuilder(FAKE_HASH, 0L, 0)
                .withTimestamp(0L)
                .withUncles(new byte[]{1, 2})
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, 0)
                .withTimestamp(9L)
                .build();
        assertEquals(1, constantinopleConfig.getCalcDifficultyMultiplier(current, parent).intValue());
    }

    @Test
    public void testDifficultyWithExplosionShouldBeImpactedByBlockTimestamp() throws Exception {

        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 8_388_608)
                .withTimestamp(0)
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 2_500_001, 8_388_608)
                .withTimestamp(10 * 60) // 10 minutes later, longer time: lowers difficulty
                .build();

        BigInteger difficulty = constantinopleConfig.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(8126464), difficulty);


        parent = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 8_388_608)
                .withTimestamp(0)
                .build();
        current = new BlockHeaderBuilder(parent.getHash(), 2_500_001, 8_388_608)
                .withTimestamp(5) // 5 seconds later, shorter time: higher difficulty
                .build();

        difficulty = constantinopleConfig.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(8396800), difficulty);
    }

    @Test
    public void testDifficultyAboveBlock5MShouldTriggerExplosion() throws Exception {

        int parentDifficulty = 268_435_456;
        BlockHeader parent = new BlockHeaderBuilder(FAKE_HASH, 6_000_000, parentDifficulty).build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 6_000_001, -1).build();
        int actualDifficulty = constantinopleConfig.calcDifficulty(current, parent).intValue();
        int differenceWithoutExplosion = actualDifficulty - parentDifficulty;
        assertEquals(262_400, differenceWithoutExplosion);

        parent = new BlockHeaderBuilder(FAKE_HASH, 7_000_000, parentDifficulty).build();
        current = new BlockHeaderBuilder(parent.getHash(), 7_000_001, -1).build();
        actualDifficulty = constantinopleConfig.calcDifficulty(current, parent).intValue();
        differenceWithoutExplosion = actualDifficulty - parentDifficulty;
        assertEquals(524_288, differenceWithoutExplosion);

        parent = new BlockHeaderBuilder(FAKE_HASH, 8_000_000, parentDifficulty).build();
        current = new BlockHeaderBuilder(parent.getHash(), 8_000_001, -1).build();
        actualDifficulty = constantinopleConfig.calcDifficulty(current, parent).intValue();
        differenceWithoutExplosion = actualDifficulty - parentDifficulty;
        assertEquals(268_697_600, differenceWithoutExplosion);  // Explosion!
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
        BlockHeader parent = new BlockHeaderBuilder(FAKE_HASH, 0L, 0)
                .withTimestamp(parentBlockTimestamp)
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, 0)
                .withTimestamp(curBlockTimestamp)
                .build();
        assertEquals(expectedMultiplier, constantinopleConfig.getCalcDifficultyMultiplier(current, parent).intValue());
    }


    @Test
    public void testExplosionChanges() throws Exception {

        BlockHeader beforePauseBlock = new BlockHeaderBuilder(FAKE_HASH, 4_000_000, 0).build();
        assertEquals(-2, constantinopleConfig.getExplosion(beforePauseBlock, null));

        BlockHeader endOfIceAge = new BlockHeaderBuilder(FAKE_HASH, 5_000_000, 0).build();
        assertEquals(-2, constantinopleConfig.getExplosion(endOfIceAge, null));

        BlockHeader startExplodingBlock = new BlockHeaderBuilder(FAKE_HASH, 5_200_000, 0).build();
        assertEquals(0, constantinopleConfig.getExplosion(startExplodingBlock, null));

        startExplodingBlock = new BlockHeaderBuilder(FAKE_HASH, 6_000_000, 0).build();
        assertEquals(8, constantinopleConfig.getExplosion(startExplodingBlock, null));

        startExplodingBlock = new BlockHeaderBuilder(FAKE_HASH, 8_000_000, 0).build();
        assertEquals(28, constantinopleConfig.getExplosion(startExplodingBlock, null));
    }

    @Test
    public void testBlockReward() throws Exception {
        ConstantinopleConfig constantinopleConfig2 = new ConstantinopleConfig(new TestBlockchainConfig() {
            @Override
            public Constants getConstants() {
                return new ConstantsAdapter(super.getConstants()) {
                    @Override
                    public BigInteger getBLOCK_REWARD() {
                        // Make sure ConstantinopleConfig is not using parent's block reward
                        return BigInteger.TEN;
                    }
                };
            }
        });
        assertEquals(EtherUtil.convert(2, EtherUtil.Unit.ETHER), constantinopleConfig2.getConstants().getBLOCK_REWARD());
    }
}