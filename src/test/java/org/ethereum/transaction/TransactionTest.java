package org.ethereum.transaction;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

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
    public void test2(){

        String rawTX      = "F86B04881BC16D674EC8000094CD2A3D9F938E13CD947EC05ABC7FE734DF8DD8268609184E72A00064801BA05E3868194605F1647593B842725818CCFA6A38651A728715133A8E97CDCFAC54A00FF91628D04B215EBCCFD5F4FC34CC1B45DF32F6B4609FBB0DE42E8522264467";
        byte[] rawTxBytes = Hex.decode(rawTX);

        String txHash = Hex.toHexString(HashUtil.sha3(rawTxBytes));
        System.out.println(txHash);
    }

}
