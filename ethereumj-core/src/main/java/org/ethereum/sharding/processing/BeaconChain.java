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

import org.ethereum.core.Block;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.processing.consensus.StateTransition;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.validator.ValidationRule;

import java.math.BigInteger;

/**
 * Responsible for validating, importing and storing blocks of the beacon chain.
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public interface BeaconChain {

    /**
     * Initializes inner state.
     * In case if db is empty populates it with initial data like genesis and initial state.
     */
    void init();

    /**
     * Returns a block that is a head of canonical chain.
     */
    Beacon getCanonicalHead();

    /**
     * Inserts a block into a chain.
     * This process includes block validation, processing, fork choice, db storage and some more actions.
     *
     * @return result of block import, check {@link ProcessingResult} for details.
     *
     * @see ValidationRule
     * @see StateTransition
     */
    ProcessingResult insert(Beacon block);

    /**
     * A hack for PoC mode, sets genesis mainChainRef to given block.
     * Thus, makes initial validator induction pass correctly.
     */
    void setBestBlock(Block block);

    class ScoredChainHead {
        final Beacon block;
        final BigInteger score;
        final BeaconState state;

        public ScoredChainHead(Beacon block, BigInteger score, BeaconState state) {
            this.block = block;
            this.score = score;
            this.state = state;
        }

        public boolean isParentOf(ScoredChainHead other) {
            return this.block.isParentOf(other.block);
        }

        public boolean shouldReorgTo(ScoredChainHead other) {
            return !this.isParentOf(other) && this.score.compareTo(other.score) < 0;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ScoredChainHead)) return false;

            return this.block.equals(((ScoredChainHead) other).block);
        }
    }
}
