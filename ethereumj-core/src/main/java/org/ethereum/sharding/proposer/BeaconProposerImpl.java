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

import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.sharding.pubsub.BeaconBlockImported;
import org.ethereum.sharding.pubsub.BeaconChainLoaded;
import org.ethereum.sharding.pubsub.BeaconChainSynced;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.processing.consensus.StateTransition;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.util.Randao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.max;

/**
 * Default implementation of {@link BeaconProposer}.
 *
 * <p>
 *     <b>Note:</b> {@link #createNewBlock(long, byte[])} must not be called prior to {@link BeaconChainSynced} event,
 *     handler of this event is used to finish proposer initialization
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class BeaconProposerImpl implements BeaconProposer {

    private static final Logger logger = LoggerFactory.getLogger("proposer");

    Randao randao;
    StateTransition<BeaconState> stateTransition;
    StateRepository repository;
    BlockStore blockStore;
    ValidatorConfig config;

    private byte[] mainChainRef;
    private ChainHead head;

    public BeaconProposerImpl(Ethereum ethereum, Publisher publisher, Randao randao,
                              StateRepository repository, StateTransition<BeaconState> stateTransition,
                              ValidatorConfig config) {
        this.randao = randao;
        this.repository = repository;
        this.stateTransition = stateTransition;
        this.blockStore = ethereum.getBlockchain().getBlockStore();
        this.mainChainRef = getMainChainRef(ethereum.getBlockchain().getBestBlock());
        this.config = config;

        // init head
        publisher.subscribe(BeaconChainSynced.class, data -> {
            head = new ChainHead(data.getHead(), data.getState());
        });

        // update head
        publisher.subscribe(BeaconBlockImported.class, data -> {
            if (head != null && data.isBest()) {
                head = new ChainHead(data.getBlock(), data.getState());
            }
        });

        // update main chain ref
        ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary, boolean best) {
                if (best)
                    mainChainRef = getMainChainRef(blockSummary.getBlock());
            }
        });
    }

    byte[] getMainChainRef(Block mainChainHead) {
        return blockStore.getBlockHashByNumber(max(0L, mainChainHead.getNumber() - REORG_SAFE_DISTANCE));
    }

    byte[] randaoReveal(BeaconState state, byte[] pubKey) {
        if (!config.isEnabled()) {
            logger.error("Failed to reveal Randao: validator is disabled in the config");
            return new byte[] {};
        }

        Validator validator = state.getValidatorSet().getByPubKey(pubKey);
        if (validator == null) {
            logger.error("Failed to reveal Randao: validator does not exist in the set");
            return new byte[] {};
        }

        return randao.reveal(validator.getRandao());
    }

    @Override
    public Beacon createNewBlock(long slotNumber, byte[] pubKey) {
        ChainHead recentHead = head;
        Beacon block = new Beacon(recentHead.block.getHash(), randaoReveal(recentHead.state, pubKey), mainChainRef,
                HashUtil.EMPTY_DATA_HASH, slotNumber);
        BeaconState newState = stateTransition.applyBlock(block, recentHead.state);
        block.setStateHash(newState.getHash());

        logger.info("New block created {}", block);
        return block;
    }

    class ChainHead {
        Beacon block;
        BeaconState state;

        public ChainHead(Beacon block, BeaconState state) {
            this.block = block;
            this.state = state;
        }
    }
}
