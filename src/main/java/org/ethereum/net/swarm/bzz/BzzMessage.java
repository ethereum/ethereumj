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
package org.ethereum.net.swarm.bzz;

import org.ethereum.net.message.Message;

/**
 * Base class for all BZZ messages
 */
public abstract class BzzMessage extends Message {

    // non-null for incoming messages
    private BzzProtocol peer;
    protected long id = -1;

    protected BzzMessage() {
    }

    protected BzzMessage(byte[] encoded) {
        super(encoded);
        decode();
    }

    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.fromByte(code);
    }

    protected abstract void decode();

    /**
     * Returns the {@link BzzProtocol} associated with incoming message
     */
    public BzzProtocol getPeer() {
        return peer;
    }

    /**
     * Message ID. Should be unique across all outgoing messages
     */
    public long getId() {
        return id;
    }

    void setPeer(BzzProtocol peer) {
        this.peer = peer;
    }

    void setId(long id) {
        this.id = id;
    }
}
