package samples;


import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/04/14 09:37
 */
public class Main4 {

    static private byte[] shortMsg = Hex.decode("54686520717569636B2062726F776E20666F78206A756D7073206F76657220746865206C617A7920646F67");

    public static void main(String args[]) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        Security.addProvider(new BouncyCastleProvider());


        EllipticCurve curve = new EllipticCurve(
                new ECFieldF2m(239, // m
                        new int[] { 36 }), // k
                new BigInteger("32010857077C5431123A46B808906756F543423E8D27877578125778AC76", 16), // a
                new BigInteger("790408F2EEDAF392B012EDEFB3392F30F4327C0CA3F31FC383C422AA8C16", 16)); // b

        ECParameterSpec params = new ECParameterSpec(
                curve,
                ECPointUtil.decodePoint(curve,
                Hex.decode("0457927098FA932E7C0A96D3FD5B706EF7E5F5C156E16B7E7C86038552E91D61D8EE5077C33FECF6F1A16B268DE469C3C7744EA9A971649FC7A9616305")), // G
                new BigInteger("220855883097298041197912187592864814557886993776713230936715041207411783"), // n
                4); // h

        ECPrivateKeySpec priKeySpec = new ECPrivateKeySpec(
                new BigInteger("145642755521911534651321230007534120304391871461646461466464667494947990"), // d
                params);

        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(
                ECPointUtil.decodePoint(curve, Hex.decode("045894609CCECF9A92533F630DE713A958E96C97CCB8F5ABB5A688A238DEED6DC2D9D0C94EBFB7D526BA6A61764175B99CB6011E2047F9F067293F57F5")), // Q
                params);

        Signature sgr = Signature.getInstance("ECDSA", "BC");
        KeyFactory  f = KeyFactory.getInstance("ECDSA", "BC");
        PrivateKey  sKey = f.generatePrivate(priKeySpec);
        PublicKey   vKey = f.generatePublic(pubKeySpec);

//        System.out.println(vKey);



          System.out.println(Hex.toHexString(Utils.sha3("coinbase".getBytes())));

//        toAddress(sha3("coinbase"));
//        toAddress(76ec948a9207fdea26dcba91086bcdd181920ff52a539b0d1eb28e73b4cd92af);

    }
}
