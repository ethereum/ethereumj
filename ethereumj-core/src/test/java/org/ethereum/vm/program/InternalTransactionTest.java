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
package org.ethereum.vm.program;

import org.ethereum.vm.DataWord;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InternalTransactionTest {

    @Test
    public void testRlpEncoding() {
        byte[] parentHash = randomBytes(32);
        int deep = Integer.MAX_VALUE;
        int index = Integer.MAX_VALUE;
        byte[] nonce = randomBytes(2);
        DataWord gasPrice = DataWord.ZERO;
        DataWord gasLimit = DataWord.ZERO;
        byte[] sendAddress = randomBytes(20);
        byte[] receiveAddress = randomBytes(20);
        byte[] value = randomBytes(2);
        byte[] data = randomBytes(128);
        String note = "transaction note";

        byte[] encoded = new InternalTransaction(parentHash, deep, index, nonce, gasPrice, gasLimit, sendAddress, receiveAddress, value, data, note).getEncoded();

        InternalTransaction tx = new InternalTransaction(encoded);

        assertEquals(deep, tx.getDeep());
        assertEquals(index, tx.getIndex());
        assertArrayEquals(parentHash, tx.getParentHash());
        assertArrayEquals(nonce, tx.getNonce());
        assertArrayEquals(gasPrice.getData(), tx.getGasPrice());
        assertArrayEquals(gasLimit.getData(), tx.getGasLimit());
        assertArrayEquals(sendAddress, tx.getSender());
        assertArrayEquals(receiveAddress, tx.getReceiveAddress());
        assertArrayEquals(value, tx.getValue());
        assertArrayEquals(data, tx.getData());
        assertEquals(note, tx.getNote());
    }

    private static byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        new Random().nextBytes(bytes);
        return bytes;
    }

}