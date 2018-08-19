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
package org.ethereum.sharding.processing.db;

import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.sharding.domain.Beacon;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 17.08.2018
 */
public class IndexedBeaconStoreTest {

    @Test
    public void testBasics() {
        StoreHelper helper = StoreHelper.newInstance();
        BeaconStore store = helper.store;

        Beacon g = createBlock(null);

        assertTrue(store.getMaxNumber() < 0);
        assertFalse(store.exist(g.getHash()));
        assertNull(store.getCanonicalHead());
        assertNull(store.getByHash(g.getHash()));
        assertEquals(BigInteger.ZERO, store.getCanonicalHeadScore());
        assertEquals(BigInteger.ZERO, store.getChainScore(g.getHash()));

        helper.saveCanonical(g);

        assertEquals(0L, store.getMaxNumber());
        assertTrue(store.exist(g.getHash()));
        assertEquals(g, store.getCanonicalHead());
        assertEquals(g, store.getByHash(g.getHash()));
        assertEquals(expectedScore(g), store.getCanonicalHeadScore());
        assertEquals(expectedScore(g), store.getChainScore(g.getHash()));

        Beacon b1 = createBlock(g);
        Beacon b2 = createBlock(b1);
        Beacon b2_ = createBlock(b1);
        Beacon b3_ = createBlock(b2);

        helper.saveCanonical(b1);
        helper.saveCanonical(b2);

        helper.saveFork(b2_);
        helper.saveFork(b3_);

        assertEquals(3L, store.getMaxNumber());
        assertTrue(store.exist(b1.getHash()));
        assertTrue(store.exist(b2.getHash()));
        assertTrue(store.exist(b2_.getHash()));
        assertTrue(store.exist(b3_.getHash()));

        assertEquals(b2, store.getCanonicalHead());
        assertEquals(b2, store.getByHash(b2.getHash()));
        assertEquals(b2_, store.getByHash(b2_.getHash()));
        assertEquals(expectedScore(g, b1, b2), store.getCanonicalHeadScore());
        assertEquals(expectedScore(g, b1, b2_, b3_), store.getChainScore(b3_.getHash()));

        assertEquals(b2, store.getCanonicalByNumber(2L));
    }

    @Test(expected = RuntimeException.class)
    public void testConsistencyBreak() {
        StoreHelper helper = StoreHelper.newInstance();

        Beacon g = createBlock(null);
        Beacon b1 = createBlock(g);
        Beacon b2_ = createBlock(b1);
        Beacon b3_ = createBlock(b2_);

        helper.saveCanonical(g);
        helper.saveCanonical(b1);
        helper.saveFork(b2_);
        helper.saveCanonical(b3_);
    }

    @Test
    public void testReorg() {
        /*

        2: 2 -> 3: 3  -> 4: 4  -> 5: 5  -> 6: 6 <-- Main
            \
             -> 3: 31 -> 4: 41
                    \          -> 5: 53
                     \        /
                      -> 4: 42 -> 5: 52
                              \
                               -> 5: 54
         */

        StoreHelper helper = StoreHelper.newInstance();
        BeaconStore store = helper.store;

        Beacon g = createBlock(null);

        Beacon b1 = createBlock(g);
        Beacon b2 = createBlock(b1);
        Beacon b3 = createBlock(b2);
        Beacon b4 = createBlock(b3);
        Beacon b5 = createBlock(b4);
        Beacon b6 = createBlock(b5);

        Beacon b31 = createBlock(b2);
        Beacon b41 = createBlock(b31);

        Beacon b42 = createBlock(b31);

        Beacon b52 = createBlock(b42);
        Beacon b53 = createBlock(b42);
        Beacon b54 = createBlock(b42);

        helper.saveCanonical(g);
        helper.saveCanonical(b1);
        helper.saveCanonical(b2);

        assertEquals(2L, store.getMaxNumber());
        assertTrue(store.exist(b1.getHash()));
        assertTrue(store.exist(b2.getHash()));

        assertEquals(b2, store.getCanonicalHead());
        assertEquals(b1, store.getByHash(b1.getHash()));
        assertEquals(b2, store.getByHash(b2.getHash()));
        assertEquals(expectedScore(g, b1, b2), store.getCanonicalHeadScore());

        // reorg to b31
        helper.saveCanonical(b3);
        helper.saveFork(b31);
        store.reorgTo(b31);
        helper.checkCanonicalHead(b31, expectedScore(g, b1, b2, b31));

        // reorg to b4
        helper.saveFork(b4);
        store.reorgTo(b4);
        helper.checkCanonicalHead(b4, expectedScore(g, b1, b2, b3, b4));

        // save fork block
        helper.saveFork(b41);
        helper.checkCanonicalHead(b4, expectedScore(g, b1, b2, b3, b4));

        // reorg to b42
        helper.saveFork(b42);
        store.reorgTo(b42);
        helper.checkCanonicalHead(b42, expectedScore(g, b1, b2, b31, b42));

        // save b52 as canonical block
        helper.saveCanonical(b52);
        helper.checkCanonicalHead(b52, expectedScore(g, b1, b2, b31, b42, b52));

        // reorg to b5
        helper.saveFork(b5);
        store.reorgTo(b5);
        helper.checkCanonicalHead(b5, expectedScore(g, b1, b2, b3, b4, b5));

        // store b53, b6, b54
        helper.saveFork(b53);
        helper.checkCanonicalHead(b5, expectedScore(g, b1, b2, b3, b4, b5));
        helper.saveCanonical(b6);
        helper.checkCanonicalHead(b6, expectedScore(g, b1, b2, b3, b4, b5, b6));
        helper.saveFork(b54);
        helper.checkCanonicalHead(b6, expectedScore(g, b1, b2, b3, b4, b5, b6));

        // reorg to b41, b52, b53, b54 and back to b6
        store.reorgTo(b41);
        helper.checkCanonicalHead(b41, expectedScore(g, b1, b2, b31, b41));
        store.reorgTo(b52);
        helper.checkCanonicalHead(b52, expectedScore(g, b1, b2, b31, b42, b52));
        store.reorgTo(b53);
        helper.checkCanonicalHead(b53, expectedScore(g, b1, b2, b31, b42, b53));
        store.reorgTo(b54);
        helper.checkCanonicalHead(b54, expectedScore(g, b1, b2, b31, b42, b54));
        store.reorgTo(b6);
        helper.checkCanonicalHead(b6, expectedScore(g, b1, b2, b3, b4, b5, b6));

        // sanity checks after all reorgs
        Beacon[] canonical = new Beacon[] { g, b1, b2, b3, b4, b5, b6 };
        Beacon[] others = new Beacon[] { b31, b41, b42, b52, b53, b54 };
        for (Beacon b : canonical) {
            assertEquals(b, store.getByHash(b.getHash()));
            assertEquals(b, store.getCanonicalByNumber(b.getNumber()));
        }
        for (Beacon b : others) {
            assertEquals(b, store.getByHash(b.getHash()));
        }
    }

    private BigInteger expectedScore(Beacon ... blocks) {
        BigInteger score = BigInteger.ZERO;
        for (Beacon b : blocks) {
            score = score.add(BigInteger.valueOf(b.getNumber()));
        }
        return score;
    }

    private Beacon createBlock(Beacon parent) {
        byte[] randaoReveal = new byte[32];
        byte[] mainChainRef = new byte[32];
        byte[] stateHash = new byte[32];

        Random rnd = new Random();
        rnd.nextBytes(randaoReveal);
        rnd.nextBytes(mainChainRef);
        rnd.nextBytes(stateHash);

        return new Beacon(parent == null ? new byte[32] : parent.getHash(),
                randaoReveal, mainChainRef, stateHash, parent == null ? 0 : parent.getNumber() + 1);
    }

    static class StoreHelper {
        BeaconStore store;

        private StoreHelper(BeaconStore store) {
            this.store = store;
        }

        static StoreHelper newInstance() {
            return new StoreHelper(new IndexedBeaconStore(new HashMapDB<>(), new HashMapDB<>()));
        }

        void saveFork(Beacon block) {
            saveBlock(block, false);
        }

        void saveCanonical(Beacon block) {
            saveBlock(block, true);
        }

        private void saveBlock(Beacon block, boolean canonical) {
            BigInteger chainScore = store.getChainScore(block.getParentHash()) == null ? BigInteger.ZERO :
                    store.getChainScore(block.getParentHash());
            store.save(block, chainScore.add(BigInteger.valueOf(block.getNumber())), canonical);
        }

        void checkCanonicalHead(Beacon head, BigInteger expectedScore) {
            assertEquals(head, store.getCanonicalHead());
            assertEquals(head, store.getCanonicalByNumber(head.getNumber()));
            assertArrayEquals(head.getParentHash(), store.getCanonicalByNumber(head.getNumber() - 1).getHash());
            assertEquals(expectedScore, store.getCanonicalHeadScore());
        }
    }
}
