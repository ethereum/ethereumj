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
import java.util.List;

import static org.ethereum.crypto.HashUtil.blake2b;
import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Dummy signature implementation without real crypto underneath
 */
public class DummySign implements Sign {

    /**
     * Sign the message
     */
    @Override
    public Signature sign(byte[] msg, BigInteger privateKey) {
        byte[] rSource = sha3(privateKey.toByteArray());
        byte[] sSource = sha3(msg, privateKey.toByteArray());
        Signature res = new Signature();
        res.r = new BigInteger(rSource);
        res.s = new BigInteger(sSource);

        return res;
    }

    /**
     * Verifies whether signature is made by signer with pubKey
     */
    @Override
    public boolean verify(Signature signature, byte[] msg, BigInteger pubKey) {
        byte[] rSource = sha3(pubKey.toByteArray());
        byte[] sSource = sha3(msg, pubKey.toByteArray());
        Signature res = new Signature();
        res.r = new BigInteger(rSource);
        res.s = new BigInteger(sSource);
        return res.equals(signature);
    }

    @Override
    public BigInteger privToPub(BigInteger privKey) {
        return privKey;
    }

    /**
     * Aggregates several signatures in one
     */
    @Override
    public Signature aggSigns(List<Signature> signatures) {
        if (signatures.isEmpty())
            throw new RuntimeException("Couldn't aggregate empty list");

        return signatures.get(0);
    }

    /**
     * Aggregates public keys
     */
    @Override
    public BigInteger aggPubs(List<BigInteger> pubKeys) {
        if (pubKeys.isEmpty())
            throw new RuntimeException("Couldn't aggregate empty list");

        return pubKeys.get(0);
    }
}
