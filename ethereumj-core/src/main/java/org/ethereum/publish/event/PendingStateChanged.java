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
package org.ethereum.publish.event;

import org.ethereum.core.PendingState;

/**
 * PendingState changes on either new pending transaction or new best block receive
 * When a new transaction arrives it is executed on top of the current pending state
 * When a new best block arrives the PendingState is adjusted to the new Repository state
 * and all transactions which remain pending are executed on top of the new PendingState
 *
 * @author Eugene Shevchenko
 */
public class PendingStateChanged extends Event<PendingState> {
    public PendingStateChanged(PendingState state) {
        super(state);
    }
}
