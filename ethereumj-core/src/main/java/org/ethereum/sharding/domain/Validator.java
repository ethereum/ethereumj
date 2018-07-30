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

/**
 * Represents and item of a list of validators held by validator registration contract.
 *
 * @author Mikhail Kalinin
 * @since 21.07.2018
 */
public class Validator {

    private byte[] pubKey;
    private long withdrawalShard;
    private byte[] withdrawalAddress;
    private byte[] randao;

    private Validator() {
    }

    public Validator(byte[] pubKey, long withdrawalShard, byte[] withdrawalAddress, byte[] randao) {
        this.pubKey = pubKey;
        this.withdrawalShard = withdrawalShard;
        this.withdrawalAddress = withdrawalAddress;
        this.randao = randao;
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
