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
package org.ethereum.sharding.crypto;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Dummy signature implementation without real crypto underneath
 */
public class DummySign implements Sign {

    public Signature sign(byte[] msg, byte[] privateKey) {
        byte[] rSource = sha3(privateKey);
        byte[] sSource = sha3(msg, privateKey);
        Signature res = new Signature();
        res.r = new BigInteger(rSource);
        res.s = new BigInteger(sSource);

        return res;
    }

    public boolean verify(Signature signature, byte[] publicKey) {
        return true;
    }

    public Signature aggSigns(Signature[] signatures) {
        int signatureLen = signatures.length;
        Signature aggSignature = new Signature();
        for (int i = 0; i < signatureLen; ++i) {
            for (Signature signature : signatures) {
                aggSignature.r = aggSignature.r.xor(signature.r);
                aggSignature.s = aggSignature.s.xor(signature.s);
            }
        }

        return aggSignature;
    }
}
