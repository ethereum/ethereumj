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
package org.ethereum.solidity;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Maximilian Schmidt on 25.09.2018.
 */
public class SolidityTypeTest {
    @Test
    public void ensureUnsignedInteger_isDecodedWithCorrectSignum() {
        byte[] bigNumberByteArray = { -13, -75, 19, 86, -119, 67, 112, -4, 118, -86, 98, -46, 103, -42, -126, 63, -60, -15, -87, 57, 43, 11, -17, -52,
                                      0, 3, -65, 14, -67, -40, 65, 119 };
        SolidityType testObject = new SolidityType.UnsignedIntType("uint256");
        Object decode = testObject.decode(bigNumberByteArray);
        assertEquals(decode.getClass(), BigInteger.class);
        BigInteger actualBigInteger = (BigInteger) decode;
        BigInteger expectedBigInteger = new BigInteger(Hex.toHexString(bigNumberByteArray), 16);
        assertEquals(expectedBigInteger, actualBigInteger);
    }

    @Test
    public void ensureSignedInteger_isDecoded() {
        byte[] bigNumberByteArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 127, -1, -1, -1, -1, -1, -1, -1 };
        SolidityType testObject = new SolidityType.IntType("int256");
        Object decode = testObject.decode(bigNumberByteArray);
        assertEquals(decode.getClass(), BigInteger.class);
        BigInteger actualBigInteger = (BigInteger) decode;
        BigInteger expectedBigInteger = new BigInteger(bigNumberByteArray);
        assertEquals(expectedBigInteger, actualBigInteger);
    }
}
