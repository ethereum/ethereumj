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
package org.ethereum.core;

import java.math.BigInteger;

public enum Denomination {

    WEI(newBigInt(0)),
    SZABO(newBigInt(12)),
    FINNEY(newBigInt(15)),
    ETHER(newBigInt(18));

    private BigInteger amount;

    private Denomination(BigInteger value) {
        this.amount = value;
    }

    public BigInteger value() {
        return amount;
    }

    public long longValue() {
        return value().longValue();
    }

    private static BigInteger newBigInt(int value) {
        return BigInteger.valueOf(10).pow(value);
    }

    public static String toFriendlyString(BigInteger value) {
        if (value.compareTo(ETHER.value()) == 1 || value.compareTo(ETHER.value()) == 0) {
            return Float.toString(value.divide(ETHER.value()).floatValue()) +  " ETHER";
        }
        else if(value.compareTo(FINNEY.value()) == 1 || value.compareTo(FINNEY.value()) == 0) {
            return Float.toString(value.divide(FINNEY.value()).floatValue()) +  " FINNEY";
        }
        else if(value.compareTo(SZABO.value()) == 1 || value.compareTo(SZABO.value()) == 0) {
            return Float.toString(value.divide(SZABO.value()).floatValue()) +  " SZABO";
        }
        else
            return Float.toString(value.divide(WEI.value()).floatValue()) +  " WEI";
    }
}
