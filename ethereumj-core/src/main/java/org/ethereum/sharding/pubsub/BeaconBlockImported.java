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
package org.ethereum.sharding.pubsub;

import org.ethereum.sharding.domain.Beacon;

/**
 * Event is triggered when new block has been successfully imported into beacon chain.
 *
 * <p>
 *     Shares the block and a flag that shows whether this block is BEST or NOT_BEST.
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class BeaconBlockImported extends Event<BeaconBlockImported.Data> {

    public static class Data {
        private final Beacon block;
        private final boolean best;

        private Data(Beacon block, boolean best) {
            this.block = block;
            this.best = best;
        }

        public Beacon getBlock() {
            return block;
        }

        public boolean isBest() {
            return best;
        }
    }

    public BeaconBlockImported(Beacon block, boolean best) {
        super(new Data(block, best));
    }
}
