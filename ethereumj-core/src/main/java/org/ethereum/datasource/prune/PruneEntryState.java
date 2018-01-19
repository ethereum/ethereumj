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
package org.ethereum.datasource.prune;

/**
 * @author Mikhail Kalinin
 * @since 18.01.2018
 */
public enum  PruneEntryState {

    UNACCEPTED,     // neither deletions nor insertions have been accepted (all actions have been reverted)
    INSERTED,       // insertion of the node has been accepted by fork management
    DELETED,        // deletion of the node has been accepted by fork management
    RECYCLED;        // meant to be removed from pruning

    public boolean relatesTo(JournalAction action) {
        return action == JournalAction.DELETION && this == DELETED ||
                action == JournalAction.INSERTION && this == INSERTED;
    }
}
