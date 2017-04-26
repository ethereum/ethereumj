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
package org.ethereum.mine;

import org.ethereum.core.Block;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * The Miner performs the proof-of-work needed for a valid block
 *
 * The mining proof-of-work (PoW) exists as a cryptographically secure nonce
 * that proves beyond reasonable doubt that a particular amount of computation
 * has been expended in the determination of some token value n.
 * It is utilised to enforce the blockchain security by giving meaning
 * and credence to the notion of difficulty (and, by extension, total difficulty).
 *
 * However, since mining new blocks comes with an attached reward,
 * the proof-of-work not only functions as a method of securing confidence
 * that the blockchain will remain canonical into the future, but also as
 * a wealth distribution mechanism.
 *
 * See Yellow Paper: http://www.gavwood.com/Paper.pdf (chapter 11.5 Mining Proof-of-Work)
 */
public class Miner {

    private static final Logger logger = LoggerFactory.getLogger("miner");
    private boolean stop = false;


    /**
     * Adds a nonce to given block which complies with the given difficulty
     *
     * For the PoC series, we use a simplified proof-of-work.
     * This is not ASIC resistant and is meant merely as a placeholder.
     * It utilizes the bare SHA3 hash function to secure the block chain by requiring
     * the SHA3 hash of the concatenation of the nonce and the header’s SHA3 hash to be
     * sufficiently low. It is formally defined as PoW:
     *
     *      PoW(H, n) ≡ BE(SHA3(SHA3(RLP(H!n)) ◦ n))
     *
     *  where:
     *      RLP(H!n) is the RLP encoding of the block header H, not including the
     *          final nonce component;
     *      SHA3 is the SHA3 hash function accepting an arbitrary length series of
     *          bytes and evaluating to a series of 32 bytes (i.e. 256-bit);
     *      n is the nonce, a series of 32 bytes;
     *      o is the series concatenation operator;
     *      BE(X) evaluates to the value equal to X when interpreted as a
     *          big-endian-encoded integer.
     *
     * @param newBlock without a valid nonce
     * @param difficulty - the mining difficulty
     * @return true if valid nonce has been added to the block
     */
    public boolean mine(Block newBlock, byte[] difficulty) {

//        eval(_root, _nonce) <= (bigint(1) << 256) / _difficulty; }
        stop = false;
        BigInteger max = BigInteger.valueOf(2).pow(255);
        byte[] target = BigIntegers.asUnsignedByteArray(32,
                max.divide(new BigInteger(1, difficulty)));


        long newGasLimit = Math.max(125000,
                (new BigInteger(1, newBlock.getGasLimit()).longValue() * (1024 - 1) + (newBlock.getGasUsed() * 6 / 5)) / 1024);
        newBlock.getHeader().setGasLimit(BigInteger.valueOf(newGasLimit).toByteArray());

        byte[] hash = sha3(newBlock.getEncodedWithoutNonce());

        byte[] testNonce = new byte[32];
        byte[] concat;

        while (ByteUtil.increment(testNonce) && !stop) {

            if (testNonce[31] == 0 && testNonce[30] == 0) {
                System.out.println("mining: " + new BigInteger(1, testNonce));
            }

            if (testNonce[31] == 0)
                sleep();
            concat = Arrays.concatenate(hash, testNonce);
            byte[] result = sha3(concat);
            if (FastByteComparisons.compareTo(result, 0, 32, target, 0, 32) < 0) {
                newBlock.setNonce(testNonce);
                return true;
            }
        }
        return false; // couldn't find a valid nonce
    }

    public void stop() {
        stop = true;
    }

    private void sleep() {
        try {
//            Thread.sleep(1);
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
