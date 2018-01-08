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
package org.ethereum.listener;

/**
 * Created by Anton Nashatyrev on 15.07.2016.
 */
public class TxStatus {

    public static final TxStatus REJECTED = new TxStatus(0);
    public static final TxStatus PENDING = new TxStatus(0);
    public static TxStatus getConfirmed(int blocks) {
        return new TxStatus(blocks);
    }

    public final int confirmed;

    private TxStatus(int confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public String toString() {
        if (this == REJECTED) return "REJECTED";
        if (this == PENDING) return "PENDING";
        return "CONFIRMED_" + confirmed;
    }
}
