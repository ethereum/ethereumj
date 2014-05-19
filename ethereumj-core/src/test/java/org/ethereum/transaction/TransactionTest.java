package org.ethereum.transaction;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.ethereum.core.Address;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.junit.Test;
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

        // cat --> 79B08AD8787060333663D19704909EE7B1903E58
        // cow --> CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826

        BigInteger value = new BigInteger("1000000000000000000000000");

        byte[] privKey = HashUtil.sha3("cat".getBytes());
        Address receiveAddress = new Address(privKey);

        byte[] senderPrivKey = HashUtil.sha3("cow".getBytes());

        byte[] gasPrice=  Hex.decode("09184e72a000");
        byte[] gas =      Hex.decode("4255");

        Transaction tx = new Transaction(null, value.toByteArray(),
                receiveAddress.getPubKey(),  gasPrice, gas, null);

        tx.sign(senderPrivKey);

        System.out.println(tx.toString());

        ECKey key = ECKey.signatureToKey(HashUtil.sha3(tx.getEncoded(true)), tx.getSignature().toBase64());

        System.out.println("Signature public key\t: " + Hex.toHexString(key.getPubKey()));
        System.out.println("Sender is\t\t: " + Hex.toHexString(key.getAddress()));
    }


    @Test /* encode transaction */
    public void test3() throws Exception {

        BigInteger value = new BigInteger("1000000000000000000000000");

        byte[] privKey = HashUtil.sha3("cat".getBytes());
        Address receiveAddress = new Address(privKey);

        byte[] gasPrice=  Hex.decode("09184e72a000");
        byte[] gas =      Hex.decode("4255");

        Transaction tx = new Transaction(null, value.toByteArray(),
                receiveAddress.getPubKey(),  gasPrice, gas, null);

        tx.sign(privKey);
        byte[] payload = tx.getEncoded(true);

        System.out.println(Hex.toHexString(  payload ));
    }

}
