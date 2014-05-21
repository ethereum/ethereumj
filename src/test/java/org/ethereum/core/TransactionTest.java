package org.ethereum.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

public class TransactionTest {

	private static String RLP_ENCODED_TX = "e88085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc1000080";
	private static String HASH_RAW_TX = "";
	private static String HASH_SIGNED_TX = "";
	
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
		
    	byte[] encodedTxBytes = Hex.decode(RLP_ENCODED_TX);
    	Transaction tx = new Transaction(encodedTxBytes);
    	assertNull(Hex.toHexString(tx.getNonce()));
    	assertNull(Hex.toHexString(tx.getValue()));
    	assertEquals(RLP_ENCODED_TX, Hex.toHexString(tx.getReceiveAddress()));
    	assertEquals(RLP_ENCODED_TX, Hex.toHexString(tx.getGasPrice()));
    	assertEquals(RLP_ENCODED_TX, Hex.toHexString(tx.getGasLimit()));
    	assertEquals(RLP_ENCODED_TX, Hex.toHexString(tx.getData()));
    	assertEquals(RLP_ENCODED_TX, Hex.toHexString(tx.getInit()));
    	assertEquals(28, tx.getSignature().v);
    	assertEquals("c2604bd6eeca76afce4e7775d87960e3d4ed3b69235a3f94d6f1497c9831b50c", tx.getSignature().r);
    	assertEquals("664124a6b323350dd57a650434dc6bf8ddf37cd1a2686fee377e512aa12f1214", tx.getSignature().s);
    	
    	assertEquals(RLP_ENCODED_TX, Hex.toHexString(tx.getEncodedSigned()));
	}

	@Test
	public void testTransactionFromNew() throws Exception {
        byte[] privKeyBytes = Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4");
        
        String RLP_TX_UNSIGNED = "eb8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc1000080";
        String RLP_TX_SIGNED = "f86b8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc10000801ba0eab47c1a49bf2fe5d40e01d313900e19ca485867d462fe06e139e3a536c6d4f4a014a569d327dcda4b29f74f93c0e9729d2f49ad726e703f9cd90dbb0fbf6649f1";
        
		byte[] nonce			= BigIntegers.asUnsignedByteArray(BigInteger.ZERO);
		byte[] gasPrice			= Hex.decode("e8d4a51000");		// 1000000000000
		byte[] gas				= Hex.decode("2710");			// 10000
		byte[] recieveAddress	= Hex.decode("13978aee95f38490e9769c39b2773ed763d9cd5f");
		byte[] value			= Hex.decode("2386f26fc10000"); //10000000000000000"
		byte[] data 			= new byte[0];
		
    	Transaction tx = new Transaction(nonce, gasPrice, gas, recieveAddress, value, data);
    	    	
    	// Testing unsigned
    	String encodedUnsigned = Hex.toHexString(tx.getEncoded());
    	
    	System.out.println(RLP_TX_UNSIGNED);
    	System.out.println(encodedUnsigned);
    	
        assertEquals(RLP_TX_UNSIGNED, encodedUnsigned);
        assertEquals(HASH_RAW_TX, Hex.toHexString(tx.getHash()));

        // Testing signed
        tx.sign(privKeyBytes);
        String encodedSigned = Hex.toHexString(tx.getEncodedSigned());
    	System.out.println(RLP_TX_SIGNED);
    	System.out.println(encodedSigned);
        
        assertEquals(RLP_TX_SIGNED, encodedSigned);
        assertEquals(HASH_RAW_TX, Hex.toHexString(tx.getHash()));
	}
}
