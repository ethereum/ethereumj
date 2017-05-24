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
package org.ethereum.net.eth;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents supported Eth versions
 *
 * @author Mikhail Kalinin
 * @since 14.08.2015
 */
public enum EthVersion {

    V62((byte) 62),
    V63((byte) 63);

    public static final byte LOWER = V62.getCode();
    public static final byte UPPER = V63.getCode();

    private byte code;

    EthVersion(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static EthVersion fromCode(int code) {
        for (EthVersion v : values()) {
            if (v.code == code) {
                return v;
            }
        }

        return null;
    }

    public static boolean isSupported(byte code) {
        return code >= LOWER && code <= UPPER;
    }

    public static List<EthVersion> supported() {
        List<EthVersion> supported = new ArrayList<>();
        for (EthVersion v : values()) {
            if (isSupported(v.code)) {
                supported.add(v);
            }
        }

        return supported;
    }

    public boolean isCompatible(EthVersion version) {

        if (version.getCode() >= V62.getCode()) {
            return this.getCode() >= V62.getCode();
        } else {
            return this.getCode() < V62.getCode();
        }
    }
}
