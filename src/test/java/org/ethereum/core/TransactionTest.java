package org.ethereum.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.ECKey.MissingPrivateKeyException;
import org.ethereum.crypto.HashUtil;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

public class TransactionTest {

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

        // cat --> 79b08ad8787060333663d19704909ee7b1903e58
        // cow --> cd2a3d9f938e13cd947ec05abc7fe734df8dd826

        BigInteger value = new BigInteger("1000000000000000000000");

        byte[] privKey = HashUtil.sha3("cat".getBytes());
        ECKey ecKey = ECKey.fromPrivate(privKey);

        byte[] senderPrivKey = HashUtil.sha3("cow".getBytes());

        byte[] gasPrice=  Hex.decode("09184e72a000");
        byte[] gas =      Hex.decode("4255");

        // Tn (nonce); Tp(pgas); Tg(gaslimi); Tt(value); Tv(value); Ti(sender);  Tw; Tr; Ts
        Transaction tx = new Transaction(null, gasPrice, gas, ecKey.getAddress(),
                value.toByteArray(),
                   null);

        tx.sign(senderPrivKey);

        System.out.println("v\t\t\t: " + Hex.toHexString(new byte[] { tx.getSignature().v }));
        System.out.println("r\t\t\t: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().r)));
        System.out.println("s\t\t\t: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().s)));

        System.out.println("RLP encoded tx\t\t: " + Hex.toHexString( tx.getEncoded() ));

        // retrieve the signer/sender of the transaction
        ECKey key = ECKey.signatureToKey(tx.getHash(), tx.getSignature().toBase64());

        System.out.println("Tx unsigned RLP\t\t: " + Hex.toHexString( tx.getEncodedRaw()));
        System.out.println("Tx signed   RLP\t\t: " + Hex.toHexString( tx.getEncoded() ));

        System.out.println("Signature public key\t: " + Hex.toHexString(key.getPubKey()));
        System.out.println("Sender is\t\t: " + Hex.toHexString(key.getAddress()));

        Assert.assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826",
                Hex.toHexString(key.getAddress()));

        System.out.println(tx.toString());
    }


    @Test  /* achieve public key of the sender nonce: 01 */
    public void test3() throws Exception {

        // cat --> 79b08ad8787060333663d19704909ee7b1903e58
        // cow --> cd2a3d9f938e13cd947ec05abc7fe734df8dd826

		ECKey ecKey = ECKey.fromPrivate(HashUtil.sha3("cat".getBytes()));
        byte[] senderPrivKey = HashUtil.sha3("cow".getBytes());
        
		byte[] nonce = { 0x01 };
		byte[] gasPrice = Hex.decode("09184e72a000");
		byte[] gasLimit = Hex.decode("4255");
        BigInteger value = new BigInteger("1000000000000000000000000");

		Transaction tx = new Transaction(nonce, gasPrice, gasLimit,
				ecKey.getAddress(), value.toByteArray(), null);

        tx.sign(senderPrivKey);

        System.out.println("v\t\t\t: " + Hex.toHexString(new byte[] { tx.getSignature().v }));
        System.out.println("r\t\t\t: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().r)));
        System.out.println("s\t\t\t: " + Hex.toHexString(BigIntegers.asUnsignedByteArray(tx.getSignature().s)));

        System.out.println("RLP encoded tx\t\t: " + Hex.toHexString( tx.getEncoded() ));

        // retrieve the signer/sender of the transaction
        ECKey key = ECKey.signatureToKey(tx.getHash(), tx.getSignature().toBase64());

        System.out.println("Tx unsigned RLP\t\t: " + Hex.toHexString( tx.getEncodedRaw()));
        System.out.println("Tx signed   RLP\t\t: " + Hex.toHexString( tx.getEncoded() ));

        System.out.println("Signature public key\t: " + Hex.toHexString(key.getPubKey()));
        System.out.println("Sender is\t\t: " + Hex.toHexString(key.getAddress()));

        Assert.assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826",
                Hex.toHexString(key.getAddress()));
    }

	// Testdata from: https://github.com/ethereum/tests/blob/master/txtest.json
    String RLP_ENCODED_RAW_TX = "e88085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc1000080";
	String RLP_ENCODED_UNSIGNED_TX = "eb8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc1000080808080";
	String HASH_TX = "328ea6d24659dec48adea1aced9a136e5ebdf40258db30d1b1d97ed2b74be34e";
	String RLP_ENCODED_SIGNED_TX = "f86b8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc10000801ba0eab47c1a49bf2fe5d40e01d313900e19ca485867d462fe06e139e3a536c6d4f4a014a569d327dcda4b29f74f93c0e9729d2f49ad726e703f9cd90dbb0fbf6649f1";
	String KEY = "c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4";
	byte[] testNonce = Hex.decode("");
	byte[] testGasPrice = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(1000000000000L));;
	byte[] testGasLimit = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(10000));;
	byte[] testReceiveAddress = Hex.decode("13978aee95f38490e9769c39b2773ed763d9cd5f");
	byte[] testValue = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(10000000000000000L));
	byte[] testData = Hex.decode("");
	byte[] testInit = Hex.decode("");
	
	@Test
	public void testTransactionFromSignedRLP() throws Exception {
    	Transaction txSigned = new Transaction(Hex.decode(RLP_ENCODED_SIGNED_TX));
    	
    	assertEquals(HASH_TX, Hex.toHexString(txSigned.getHash()));
    	assertEquals(RLP_ENCODED_SIGNED_TX, Hex.toHexString(txSigned.getEncoded()));
    	
    	assertNull(txSigned.getNonce());
    	assertEquals(new BigInteger(1, testGasPrice), new BigInteger(1, txSigned.getGasPrice()));
    	assertEquals(new BigInteger(1, testGasLimit), new BigInteger(1, txSigned.getGasLimit()));
    	assertEquals(Hex.toHexString(testReceiveAddress), Hex.toHexString(txSigned.getReceiveAddress()));
    	assertEquals(new BigInteger(1, testValue), new BigInteger(1, txSigned.getValue()));
    	assertNull(txSigned.getData());
    	assertNull(txSigned.getInit());
    	assertEquals(27, txSigned.getSignature().v);
    	assertEquals("eab47c1a49bf2fe5d40e01d313900e19ca485867d462fe06e139e3a536c6d4f4", Hex.toHexString(BigIntegers.asUnsignedByteArray(txSigned.getSignature().r)));
    	assertEquals("14a569d327dcda4b29f74f93c0e9729d2f49ad726e703f9cd90dbb0fbf6649f1", Hex.toHexString(BigIntegers.asUnsignedByteArray(txSigned.getSignature().s)));
	}
	
	@Test
	public void testTransactionFromUnsignedRLP() throws Exception {
    	Transaction txUnsigned = new Transaction(Hex.decode(RLP_ENCODED_UNSIGNED_TX));
    	
    	assertEquals(HASH_TX, Hex.toHexString(txUnsigned.getHash()));
    	assertEquals(RLP_ENCODED_UNSIGNED_TX, Hex.toHexString(txUnsigned.getEncoded()));
    	txUnsigned.sign(Hex.decode(KEY));
    	assertEquals(RLP_ENCODED_SIGNED_TX, Hex.toHexString(txUnsigned.getEncoded()));   	
    	
    	assertNull(txUnsigned.getNonce());
    	assertEquals(new BigInteger(1, testGasPrice), new BigInteger(1, txUnsigned.getGasPrice()));
    	assertEquals(new BigInteger(1, testGasLimit), new BigInteger(1, txUnsigned.getGasLimit()));
    	assertEquals(Hex.toHexString(testReceiveAddress), Hex.toHexString(txUnsigned.getReceiveAddress()));
    	assertEquals(new BigInteger(1, testValue), new BigInteger(1, txUnsigned.getValue()));
    	assertNull(txUnsigned.getData());
    	assertNull(txUnsigned.getInit());
    	assertEquals(27, txUnsigned.getSignature().v);
    	assertEquals("eab47c1a49bf2fe5d40e01d313900e19ca485867d462fe06e139e3a536c6d4f4", Hex.toHexString(BigIntegers.asUnsignedByteArray(txUnsigned.getSignature().r)));
    	assertEquals("14a569d327dcda4b29f74f93c0e9729d2f49ad726e703f9cd90dbb0fbf6649f1", Hex.toHexString(BigIntegers.asUnsignedByteArray(txUnsigned.getSignature().s)));
	}
	
	@Test
	public void testTransactionFromNew1() throws MissingPrivateKeyException {
    	Transaction txNew = new Transaction(testNonce, testGasPrice, testGasLimit, testReceiveAddress, testValue, testData);
    	
    	assertEquals("", Hex.toHexString(txNew.getNonce()));
    	assertEquals(new BigInteger(1, testGasPrice), new BigInteger(1, txNew.getGasPrice()));
    	assertEquals(new BigInteger(1, testGasLimit), new BigInteger(1, txNew.getGasLimit()));
    	assertEquals(Hex.toHexString(testReceiveAddress), Hex.toHexString(txNew.getReceiveAddress()));
    	assertEquals(new BigInteger(1, testValue), new BigInteger(1, txNew.getValue()));
    	assertEquals("", Hex.toHexString(txNew.getData()));
    	assertNull(txNew.getInit());
    	assertNull(txNew.getSignature());
    	
    	assertEquals(RLP_ENCODED_RAW_TX, Hex.toHexString(txNew.getEncodedRaw()));
    	assertEquals(HASH_TX, Hex.toHexString(txNew.getHash()));
    	assertEquals(RLP_ENCODED_UNSIGNED_TX, Hex.toHexString(txNew.getEncoded()));
    	txNew.sign(Hex.decode(KEY));
    	assertEquals(RLP_ENCODED_SIGNED_TX, Hex.toHexString(txNew.getEncoded()));   	
    	
    	assertEquals(27, txNew.getSignature().v);
    	assertEquals("eab47c1a49bf2fe5d40e01d313900e19ca485867d462fe06e139e3a536c6d4f4", Hex.toHexString(BigIntegers.asUnsignedByteArray(txNew.getSignature().r)));
    	assertEquals("14a569d327dcda4b29f74f93c0e9729d2f49ad726e703f9cd90dbb0fbf6649f1", Hex.toHexString(BigIntegers.asUnsignedByteArray(txNew.getSignature().s)));
	}
	
    @Test
	public void testTransactionFromNew2() throws MissingPrivateKeyException {
        byte[] privKeyBytes = Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4");
        
        String RLP_TX_UNSIGNED = "eb8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc1000080808080";
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
        String encodedSigned = Hex.toHexString(tx.getEncoded());       
        assertEquals(RLP_TX_SIGNED, encodedSigned);
        assertEquals(HASH_TX_UNSIGNED, Hex.toHexString(tx.getHash()));
	}
}
