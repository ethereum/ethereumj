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

/**
 * Pushed when state recalc happens and
 * crystallized state {@link org.ethereum.sharding.processing.state.CrystallizedState} is changed
 */
public class StateRecalc extends Event<StateRecalc.Data> {

    public static class Data {
        private final Long slot;

        public Data(Long slot) {
            this.slot = slot;
        }

        public Long getSlot() {
            return slot;
        }
    }

    public StateRecalc(Long slot) {
        super(new Data(slot));
    }
}
