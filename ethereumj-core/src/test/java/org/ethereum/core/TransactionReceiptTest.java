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

}
