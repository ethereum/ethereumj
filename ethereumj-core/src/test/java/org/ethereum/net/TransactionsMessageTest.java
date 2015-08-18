package org.ethereum.net;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.eth.message.TransactionsMessage;
import org.ethereum.util.ByteUtil;

import org.junit.Ignore;
import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

public class TransactionsMessageTest {

    /* TRANSACTIONS */

    @Ignore
    @Test  /* Transactions message 1 */
    public void test_1() {

        String txsPacketRaw = "f86e12f86b04648609184e72a00094cd2a3d9f938e13cd947ec05abc7fe734df8dd826"
                + "881bc16d674ec80000801ba05c89ebf2b77eeab88251e553f6f9d53badc1d800"
                + "bbac02d830801c2aa94a4c9fa00b7907532b1f29c79942b75fff98822293bf5f"
                + "daa3653a8d9f424c6a3265f06c";

        byte[] payload = Hex.decode(txsPacketRaw);

        TransactionsMessage transactionsMessage = new TransactionsMessage(payload);
        System.out.println(transactionsMessage);

        assertEquals(EthMessageCodes.TRANSACTIONS, transactionsMessage.getCommand());
        assertEquals(1, transactionsMessage.getTransactions().size());

        Transaction tx = transactionsMessage.getTransactions().iterator().next();

        assertEquals("5d2aee0490a9228024158433d650335116b4af5a30b8abb10e9b7f9f7e090fd8", Hex.toHexString(tx.getHash()));
        assertEquals("04", Hex.toHexString(tx.getNonce()));
        assertEquals("1bc16d674ec80000", Hex.toHexString(tx.getValue()));
        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(tx.getReceiveAddress()));
        assertEquals("64", Hex.toHexString(tx.getGasPrice()));
        assertEquals("09184e72a000", Hex.toHexString(tx.getGasLimit()));
        assertEquals("", ByteUtil.toHexString(tx.getData()));

        assertEquals("1b", Hex.toHexString(new byte[]{tx.getSignature().v}));
        assertEquals("5c89ebf2b77eeab88251e553f6f9d53badc1d800bbac02d830801c2aa94a4c9f", Hex.toHexString(tx.getSignature().r.toByteArray()));
        assertEquals("0b7907532b1f29c79942b75fff98822293bf5fdaa3653a8d9f424c6a3265f06c", Hex.toHexString(tx.getSignature().s.toByteArray()));
    }

    @Ignore
    @Test  /* Transactions message 2 */
    public void test_2() {

        String txsPacketRaw = "f9025012f89d8080940000000000000000000000000000000000000000860918"
                + "4e72a000822710b3606956330c0d630000003359366000530a0d630000003359"
                + "602060005301356000533557604060005301600054630000000c588433606957"
                + "1ca07f6eb94576346488c6253197bde6a7e59ddc36f2773672c849402aa9c402"
                + "c3c4a06d254e662bf7450dd8d835160cbb053463fed0b53f2cdd7f3ea8731919"
                + "c8e8ccf901050180940000000000000000000000000000000000000000860918"
                + "4e72a000822710b85336630000002e59606956330c0d63000000155933ff3356"
                + "0d63000000275960003356576000335700630000005358600035560d63000000"
                + "3a590033560d63000000485960003356573360003557600035335700b84a7f4e"
                + "616d655265670000000000000000000000000000000000000000000000000030"
                + "57307f4e616d6552656700000000000000000000000000000000000000000000"
                + "00000057336069571ba04af15a0ec494aeac5b243c8a2690833faa74c0f73db1"
                + "f439d521c49c381513e9a05802e64939be5a1f9d4d614038fbd5479538c48795"
                + "614ef9c551477ecbdb49d2f8a6028094ccdeac59d35627b7de09332e819d5159"
                + "e7bb72508609184e72a000822710b84000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000002d0aceee"
                + "7e5ab874e22ccf8d1a649f59106d74e81ba0d05887574456c6de8f7a0d172342"
                + "c2cbdd4cf7afe15d9dbb8b75b748ba6791c9a01e87172a861f6c37b5a9e3a5d0"
                + "d7393152a7fbe41530e5bb8ac8f35433e5931b";

        byte[] payload = Hex.decode(txsPacketRaw);

        TransactionsMessage transactionsMessage = new TransactionsMessage(payload);
        System.out.println(transactionsMessage);

        assertEquals(EthMessageCodes.TRANSACTIONS, transactionsMessage.getCommand());

        assertEquals(3, transactionsMessage.getTransactions().size());

        Iterator<Transaction> txIter = transactionsMessage.getTransactions().iterator();
        Transaction tx1 = txIter.next();
        txIter.next(); // skip one
        Transaction tx3 = txIter.next();

        assertEquals("1b9d9456293cbcbc2f28a0fdc67028128ea571b033fb0e21d0ee00bcd6167e5d",
                Hex.toHexString(tx3.getHash()));

        assertEquals("00",
                Hex.toHexString(tx3.getNonce()));

        assertEquals("2710",
                Hex.toHexString(tx3.getValue()));

        assertEquals("09184e72a000",
                Hex.toHexString(tx3.getReceiveAddress()));

        assertNull(tx3.getGasPrice());

        assertEquals("0000000000000000000000000000000000000000",
                Hex.toHexString(tx3.getGasLimit()));

        assertEquals("606956330c0d630000003359366000530a0d630000003359602060005301356000533557604060005301600054630000000c58",
                Hex.toHexString(tx3.getData()));

        assertEquals("33",
                Hex.toHexString(new byte[]{tx3.getSignature().v}));

        assertEquals("1c",
                Hex.toHexString(tx3.getSignature().r.toByteArray()));

        assertEquals("7f6eb94576346488c6253197bde6a7e59ddc36f2773672c849402aa9c402c3c4",
                Hex.toHexString(tx3.getSignature().s.toByteArray()));

        // Transaction #2

        assertEquals("dde9543921850f41ca88e5401322cd7651c78a1e4deebd5ee385af8ac343f0ad",
                Hex.toHexString(tx1.getHash()));

        assertEquals("02",
                Hex.toHexString(tx1.getNonce()));

        assertEquals("2710",
                Hex.toHexString(tx1.getValue()));

        assertEquals("09184e72a000",
                Hex.toHexString(tx1.getReceiveAddress()));

        assertNull(tx1.getGasPrice());

        assertEquals("ccdeac59d35627b7de09332e819d5159e7bb7250",
                Hex.toHexString(tx1.getGasLimit()));

        assertEquals
                ("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000002d0aceee7e5ab874e22ccf8d1a649f59106d74e8",
                        Hex.toHexString(tx1.getData()));

        assertEquals("1b",
                Hex.toHexString(new byte[]{tx1.getSignature().v}));

        assertEquals("00d05887574456c6de8f7a0d172342c2cbdd4cf7afe15d9dbb8b75b748ba6791c9",
                Hex.toHexString(tx1.getSignature().r.toByteArray()));

        assertEquals("1e87172a861f6c37b5a9e3a5d0d7393152a7fbe41530e5bb8ac8f35433e5931b",
                Hex.toHexString(tx1.getSignature().s.toByteArray()));
    }

    @Test /* Transactions msg encode */
    public void test_3() throws Exception {

        String expected =
                "f872f870808b00d3c21bcecceda10000009479b08ad8787060333663d19704909ee7b1903e588609184e72a000824255801ca00f410a70e42b2c9854a8421d32c87c370a2b9fff0a27f9f031bb4443681d73b5a018a7dc4c4f9dee9f3dc35cb96ca15859aa27e219a8e4a8547be6bd3206979858";

        BigInteger value = new BigInteger("1000000000000000000000000");

        byte[] privKey = HashUtil.sha3("cat".getBytes());
        ECKey ecKey = ECKey.fromPrivate(privKey);

        byte[] gasPrice = Hex.decode("09184e72a000");
        byte[] gas = Hex.decode("4255");

        Transaction tx = new Transaction(null, value.toByteArray(),
                ecKey.getAddress(), gasPrice, gas, null);

        tx.sign(privKey);
        tx.getEncoded();

        Set<Transaction> txs = new HashSet<>(Arrays.asList(tx));
        TransactionsMessage transactionsMessage = new TransactionsMessage(txs);

        assertEquals(expected, Hex.toHexString(transactionsMessage.getEncoded()));
    }
}

