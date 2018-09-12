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
package org.ethereum.sharding.processing;

import com.google.common.util.concurrent.Futures;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.DbFlushManager;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.processing.consensus.NoTransition;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.db.IndexedBeaconStore;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.BeaconStateRepository;
import org.ethereum.sharding.processing.state.StateRepository;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Future;

import static org.ethereum.sharding.processing.ProcessingResult.Best;
import static org.ethereum.sharding.processing.ProcessingResult.NotBest;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 17.08.2018
 */
public class BeaconChainTest {

    @Test
    public void testInit() {
        Helper helper = Helper.newInstance();
        BeaconChain beaconChain = helper.beaconChain;

        beaconChain.init();
        Beacon head = beaconChain.getCanonicalHead();
        assertEquals(BeaconGenesis.instance(), head);
        assertArrayEquals(BeaconState.empty().getHash(), helper.repository.get(head.getStateHash()).getHash());
        assertEquals(BeaconGenesis.instance().getScore(), helper.store.getCanonicalHeadScore());
    }

    @Test
    public void testImport() {
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

        Helper helper = Helper.newInstance();
        BeaconChain beaconChain = helper.beaconChain;
        beaconChain.init();

        Beacon g = beaconChain.getCanonicalHead();

        Beacon b1 = helper.createBlock(g);
        Beacon b2 = helper.createBlock(b1);
        Beacon b3 = helper.createBlock(b2);
        Beacon b4 = helper.createBlock(b3);
        Beacon b5 = helper.createBlock(b4, 6);
        Beacon b6 = helper.createBlock(b5);

        Beacon b31 = helper.createBlock(b2, 4);
        Beacon b41 = helper.createBlock(b31);

        Beacon b42 = helper.createBlock(b31);

        Beacon b52 = helper.createBlock(b42);
        Beacon b53 = helper.createBlock(b42);
        Beacon b54 = helper.createBlock(b42, 6);

        assertEquals(Best, beaconChain.insert(b1));
        assertEquals(Best, beaconChain.insert(b2));
        assertEquals(Best, beaconChain.insert(b3));
        helper.checkCanonical(b1, b2, b3);

        // b31 beats b3
        assertEquals(Best, beaconChain.insert(b31));
        helper.checkCanonical(b1, b2, b31);

        // b4 beats b31
        assertEquals(Best, beaconChain.insert(b4));
        helper.checkCanonical(b1, b2, b3, b4);

        // b42 beats b4
        assertEquals(Best, beaconChain.insert(b42));
        helper.checkCanonical(b1, b2, b31, b42);

        // b41 is not best
        assertEquals(NotBest, beaconChain.insert(b41));
        assertTrue(helper.store.exist(b41.getHash()));
        helper.checkCanonical(b1, b2, b31, b42);

        // b5 beats b42
        assertEquals(Best, beaconChain.insert(b5));
        helper.checkCanonical(b1, b2, b3, b4, b5);

        // b52 is not best
        assertEquals(NotBest, beaconChain.insert(b52));
        assertTrue(helper.store.exist(b52.getHash()));
        helper.checkCanonical(b1, b2, b3, b4, b5);

        // b54 beats b5
        assertEquals(Best, beaconChain.insert(b54));
        helper.checkCanonical(b1, b2, b31, b42, b54);

        // b53 is not best
        assertEquals(NotBest, beaconChain.insert(b53));
        assertTrue(helper.store.exist(b53.getHash()));
        helper.checkCanonical(b1, b2, b31, b42, b54);

        // b6 beats b53
        assertEquals(Best, beaconChain.insert(b6));
        helper.checkCanonical(b1, b2, b3, b4, b5, b6);
    }

    @Test
    public void testPreValidation() {
        Helper helper = Helper.newInstance();
        BeaconChain beaconChain = helper.beaconChain;
        beaconChain.init();

        Beacon g = beaconChain.getCanonicalHead();
        Beacon b1 = helper.createBlock(g);
        Beacon b2 = helper.createBlock(b1);

        assertEquals(ProcessingResult.NoParent, beaconChain.insert(b2));

        beaconChain.insert(b1);
        assertEquals(ProcessingResult.Exist, beaconChain.insert(b1));
    }

    @Test
    public void testPostValidation() {
        Helper helper = Helper.newInstance();
        BeaconChain beaconChain = helper.beaconChain;
        beaconChain.init();

        Beacon g = beaconChain.getCanonicalHead();
        Beacon b1 = helper.createBlock(g);
        byte[] incorrectHash = new byte[32];
        new Random().nextBytes(incorrectHash);

        b1.setStateHash(incorrectHash);

        assertEquals(ProcessingResult.ConsensusBreak, beaconChain.insert(b1));
    }

    static class Helper {
        StateRepository repository;
        BeaconStore store;
        BeaconChainImpl beaconChain;

        static Helper newInstance() {
            Helper inst = new Helper();
            inst.store = new IndexedBeaconStore(new HashMapDB<>(), new HashMapDB<>());
            inst.repository = new BeaconStateRepository(new HashMapDB<>(), new HashMapDB<>());
            inst.beaconChain = (BeaconChainImpl) BeaconChainFactory.create(
                    new DummyFlusher(), inst.store, inst.repository, new NoTransition());
            inst.beaconChain.scoreFunction = (block, state) -> BigInteger.valueOf(block.getMainChainRef()[0]);
            return inst;
        }

        Beacon createBlock(Beacon parent) {
            return createBlock(parent, parent.getSlotNumber() + 1);
        }

        Beacon createBlock(Beacon parent, long score) {
            byte[] randaoReveal = new byte[32];
            byte[] mainChainRef = new byte[32];

            Random rnd = new Random();
            rnd.nextBytes(randaoReveal);
            rnd.nextBytes(mainChainRef);

            // save score
            mainChainRef[0] = (byte) score;

            BeaconState parentState = repository.get(parent.getStateHash());

            Beacon newBlock = new Beacon(parent.getHash(),
                    randaoReveal, mainChainRef, null, parent.getSlotNumber() + 1);
            BeaconState newState = beaconChain.transitionFunction.applyBlock(newBlock, parentState);
            newBlock.setStateHash(newState.getHash());

            return newBlock;
        }

        void checkCanonical(Beacon... chain) {
            BigInteger expectedScore = BigInteger.ZERO;
            for (Beacon b : chain) {
                assertTrue(store.exist(b.getHash()));
                expectedScore = expectedScore.add(beaconChain.scoreFunction.apply(b, null));
            }

            Beacon expectedHead = chain[chain.length - 1];
            assertEquals(expectedHead, beaconChain.getCanonicalHead());
            assertEquals(expectedScore, beaconChain.store.getCanonicalHeadScore());
            assertNotNull(repository.get(expectedHead.getStateHash()));
        }
    }

    // nothing to flush, the data goes into DB instantly
    static class DummyFlusher extends DbFlushManager {

        public DummyFlusher() {
            super(SystemProperties.getDefault(), Collections.emptySet(), null);
        }

        @Override
        public synchronized Future<Boolean> flush() {
            return Futures.immediateFuture(true);
        }

        @Override
        public synchronized void commit() {
        }
    }
}
