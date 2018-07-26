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
package org.ethereum.sharding.domain;

import org.ethereum.util.ByteUtil;

import static java.util.Arrays.copyOfRange;

/**
 * @author Mikhail Kalinin
 * @since 21.07.2018
 */
public class Validator {

    private static final int LOG_DATA_SIZE = 32 * 3 + 20; // pubKey + shardId + withdrawalAddress + randao

    private byte[] pubKey;
    private long withdrawalShard;
    private byte[] withdrawalAddress;
    private byte[] randao;

    private Validator() {
    }

    public static Validator fromLogData(byte[] data) {
        if (data.length < LOG_DATA_SIZE)
            return null;

        Validator v = new Validator();
        v.pubKey = copyOfRange(data, 0, 32);
        v.withdrawalShard = ByteUtil.bytesToBigInteger(copyOfRange(data, 32, 64)).longValue();
        v.withdrawalAddress = copyOfRange(data, 64, 84);
        v.randao = copyOfRange(data, 84, 116);

        return v;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public long getWithdrawalShard() {
        return withdrawalShard;
    }

    public byte[] getWithdrawalAddress() {
        return withdrawalAddress;
    }

    public byte[] getRandao() {
        return randao;
    }
}
