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
package org.ethereum.sharding.util;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Signature utilities
 */
public class Sign {

    public static byte[] sign(byte[] msg, byte[] privateKey) {
        // TODO: BLS should be here
        return sha3(msg, privateKey);
    }

    public static boolean verify(byte[] signature, byte[] publicKey) {
        // TODO: real BLS verification should be here
        return true;
    }

    public static BigInteger[] aggSigns(byte[][] signatures) {
        // TODO: real BLS signature aggregation instead of XOR
        int signatureLen = signatures[0].length;
        byte[] aggSignature = new byte[signatureLen];
        for (int i = 0; i < signatureLen; ++i) {
            for (byte[] signature : signatures) {
                aggSignature[i] ^= signature[i];
            }
        }

        return new BigInteger[] {new BigInteger(1, aggSignature), BigInteger.ONE};
    }
}
