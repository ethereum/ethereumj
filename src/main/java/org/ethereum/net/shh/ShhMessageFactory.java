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
package org.ethereum.net.shh;

import org.ethereum.net.message.*;
import org.ethereum.net.message.Message;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class ShhMessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        ShhMessageCodes receivedCommand = ShhMessageCodes.fromByte(code);
        switch (receivedCommand) {
            case STATUS:
                return new ShhStatusMessage(encoded);
            case MESSAGE:
                return new ShhEnvelopeMessage(encoded);
            case FILTER:
                return new ShhFilterMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}
