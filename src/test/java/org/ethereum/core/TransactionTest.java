package org.ethereum.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

public class TransactionTest {

	private static String RLP_ENCODED_RAW_TX = "ed808b00d3c21bcecceda10000009479b08ad8787060333663d19704909ee7b1903e588609184e72a00082425580";
	private static String HASH_RAW_TX = "c957fce141839221403b51d26a5de186db2dabe0de4ac48f3f6718bfeb7c5f47";
	private static String RLP_ENCODED_SIGNED_TX = "f870808b00d3c21bcecceda10000009479b08ad8787060333663d19704909ee7b1903e588609184e72a000824255801ca08e7dfa371b0acde61f894f1969f1f17696b86492a8572c60154e85d7801a4a08a0229807de94c4cfa63d978ff22f764cd9e6abd1bae1bcdba4aa4ae299ad0a8a9f";
	
    @Test /* sign transaction  https://tools.ietf.org/html/rfc6979 */
    public void test1() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, IOException {

        //python taken exact data
        String txRLPRawData = "a9e880872386f26fc1000085e8d4a510008203e89413978aee95f38490e9769c39b2773ed763d9cd5f80";
        // String txRLPRawData = "f82804881bc16d674ec8000094cd2a3d9f938e13cd947ec05abc7fe734df8dd8268609184e72a0006480";

        byte[] cowPrivKey = Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4");
        ECKey key = ECKey.fromPrivate(cowPrivKey);

        byte[] data    = Hex.decode(txRLPRawData);

        // step 1: serialize + RLP encode
        // step 2: hash = sha3(step1)
        byte[] txHash = HashUtil.sha3(data);

        String signature = key.doSign(txHash).toBase64();
        System.out.println(signature);
    }

    @Test  /* achieve public key of the sender */
    public void test2() throws Exception {

        // cat --> 79B08AD8787060333663D19704909EE7B1903E58
        // cow --> CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826

        BigInteger value = new BigInteger("1000000000000000000000000");

        byte[] privKey = HashUtil.sha3("cat".getBytes());
        Address receiveAddress = new Address(privKey);

        byte[] senderPrivKey = HashUtil.sha3("cow".getBytes());

        byte[] gasPrice=  Hex.decode("09184e72a000");
        byte[] gas =      Hex.decode("4255");

        Transaction tx = new Transaction(null, value.toByteArray(),
                receiveAddress.getAddress(),  gasPrice, gas, null);

        tx.sign(senderPrivKey);

        System.out.println("v\t\t\t: " + Hex.toHexString(new byte[] { tx.getSignature().v }));
        System.out.println("r\t\t\t: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().r)));
        System.out.println("s\t\t\t: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().s)));

        System.out.println("RLP encoded tx\t\t: " + Hex.toHexString( tx.getEncodedSigned() ));

        // retrieve the signer/sender of the transaction
        ECKey key = ECKey.signatureToKey(tx.getHash(), tx.getSignature().toBase64());

        System.out.println("Tx unsigned RLP\t\t: " + Hex.toHexString( tx.getEncoded()));
        System.out.println("Tx signed   RLP\t\t: " + Hex.toHexString( tx.getEncodedSigned() ));

        System.out.println("Signature public key\t: " + Hex.toHexString(key.getPubKey()));
        System.out.println("Sender is\t\t: " + Hex.toHexString(key.getAddress()));

        Assert.assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826",
                Hex.toHexString(key.getAddress()));
    }

    @Test /* encode transaction */
    public void test3() throws Exception {

        BigInteger value = new BigInteger("1000000000000000000000000");

        byte[] privKey = HashUtil.sha3("cat".getBytes());
        Address receiveAddress = new Address(privKey);

        byte[] gasPrice=  Hex.decode("09184e72a000");
        byte[] gas =      Hex.decode("4255");

        Transaction tx = new Transaction(null, value.toByteArray(),
                receiveAddress.getAddress(),  gasPrice, gas, null);

        tx.sign(privKey);
        byte[] payload = tx.getEncodedSigned();

        System.out.println(Hex.toHexString(  payload ));
    }
	
	@Test
	public void testTransactionFromRLP() {
    	// from RLP encoding
		fail("Double check the expected values, they don't seem to be parsed in the right order.");
    	byte[] encodedTxBytes = Hex.decode(RLP_ENCODED_SIGNED_TX);
    	Transaction tx = new Transaction(encodedTxBytes);
    	assertNull(tx.getNonce());
    	assertEquals(BigInteger.valueOf(16981), new BigInteger(tx.getValue()));
    	assertEquals("09184e72a000", ByteUtil.toHexString(tx.getReceiveAddress()));
    	assertEquals("00d3c21bcecceda1000000", ByteUtil.toHexString(tx.getGasPrice()));
    	assertEquals("79b08ad8787060333663d19704909ee7b1903e58", ByteUtil.toHexString(tx.getGasLimit()));
    	assertNull(tx.getData());
    	assertNull(tx.getInit());
    	assertEquals(28, tx.getSignature().v);
    	assertEquals("8e7dfa371b0acde61f894f1969f1f17696b86492a8572c60154e85d7801a4a08", Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().r)));
    	assertEquals("229807de94c4cfa63d978ff22f764cd9e6abd1bae1bcdba4aa4ae299ad0a8a9f", Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().s)));
    	
    	assertEquals(HASH_RAW_TX, ByteUtil.toHexString(tx.getHash()));
    	assertEquals(RLP_ENCODED_RAW_TX, ByteUtil.toHexString(tx.getEncoded()));
    	assertEquals(RLP_ENCODED_SIGNED_TX, ByteUtil.toHexString(tx.getEncodedSigned()));
	}

	@Test
	public void testTransactionFromNew() throws Exception {
        byte[] privKeyBytes = Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4");
        
        String RLP_TX_UNSIGNED = "e88085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc1000080";
        String RLP_TX_SIGNED = "f86b8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc10000801ba0eab47c1a49bf2fe5d40e01d313900e19ca485867d462fe06e139e3a536c6d4f4a014a569d327dcda4b29f74f93c0e9729d2f49ad726e703f9cd90dbb0fbf6649f1";
        String HASH_TX_UNSIGNED = "328ea6d24659dec48adea1aced9a136e5ebdf40258db30d1b1d97ed2b74be34e";
        
		byte[] nonce			= BigIntegers.asUnsignedByteArray(BigInteger.ZERO);
		byte[] gasPrice			= Hex.decode("e8d4a51000");		// 1000000000000
		byte[] gas				= Hex.decode("2710");			// 10000
		byte[] recieveAddress	= Hex.decode("13978aee95f38490e9769c39b2773ed763d9cd5f");
		byte[] value			= Hex.decode("2386f26fc10000"); //10000000000000000"
		byte[] data 			= new byte[0];
		
    	Transaction tx = new Transaction(nonce, gasPrice, gas, recieveAddress, value, data);
    	    	
    	// Testing unsigned
    	String encodedUnsigned = Hex.toHexString(tx.getEncoded());
        assertEquals(RLP_TX_UNSIGNED, encodedUnsigned);
        assertEquals(HASH_TX_UNSIGNED, Hex.toHexString(tx.getHash()));

        // Testing signed
        tx.sign(privKeyBytes);
        String encodedSigned = Hex.toHexString(tx.getEncodedSigned());       
        assertEquals(RLP_TX_SIGNED, encodedSigned);
        assertEquals(HASH_TX_UNSIGNED, Hex.toHexString(tx.getHash()));
	}
}
