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

import org.apache.commons.lang3.StringUtils;
import org.ethereum.core.BlockHeader;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

@SuppressWarnings("SameParameterValue")
public class ETCFork3MTest {
    /**
     * Ethereum Classic's Chain ID should be '61' according to
     * https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md
     */
    @Test
    public void textPredefinedChainId() throws Exception {
        ETCFork3M etcFork3M = new ETCFork3M(new TestBlockchainConfig());
        assertEquals(61, (int) etcFork3M.getChainId());
    }

    @Test
    public void testRelatedEip() throws Exception {
        TestBlockchainConfig parent = new TestBlockchainConfig();

        ETCFork3M etcFork3M = new ETCFork3M(parent);
        // Inherited from parent
        assertFalse(etcFork3M.eip198());
        assertFalse(etcFork3M.eip206());
        assertFalse(etcFork3M.eip211());
        assertFalse(etcFork3M.eip212());
        assertFalse(etcFork3M.eip213());
        assertFalse(etcFork3M.eip214());
        assertFalse(etcFork3M.eip658());

        // Always false
        assertFalse(etcFork3M.eip161());

        /*
         * By flipping parent's eip values, we assert that
         * ETCFork3M delegates respective eip calls to parent.
         */
        parent.enableAllEip();

        // Inherited from parent
        assertTrue(etcFork3M.eip198());
        assertFalse(etcFork3M.eip206());
        assertFalse(etcFork3M.eip211());
        assertTrue(etcFork3M.eip212());
        assertTrue(etcFork3M.eip213());
        assertFalse(etcFork3M.eip214());
        assertFalse(etcFork3M.eip658());

        // Always false
        assertFalse(etcFork3M.eip161());
    }


    @Test
    public void testDifficultyWithoutExplosion() throws Exception {
        ETCFork3M etcFork3M = new ETCFork3M(new TestBlockchainConfig());

        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 0L, 1_000_000).build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, -1).build();

        BigInteger difficulty = etcFork3M.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(269435944), difficulty);
    }

    @Test
    public void testDifficultyWithExplosionShouldBeImpactedByBlockTimestamp() throws Exception {
        ETCFork3M etcFork3M = new ETCFork3M(new TestBlockchainConfig());

        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 8_388_608)
                .withTimestamp(0)
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 2_500_001, -1)
                .withTimestamp(10 * 60) // 10 minutes later, longer time: lowers difficulty
                .build();

        BigInteger difficulty = etcFork3M.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(276582400), difficulty);


        parent = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 8_388_608)
                .withTimestamp(0)
                .build();
        current = new BlockHeaderBuilder(parent.getHash(), 2_500_001, -1)
                .withTimestamp(5) // 5 seconds later, shorter time: higher difficulty
                .build();

        difficulty = etcFork3M.calcDifficulty(current, parent);
        assertEquals(BigInteger.valueOf(276828160), difficulty);
    }

    @Test
    public void testDifficultyAboveBlock5MShouldTriggerExplosion() throws Exception {
        ETCFork3M etcFork3M = new ETCFork3M(new TestBlockchainConfig());

        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 5_000_000, 268_435_456).build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 5_000_001, -1).build();
        assertEquals(BigInteger.valueOf(537_001_984), etcFork3M.calcDifficulty(current, parent));

        parent = new BlockHeaderBuilder(new byte[]{11, 12}, 5_199_999, 1_073_872_896).build();
        current = new BlockHeaderBuilder(parent.getHash(), 5_200_000, 1_073_872_896).build();
        assertEquals(BlockHeaderBuilder.parse("2,148,139,072"), etcFork3M.calcDifficulty(current, parent));
    }

    @Test
    @SuppressWarnings("PointlessArithmeticExpression")
    public void testCalcDifficultyMultiplier() throws Exception {
        // Note; timestamps are in seconds
        assertCalcDifficultyMultiplier(0L, 1L, 1);
        assertCalcDifficultyMultiplier(0L, 5, 1); // 5 seconds
        assertCalcDifficultyMultiplier(0L, 1 * 10, 0); // 10 seconds
        assertCalcDifficultyMultiplier(0L, 2 * 10, -1); // 20 seconds
        assertCalcDifficultyMultiplier(0L, 10 * 10, -9); // 100 seconds
        assertCalcDifficultyMultiplier(0L, 60 * 10, -59); // 10 mins
        assertCalcDifficultyMultiplier(0L, 60 * 12, -71); // 12 mins
    }

    private void assertCalcDifficultyMultiplier(long parentBlockTimestamp, long curBlockTimestamp, int expectedMultiplier) {
        ETCFork3M etcFork3M = new ETCFork3M(new TestBlockchainConfig());
        BlockHeader parent = new BlockHeaderBuilder(new byte[]{11, 12}, 0L, 0)
                .withTimestamp(parentBlockTimestamp)
                .build();
        BlockHeader current = new BlockHeaderBuilder(parent.getHash(), 1L, 0)
                .withTimestamp(curBlockTimestamp)
                .build();
        assertEquals(expectedMultiplier, etcFork3M.getCalcDifficultyMultiplier(current, parent).intValue());
    }


    /**
     * https://github.com/ethereumproject/ECIPs/blob/master/ECIPs/ECIP-1010.md
     *
     <pre>
     if (block.number < pause_block) {
        explosion = (block.number / 100000) - 2
     } else if (block.number < cont_block) {
        explosion = fixed_diff
     } else { // block.number >= cont_block
        explosion = (block.number / 100000) - delay - 2
     }
     </pre>

     * Expected explosion values would be:
     <pre>
     Block 3,000,000 == 2**28 == 268,435,456
     Block 4,000,000 == 2**28 == 268,435,456
     Block 5,000,000 == 2**28 == 268,435,456
     Block 5,200,000 == 2**30 == 1 TH
     Block 6,000,000 == 2**38 == 274 TH
     </pre>
     Where the explosion is the value after '**'.
     */
    @Test
    public void testEcip1010ExplosionChanges() throws Exception {
        ETCFork3M etcFork3M = new ETCFork3M(new TestBlockchainConfig());

        /*
         * Technically, a block number < 3_000_000 should result in an explosion < fixed_diff, or explosion < 28
         *
         * Block number 3_000_000 occurred on Jan 15, 2017. The ETCFork3M configuration was committed a day after. It
         * is therefor not necessary to have block.number < pause_block be implemented
         */
        BlockHeader beforePauseBlock = new BlockHeaderBuilder(new byte[]{11, 12}, 2_500_000, 0).build();
        int unimplementedPrePauseBlockExplosion = 28;
        assertEquals(unimplementedPrePauseBlockExplosion, etcFork3M.getExplosion(beforePauseBlock, null));

        BlockHeader endOfIceAge = new BlockHeaderBuilder(new byte[]{11, 12}, 5_000_000, 0).build();
        assertEquals(28, etcFork3M.getExplosion(endOfIceAge, null));

        BlockHeader startExplodingBlock = new BlockHeaderBuilder(new byte[]{11, 12}, 5_200_000, 0).build();
        assertEquals(30, etcFork3M.getExplosion(startExplodingBlock, null));

        startExplodingBlock = new BlockHeaderBuilder(new byte[]{11, 12}, 6_000_000, 0).build();
        assertEquals(38, etcFork3M.getExplosion(startExplodingBlock, null));
    }
}