package samples;



import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECFieldF2m;
import java.security.spec.EllipticCurve;
import java.util.Arrays;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/04/14 09:37
 */
public class Main3 {

    static private byte[] shortMsg = Hex.decode("54686520717569636B2062726F776E20666F78206A756D7073206F76657220746865206C617A7920646F67");

    public static void main(String args[]) throws NoSuchProviderException, NoSuchAlgorithmException, IOException {

        Security.addProvider(new BouncyCastleProvider());

        MessageDigest digest = MessageDigest.getInstance("SHA3-256", "BC");
        byte[] result = digest.digest(shortMsg);

        byte[] expected = Hex.decode("4D741B6F1EB29CB2A9B9911C82F56FA8D73B04959D3D9D222895DF6C0B28AA15");

        if (Arrays.equals(expected, result)){

            System.out.println("equal !!!");
        } else {

            Hex.encode(result, System.out);
            System.out.flush();
        }


    }
}
