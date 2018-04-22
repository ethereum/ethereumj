package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.Value;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

public class EncodedPurgeTest {
    private byte[] RLP_SIGNED_TX = Hex.decode("f871830617428504a817c80083015f90940123286bd94beecd40905321f5c3202c7628d685880ecab7b2bae2c27080819ea021355678b1aa704f6ad4706fb8647f5125beadd1d84c6f9cf37dda1b62f24b1aa06b4a64fd29bb6e54a2c5107e8be42ac039a8ffb631e16e7bcbd15cdfc0015ee2");
    private byte[] RLP_UNSIGNED_TX = Hex.decode("ef098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a764000080830516158080");

    @Test
    public void purgeSignedTransactionTest() {
        Transaction tx = new Transaction(RLP_SIGNED_TX);
        assertEquals(147, Transaction.MemEstimator.estimateSize(tx));
        assertEquals(61, (long) tx.getChainId());
        assertEquals(510, Transaction.MemEstimator.estimateSize(tx));
        tx.purgeData();
        assertEquals(195, Transaction.MemEstimator.estimateSize(tx)); // rlp + hash
        assertArrayEquals(RLP_SIGNED_TX, tx.rlpEncoded);
        assertEquals(61, (long) tx.getChainId());
        tx.purgeEncoded();
        assertEquals(331, Transaction.MemEstimator.estimateSize(tx));
        assertArrayEquals(RLP_SIGNED_TX, tx.getEncoded());
    }

    @Test
    public void purgeUnsignedTransactionTest() {
        Transaction tx = new Transaction(RLP_UNSIGNED_TX);
        assertEquals(80, Transaction.MemEstimator.estimateSize(tx));
        assertEquals(333333, (long) tx.getChainId());
        assertEquals(232, Transaction.MemEstimator.estimateSize(tx));
        tx.purgeData();
        assertEquals(128, Transaction.MemEstimator.estimateSize(tx)); // rlp + hash
        assertArrayEquals(RLP_UNSIGNED_TX, tx.rlpEncoded);
        assertEquals(333333, (long) tx.getChainId());
        tx.purgeEncoded();
        assertEquals(120, Transaction.MemEstimator.estimateSize(tx));
        assertArrayEquals(RLP_UNSIGNED_TX, tx.getEncoded());
    }

    @Test
    public void purgeValue() {
        String test1 = "Test";
        Value v1 = new Value(test1);
        v1.purgeData();
        v1.purgeEncoded();
        assertEquals(test1, v1.asString());

        byte[] test2 = RLP.encodeInt(10);
        Value v2 = new Value(test2);
        v2.purgeData();
        v2.purgeEncoded();
        assertEquals(10, v2.asInt());
    }

    @Test
    public void purgeTransactionReceipt() {
        byte[] rlp = Hex.decode("f88ba0966265cc49fa1f10f0445f035258d116563931022a3570a640af5d73a214a8da822b6fb84000000010000000010000000000008000000000000000000000000000000000000000000000000000000000020000000000000014000000000400000000000440d8d7948513d39a34a1a8570c9c9f0af2cba79ac34e0ac8c0808301e2408687342343789880");
        TransactionReceipt txReceipt = new TransactionReceipt(rlp);
        assertEquals(660, TransactionReceipt.MemEstimator.estimateSize(txReceipt));
        txReceipt.purgeData();
        txReceipt.purgeEncoded();
        assertEquals(503, TransactionReceipt.MemEstimator.estimateSize(txReceipt));  // Rlp removed
        assertArrayEquals(rlp, txReceipt.getEncoded());
        assertEquals(660, TransactionReceipt.MemEstimator.estimateSize(txReceipt));  // Rlp added again
    }
}
