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
package org.ethereum.sharding.validator;

import org.ethereum.core.Block;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.processing.consensus.NoTransition;
import org.ethereum.sharding.processing.consensus.StateTransition;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.db.IndexedBeaconStore;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.BeaconStateRepository;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.util.Randao;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.crypto.HashUtil.randomHash;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class BeaconProposerTest {

    @Test
    public void testBlockProposing() {
        Helper helper = Helper.newInstance();
        BeaconProposer proposer = helper.proposer;

        Beacon newBlock = BeaconGenesis.instance();
        helper.insertBlock(newBlock);
        byte[] reveal = helper.randao.revealNext();

        for (int i = 0; i < 5; i++) {
            long slotNumber = i * 10;
            byte[] mainChainRef = helper.newMainChainBlockHash();
            Beacon parent = newBlock;

            BeaconProposer.Input in = new BeaconProposer.Input(slotNumber, parent, helper.recentState, mainChainRef);
            newBlock = proposer.createNewBlock(in, new byte[] {});
            helper.insertBlock(newBlock);
            helper.checkBlock(newBlock, parent, reveal, mainChainRef, slotNumber);

            reveal = newBlock.getRandaoReveal();
        }
    }

    static class Helper {
        BeaconProposer proposer;
        Randao randao;
        StateTransition<BeaconState> stateTransition;
        StateRepository repository;
        BeaconState recentState;

        void checkBlock(Beacon newBlock, Beacon parent, byte[] prevReveal, byte[] mainChainRef, long slotNumber) {
            assertTrue(parent.isParentOf(newBlock));
            assertArrayEquals(prevReveal, blake2b(newBlock.getRandaoReveal()));
            assertArrayEquals(mainChainRef, newBlock.getMainChainRef());
            assertEquals(slotNumber, newBlock.getSlotNumber());
            assertArrayEquals(recentState.getHash(), newBlock.getStateHash());
        }

        void insertBlock(Beacon block) {
            if (block.isGenesis()) {
                recentState = repository.getEmpty();
                block.setStateHash(recentState.getHash());
            } else {
                recentState = stateTransition.applyBlock(block, recentState);
            }
            repository.insert(recentState);
        }

        byte[] newMainChainBlockHash() {
            Block block = new Block(randomHash(), randomHash(), null, null, BigInteger.ONE.toByteArray(), 1L, new byte[]{0},
                                    0, 0, null, null, null, null, EMPTY_TRIE_HASH, randomHash(), null, null);

            return block.getHash();
        }

        static Helper newInstance() {
            Randao randao = new Randao(new HashMapDB<>());
            randao.generate(1000);

            StateRepository repository = new BeaconStateRepository(new HashMapDB<>(), new HashMapDB<>(),
                    new HashMapDB<>(), new HashMapDB<>(), new HashMapDB<>());
            BeaconStore store = new IndexedBeaconStore(new HashMapDB<>(), new HashMapDB<>());
            StateTransition<BeaconState> stateTransition = new NoTransition();

            Helper helper = new Helper();
            helper.proposer = new BeaconProposerImpl(randao, repository, store, stateTransition,
                    ValidatorConfig.DISABLED, Mockito.mock(BeaconAttester.class)) {

                @Override
                byte[] randaoReveal(BeaconState state, byte[] pubKey) {
                    return randao.revealNext();
                }
            };
            helper.randao = randao;
            helper.stateTransition = stateTransition;
            helper.repository = repository;

            return helper;
        }
    }
}
