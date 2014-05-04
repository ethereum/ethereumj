package org.ethereum.transaction;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.util.Utils;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 20/04/14 14:21
 */
public class TransactionTest {



    @Test /* sign transaction  https://tools.ietf.org/html/rfc6979 */
    public void test1() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, IOException {

        //python taken exact data
        String txRLPRawData = "a9e880872386f26fc1000085e8d4a510008203e89413978aee95f38490e9769c39b2773ed763d9cd5f80";

//        String txRLPRawData = "f82804881bc16d674ec8000094cd2a3d9f938e13cd947ec05abc7fe734df8dd8268609184e72a0006480";
        String cowPrivKey   = "c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4";

        byte[] data    = Hex.decode(txRLPRawData);
        byte[] privKey = Hex.decode(cowPrivKey);

        // step 1: serialize + RLP encode
        // step 2: hash = sha3(step1)
        byte[] txHash = Utils.sha3(data);

        X9ECParameters curvParams = SECNamedCurves.getByName("secp256k1");

//        z = hash_to_int(msghash)
//        k = deterministic_generate_k(msghash,priv)
        BigInteger txHashInt = new BigInteger(1, txHash );

/*
        v = '\x01' * 32
        k = '\x00' * 32
        priv = encode_privkey(priv,'bin')
        msghash = encode(hash_to_int(msghash),256,32)
        k = hmac.new(k, v+'\x00'+priv+msghash, hashlib.sha256).digest()
        v = hmac.new(k, v, hashlib.sha256).digest()
        k = hmac.new(k, v+'\x01'+priv+msghash, hashlib.sha256).digest()
        v = hmac.new(k, v, hashlib.sha256).digest()
        return decode(hmac.new(k, v, hashlib.sha256).digest(),256)
*/
        byte[] v = {
                0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,
                0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,
                0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,
                0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01
            };

        byte[] k = {
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
        };

        Mac hmac = Mac.getInstance("HMac-SHA256", "BC");

        SecretKey secretKey = new SecretKeySpec(k, "HMac-SHA256");

        hmac.init(secretKey);
        hmac.reset();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(v.length + 1 + privKey.length + txHash.length);
        baos.write(v);
        baos.write(new byte[]{00});
        baos.write(privKey);
        baos.write(txHash);

        hmac.update(baos.toByteArray());
        byte[] k_ = hmac.doFinal(secretKey.getEncoded());


        System.out.println(Hex.toHexString(k_));



    }

    @Test  /* achieve public key of the sender */
    public void test2(){

//        http://etherchain.org/#/tx/558a3797e0dd3fbfaf761f1add6749c7d5db313fdac5cba59f40e28af7bbacd1
//        f86b04881bc16d674ec8000094cd2a3d9f938e13cd947ec05abc7fe734df8dd8268609184e72a00064801ba05e3868194605f1647593b842725818ccfa6a38651a728715133a8e97cdcfac54a00ff91628d04b215ebccfd5f4fc34cc1b45df32f6b4609fbb0de42e8522264467

//        [ 0x12, [ 0x4, 0x1BC16D674EC80000, 0xCD2A3D9F938E13CD947EC05ABC7FE734DF8DD826, 0x9184E72A000, 0x64, 0x0,
//                0x1B, 0x5E3868194605F1647593B842725818CCFA6A38651A728715133A8E97CDCFAC54, 0xFF91628D04B215EBCCFD5F4FC34CC1B45DF32F6B4609FBB0DE42E8522264467 ] ]
// sender: d4bfbf8d0f435c2ee2b4e3680018f1892fc1fba6

        String rawTX      = "F86B04881BC16D674EC8000094CD2A3D9F938E13CD947EC05ABC7FE734DF8DD8268609184E72A00064801BA05E3868194605F1647593B842725818CCFA6A38651A728715133A8E97CDCFAC54A00FF91628D04B215EBCCFD5F4FC34CC1B45DF32F6B4609FBB0DE42E8522264467";
        byte[] rawTxBytes = Hex.decode(rawTX);

        String txHash = Hex.toHexString(Utils.sha3(rawTxBytes));
        System.out.println(txHash);
    }

}
