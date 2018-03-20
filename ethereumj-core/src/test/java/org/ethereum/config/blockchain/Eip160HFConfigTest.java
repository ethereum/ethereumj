/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
 *
 *
 */

package org.ethereum.config.blockchain;

import org.ethereum.config.Constants;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

import static java.math.BigInteger.valueOf;
import static org.junit.Assert.*;

/**
 <pre>
 Specification

 If block.number >= FORK_BLKNUM, increase the gas cost of EXP from 10 + 10 per byte in the
 exponent to 10 + 50 per byte in the exponent.
 </pre>
 *
 * https://github.com/ethereum/EIPs/blob/master/EIPS/eip-160.md
 */
public class Eip160HFConfigTest {
    private byte[] emptyBytes = new byte[]{};

    @Test
    public void testGetGasCost() throws Exception {
        TestBlockchainConfig parent = new TestBlockchainConfig();
        Eip160HFConfig config = new Eip160HFConfig(parent);
        assertTrue(config.getGasCost() instanceof Eip160HFConfig.GasCostEip160HF);
        assertEquals(50, config.getGasCost().getEXP_BYTE_GAS());
    }

    @Test
    public void testMaxContractSizeIsOnlyChangeInEip160() throws Exception {
        TestBlockchainConfig parent = new TestBlockchainConfig();
        Eip160HFConfig config = new Eip160HFConfig(parent);
        assertEquals(0x6000, config.getConstants().getMAX_CONTRACT_SZIE());

        Constants expected = parent.getConstants();
        Constants actual = config.getConstants();
        assertEquals(expected.getInitialNonce(), actual.getInitialNonce());
        assertEquals(expected.getMINIMUM_DIFFICULTY(), actual.getMINIMUM_DIFFICULTY());
        assertEquals(expected.getDIFFICULTY_BOUND_DIVISOR(), actual.getDIFFICULTY_BOUND_DIVISOR());
        assertEquals(expected.getMAXIMUM_EXTRA_DATA_SIZE(), actual.getMAXIMUM_EXTRA_DATA_SIZE());
        assertEquals(expected.getBLOCK_REWARD(), actual.getBLOCK_REWARD());
        assertEquals(expected.getEXP_DIFFICULTY_PERIOD(), actual.getEXP_DIFFICULTY_PERIOD());
        assertEquals(expected.getGAS_LIMIT_BOUND_DIVISOR(), actual.getGAS_LIMIT_BOUND_DIVISOR());
        assertEquals(expected.getUNCLE_GENERATION_LIMIT(), actual.getUNCLE_GENERATION_LIMIT());
        assertEquals(expected.getUNCLE_LIST_LIMIT(), actual.getUNCLE_LIST_LIMIT());
        assertEquals(expected.getDURATION_LIMIT(), actual.getDURATION_LIMIT());
        assertEquals(expected.getBEST_NUMBER_DIFF_LIMIT(), actual.getBEST_NUMBER_DIFF_LIMIT());
        assertEquals(expected.createEmptyContractOnOOG(), actual.createEmptyContractOnOOG());
        assertEquals(expected.hasDelegateCallOpcode(), actual.hasDelegateCallOpcode());
    }

    /**
     * See also {@link org.ethereum.core.TransactionTest#afterEIP158Test} which tests the
     * EIP-155 'signature.v' specification changes.
     */
    @Test
    public void testAcceptTransactionWithSignature() throws Exception {
        Transaction tx = Transaction.create(
                "3535353535353535353535353535353535353535",
                new BigInteger("1000000000000000000"),
                new BigInteger("9"),
                new BigInteger("20000000000"),
                new BigInteger("21000"),
                1);

        ECKey ecKey = ECKey.fromPrivate(Hex.decode("4646464646464646464646464646464646464646464646464646464646464646"));
        tx.sign(ecKey);

        TestBlockchainConfig parent = new TestBlockchainConfig();
        Eip160HFConfig config = new Eip160HFConfig(parent);
        assertTrue(config.acceptTransactionSignature(tx));
    }

    @Test
    public void testDenyTransactionWithInvalidSignature() throws Exception {
        Transaction tx = Transaction.create(
                "3535353535353535353535353535353535353535",
                new BigInteger("1000000000000000000"),
                new BigInteger("9"),
                new BigInteger("20000000000"),
                new BigInteger("21000"),
                1);

        ECKey ecKey = ECKey.fromPrivate(Hex.decode("4646464646464646464646464646464646464646464646464646464646464646"));
        tx.sign(ecKey);

        TestBlockchainConfig parent = new TestBlockchainConfig();
        Eip160HFConfig config = new Eip160HFConfig(parent);

        // Corrupt the signature and assert it's *not* accepted
        tx.getSignature().v = 99;
        assertFalse(config.acceptTransactionSignature(tx));
    }

    @Test
    public void testDenyTransactionWithoutSignature() throws Exception {
        TestBlockchainConfig parent = new TestBlockchainConfig();
        Eip160HFConfig config = new Eip160HFConfig(parent);
        Transaction txWithoutSignature = new Transaction(emptyBytes, emptyBytes, emptyBytes, emptyBytes, emptyBytes, emptyBytes, null);
        assertFalse(config.acceptTransactionSignature(txWithoutSignature));
    }

}