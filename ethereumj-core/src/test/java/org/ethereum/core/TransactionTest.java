package org.ethereum.core;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class TransactionTest {

	private static String RLP_ENCODED_TX = "f88b8085e8d4a510008227109413978aee95f38490e9769c39b2773ed763d9cd5f872386f26fc10000a0c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a4701ca00502e84be138ca397e49f96e8c82e5a99afc09e0ea4582cc109ea221eeb479efa078f18d645b39ec44778c12ffc4b0";
	private static String RLP_ENCODED_TX2 = "f8ccf8a6808609184e72a0008227109491a10664d0cd489085a7a018beb5245d4f2272f180b840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011ca0c2604bd6eeca76afce4e7775d87960e3d4ed3b69235a3f94d6f1497c9831b50ca0664124a6b323350dd57a650434dc6bf8ddf37cd1a2686fee377e512aa12f1214a0c84f20b2df6abd635babd7af64cd76756cd4e39c0ccb87eaaaad609f21d1fe51820334";
	private static String HASH_RAW_TX = "";
	private static String HASH_SIGNED_TX = "";
	
	@Test
	public void testTransactionFromRLP() {
    	// from RLP encoding
		
    	byte[] encodedTxBytes = Hex.decode(RLP_ENCODED_TX2);
    	Transaction tx = new Transaction(encodedTxBytes);
    	assertNull(Hex.toHexString(tx.getNonce()));
    	assertNull(Hex.toHexString(tx.getValue()));
    	assertEquals(RLP_ENCODED_TX2, Hex.toHexString(tx.getReceiveAddress()));
    	assertEquals(RLP_ENCODED_TX2, Hex.toHexString(tx.getGasPrice()));
    	assertEquals(RLP_ENCODED_TX2, Hex.toHexString(tx.getGasLimit()));
    	assertEquals(RLP_ENCODED_TX2, Hex.toHexString(tx.getData()));
    	assertEquals(RLP_ENCODED_TX2, Hex.toHexString(tx.getInit()));
    	assertEquals(28, tx.getSignature().v);
    	assertEquals("c2604bd6eeca76afce4e7775d87960e3d4ed3b69235a3f94d6f1497c9831b50c", tx.getSignature().r);
    	assertEquals("664124a6b323350dd57a650434dc6bf8ddf37cd1a2686fee377e512aa12f1214", tx.getSignature().s);
    	
    	assertEquals(RLP_ENCODED_TX2, Hex.toHexString(tx.getEncoded()));
	}

	@Test
	public void testTransactionFromNew() throws Exception {
        byte[] privKeyBytes = Hex.decode("3ecb44df2159c26e0f995712d4f39b6f6e499b40749b1cf1246c37f9516cb6a4");
        
//        nonce=0, gasprice=10 ** 12, startgas=10000, to=, value=10 ** 16, data='').sign(k)
//		byte[] nonce			= BigInteger.ZERO.toByteArray();
//		byte[] value			= Denomination.ETHER.getDenomination().toByteArray();
//		byte[] recieveAddress	= Hex.decode("8a40bfaa73256b60764c1bf40675a99083efb075");
//		byte[] gasPrice			= Denomination.SZABO.getDenomination().toByteArray();
//		byte[] gas				= new BigInteger("10000").toByteArray();
//		byte[] data				= new byte[0];
		

		byte[] nonce			= null;
		byte[] value			= null;
		byte[] recieveAddress	= Hex.decode("91a10664d0cd489085a7a018beb5245d4f2272f1");
		byte[] gasPrice			= Hex.decode("09184e72a000");
		byte[] gas				= Hex.decode("2710");
		byte[] data				= Hex.decode("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
		
    	Transaction tx = new Transaction(nonce, value, recieveAddress, gasPrice, gas, data);
    	byte[] encoded = tx.getEncoded();
    	String test = Hex.toHexString(encoded);
    	
    	System.out.println(RLP_ENCODED_TX2);
    	System.out.println(test);
    	
        assertEquals(RLP_ENCODED_TX2, test);
        assertEquals(HASH_RAW_TX, Hex.toHexString(tx.getHash()));
        tx.sign(privKeyBytes);
        assertEquals(HASH_RAW_TX, Hex.toHexString(tx.getHash()));
	}
}
