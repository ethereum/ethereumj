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

import org.ethereum.datasource.Serializer;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import java.math.BigInteger;

/**
 * Represents an item of a list of validators held by validator registration contract.
 *
 * @author Mikhail Kalinin
 * @since 21.07.2018
 */
public class Validator {

    private byte[] pubKey;
    private long withdrawalShard;
    private byte[] withdrawalAddress;
    private byte[] randao;
    private int index = -1;

    public Validator(byte[] encoded) {
        RLPList list = RLP.unwrapList(encoded);

        this.pubKey = list.get(0).getRLPData();
        this.withdrawalShard = ByteUtil.byteArrayToLong(list.get(1).getRLPData());
        this.withdrawalAddress = list.get(2).getRLPData();
        this.randao = list.get(3).getRLPData();
        this.index = ByteUtil.byteArrayToInt(list.get(4).getRLPData());
    }

    public Validator(byte[] pubKey, long withdrawalShard, byte[] withdrawalAddress, byte[] randao) {
        this(pubKey, withdrawalShard, withdrawalAddress, randao, -1);
    }

    public Validator(byte[] pubKey, long withdrawalShard, byte[] withdrawalAddress, byte[] randao, int index) {
        this.pubKey = pubKey;
        this.withdrawalShard = withdrawalShard;
        this.withdrawalAddress = withdrawalAddress;
        this.randao = randao;
        this.index = index;
    }

    public byte[] getEncoded() {
        return RLP.wrapList(pubKey, ByteUtil.longToBytes(withdrawalShard), withdrawalAddress,
                randao, ByteUtil.intToBytes(index));
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

    public int getIndex() {
        return index;
    }

    public Validator withIndex(int index) {
        return new Validator(pubKey, withdrawalShard, withdrawalAddress, randao, index);
    }

    public static final Serializer<Validator, byte[]> Serializer = new Serializer<Validator, byte[]>() {
        @Override
        public byte[] serialize(Validator validator) {
            return validator.getEncoded();
        }

        @Override
        public Validator deserialize(byte[] stream) {
            return stream == null ? null : new Validator(stream);
        }
    };
}
