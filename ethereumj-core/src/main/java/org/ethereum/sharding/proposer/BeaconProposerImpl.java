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

import org.ethereum.core.BlockSummary;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.pubsub.BeaconBlockImported;
import org.ethereum.sharding.pubsub.BeaconChainLoaded;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.processing.consensus.StateTransition;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.util.Randao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link BeaconProposer}.
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class BeaconProposerImpl implements BeaconProposer {

    private static final Logger logger = LoggerFactory.getLogger("proposer");

    Randao randao;
    StateTransition stateTransition;
    StateRepository repository;

    private byte[] mainChainRef;
    private Beacon head;
    private BeaconState recentState;

    public BeaconProposerImpl(Ethereum ethereum, Publisher publisher, Randao randao,
                              StateRepository repository, StateTransition stateTransition) {
        this.randao = randao;
        this.repository = repository;
        this.stateTransition = stateTransition;
        this.mainChainRef = ethereum.getBlockchain().getBestBlock().getHash();

        // init head
        publisher.subscribe(BeaconChainLoaded.class, data -> head = data.getHead());

        // update head
        publisher.subscribe(BeaconBlockImported.class, data -> {
            if (data.isBest()) {
                head = data.getBlock();
                recentState = this.repository.get(data.getBlock().getStateHash());
            }
        });

        // update main chain ref
        ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary, boolean best) {
                if (best)
                    mainChainRef = blockSummary.getBlock().getHash();
            }
        });
    }

    @Override
    public Beacon createNewBlock(long slotNumber) {
        Beacon block = new Beacon(head.getHash(), randao.reveal(), mainChainRef,
                HashUtil.EMPTY_DATA_HASH, slotNumber);
        BeaconState newState = stateTransition.applyBlock(block, recentState);
        block.setStateHash(newState.getHash());

        logger.info("New block created {}", block);
        return block;
    }

    @Override
    public long getTimestamp(long slotNumber) {
        return BeaconGenesis.instance().getTimestamp() + slotNumber * SLOT_DURATION;
    }

    @Override
    public long getSlotNumber(long timestamp) {
        return (timestamp - BeaconGenesis.instance().getTimestamp()) / SLOT_DURATION;
    }
}
