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

import org.ethereum.crypto.ECKey;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Roman Mandeleil
 * @since 05.12.2014
 */
public class TransactionReceiptTest {

    private static final Logger logger = LoggerFactory.getLogger("test");


    @Test // rlp decode
    public void test_1() {

        byte[] rlp = Hex.decode("f88aa0966265cc49fa1f10f0445f035258d116563931022a3570a640af5d73a214a8da822b6fb84000000010000000010000000000008000000000000000000000000000000000000000000000000000000000020000000000000014000000000400000000000440d8d7948513d39a34a1a8570c9c9f0af2cba79ac34e0ac8c0808301e24086873423437898");

        TransactionReceipt txReceipt = new TransactionReceipt(rlp);

        assertEquals(1, txReceipt.getLogInfoList().size());

        assertEquals("966265cc49fa1f10f0445f035258d116563931022a3570a640af5d73a214a8da",
                Hex.toHexString(txReceipt.getPostTxState()));

        assertEquals("2b6f",
                Hex.toHexString(txReceipt.getCumulativeGas()));

        assertEquals("01e240",
                Hex.toHexString(txReceipt.getGasUsed()));

        assertEquals("00000010000000010000000000008000000000000000000000000000000000000000000000000000000000020000000000000014000000000400000000000440",
                Hex.toHexString(txReceipt.getBloomFilter().getData()));

        assertEquals("873423437898",
                Hex.toHexString(txReceipt.getExecutionResult()));
        logger.info("{}", txReceipt);
    }

    @Test
    public void test_2() {
        byte[] rlp = Hex.decode("f9012ea02d0cd041158c807326dae7cf5f044f3b9d4bd91a378cc55781b75455206e0c368339dc68b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c08252088080");

        TransactionReceipt txReceipt = new TransactionReceipt(rlp);
        txReceipt.setExecutionResult(new byte[0]);
        byte[] encoded = txReceipt.getEncoded();
        TransactionReceipt txReceipt1 = new TransactionReceipt(encoded);
        System.out.println(txReceipt1);

    }
}
