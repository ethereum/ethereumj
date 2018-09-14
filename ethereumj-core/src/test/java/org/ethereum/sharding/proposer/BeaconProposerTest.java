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
package org.ethereum.sharding.proposer;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.processing.consensus.NoTransition;
import org.ethereum.sharding.processing.consensus.StateTransition;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.BeaconStateRepository;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.pubsub.Event;
import org.ethereum.sharding.pubsub.Events;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.util.Randao;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.crypto.HashUtil.randomHash;
import static org.ethereum.sharding.proposer.BeaconProposer.SLOT_DURATION;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class BeaconProposerTest {

    @Test
    public void testSlotCalculations() {
        Helper helper = Helper.newInstance();
        BeaconProposer proposer = helper.proposer;

        long genesisTimestamp = BeaconGenesis.instance().getTimestamp();

        assertEquals(0L, proposer.getSlotNumber(genesisTimestamp));
        assertEquals(0L, proposer.getSlotNumber(genesisTimestamp + SLOT_DURATION / 2));
        assertEquals(1L, proposer.getSlotNumber(genesisTimestamp + SLOT_DURATION));
        assertEquals(1L, proposer.getSlotNumber(genesisTimestamp + SLOT_DURATION + SLOT_DURATION / 2));
        assertEquals(49L, proposer.getSlotNumber(genesisTimestamp + SLOT_DURATION * 49));
        assertEquals(49L, proposer.getSlotNumber(genesisTimestamp + SLOT_DURATION * 49 + SLOT_DURATION / 100));

        assertEquals(genesisTimestamp, proposer.getTimestamp(0L));
        assertEquals(genesisTimestamp + SLOT_DURATION, proposer.getTimestamp(1L));
        assertEquals(genesisTimestamp + SLOT_DURATION * 49, proposer.getTimestamp(49L));
    }

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

            newBlock = proposer.createNewBlock(slotNumber);
            helper.insertBlock(newBlock);
            helper.checkBlock(newBlock, parent, reveal, mainChainRef, slotNumber);

            reveal = newBlock.getRandaoReveal();
        }
    }

    static class Helper {
        BeaconProposer proposer;
        EthereumListener listener;
        Publisher publisher;
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
            publisher.publish(Events.onBeaconBlock(block, true));
        }

        byte[] newMainChainBlockHash() {
            Block block = new Block(randomHash(), randomHash(), null, null, BigInteger.ONE.toByteArray(), 1L, new byte[]{0},
                                    0, 0, null, null, null, null, EMPTY_TRIE_HASH, randomHash(), null, null);
            listener.onBlock(new BlockSummary(block, Collections.emptyMap(), Collections.emptyList(),
                    Collections.emptyList()), true);

            return block.getHash();
        }

        static Helper newInstance() {
            Randao randao = new Randao(new HashMapDB<>());
            randao.generate(1000);

            StateRepository repository = new BeaconStateRepository(new HashMapDB<>(), new HashMapDB<>(), new HashMapDB<>());
            StateTransition<BeaconState> stateTransition = new NoTransition();

            CompositeEthereumListenerMock listener = new CompositeEthereumListenerMock();
            Ethereum ethereum = new EthereumMock(listener);

            Publisher publisher = new PublisherMock();


            Helper helper = new Helper();
            helper.proposer = new BeaconProposerImpl(ethereum, publisher, randao, repository, stateTransition) {
                @Override
                byte[] getMainChainRef(Block mainChainHead) {
                    return mainChainHead.getHash();
                }
            };
            helper.listener = listener;
            helper.publisher = publisher;
            helper.randao = randao;
            helper.stateTransition = stateTransition;
            helper.repository = repository;

            return helper;
        }
    }

    static class PublisherMock extends Publisher {
        public PublisherMock() {
            super(EventDispatchThread.getDefault());
        }

        @Override
        public void publish(Event event) {
            List<Consumer> subs = subscriptionMap.getOrDefault(event.getClass(), Collections.emptyList());
            subs.forEach(s -> s.accept(event.getData()));
        }
    }

    static class CompositeEthereumListenerMock extends CompositeEthereumListener {
        @Override
        public void onBlock(BlockSummary blockSummary, boolean best) {
            for (EthereumListener l : listeners) {
                l.onBlock(blockSummary, best);
            }
        }
    }

    static class EthereumMock extends EthereumImpl {

        CompositeEthereumListener listener;

        public EthereumMock(CompositeEthereumListener listener) {
            super(SystemProperties.getDefault(), listener);
            this.listener = listener;
        }

        @Override
        public void addListener(EthereumListener listener) {
            this.listener.addListener(listener);
        }

        @Override
        public Blockchain getBlockchain() {
            return new BlockchainImpl() {
                @Override
                public synchronized Block getBestBlock() {
                    return new Block(randomHash(), randomHash(), null, null, BigInteger.ONE.toByteArray(),
                            1L, new byte[]{0}, 0, 0, null, null, null, null, EMPTY_TRIE_HASH, randomHash(), null, null);
                }
            };
        }
    }
}
