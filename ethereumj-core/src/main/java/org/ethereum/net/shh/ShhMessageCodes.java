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

import java.util.HashMap;
import java.util.Map;

/**
 * A list of commands for the Whisper network protocol.
 * <br>
 * The codes for these commands are the first byte in every packet.
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Wire-Protocol">
 * https://github.com/ethereum/wiki/wiki/Wire-Protocol</a>
 */
public enum ShhMessageCodes {

    /* Whisper Protocol */

    /**
     * [+0x00]
     */
    STATUS(0x00),

    /**
     * [+0x01]
     */
    MESSAGE(0x01),

    /**
     * [+0x02]
     */
    FILTER(0x02);

    private final int cmd;

    private static final Map<Integer, ShhMessageCodes> intToTypeMap = new HashMap<>();

    static {
        for (ShhMessageCodes type : ShhMessageCodes.values()) {
            intToTypeMap.put(type.cmd, type);
        }
    }

    private ShhMessageCodes(int cmd) {
        this.cmd = cmd;
    }

    public static ShhMessageCodes fromByte(byte i) {
        return intToTypeMap.get((int) i);
    }

    public static boolean inRange(byte code) {
        return code >= STATUS.asByte() && code <= FILTER.asByte();
    }

    public byte asByte() {
        return (byte) (cmd);
    }
}
