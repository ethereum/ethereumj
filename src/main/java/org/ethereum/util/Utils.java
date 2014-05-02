package org.ethereum.util;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import javax.swing.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * www.ethereumj.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 13:13
 */
public class Utils {

    public static byte[]  hexStringToByteArr(String hexString){

        String hexSymbols = "0123456789ABCDEF";

        int arrSize = (int) (hexString.length() / 3);
        byte[] result = new byte[arrSize];

        for (int i = 0; i < arrSize; ++i){

            int digit1 = hexSymbols.indexOf( hexString.charAt(i * 3) );
            int digit2 = hexSymbols.indexOf( hexString.charAt(i * 3 + 1) );

            result[i] = (byte) (digit1 * 16 + digit2);
        }

        return result;
    }


    public static String toHexString(byte[] data){

        if (data == null) return "null";
        else return Hex.toHexString(data);
    }

    /**
     * @param hexNum should be in form '0x34fabd34....'
     * @return
     */
    public static String hexStringToDecimalString(String hexNum){

        boolean match = Pattern.matches("0[xX][0-9a-fA-F]+", hexNum);
        if (!match) throw new Error("The string doesn't conains hex num in form 0x.. : [" + hexNum + "]");

        byte[] numberBytes = Hex.decode(hexNum.substring(2));
        return (new BigInteger(1, numberBytes)).toString();
    }


    public static void printHexStringForByte(byte data){

        System.out.print("[");

        String hexNum = Integer.toHexString ((int) data & 0xFF);
        if (((int) data & 0xFF) < 16) {
            hexNum = "0" + hexNum;
        }

        System.out.print( hexNum );

        System.out.print("]");

        System.out.println();
    }



    public static void printHexStringForByteArray(byte[] data){

        System.out.print("[");
        for (int i = 0; i < data.length; ++i){

            String hexNum = Integer.toHexString ((int) data[i] & 0xFF);
            if (((int) data[i] & 0xFF) < 16) {
                hexNum = "0" + hexNum;
            }

            System.out.print( hexNum );
            System.out.print(" ");
        }
        System.out.print("]");

        System.out.println();
    }

    private  static MessageDigest sha3Digest = null;
    private  static MessageDigest ripemd160Digest = null;
    static {

        try{
            Security.addProvider(new BouncyCastleProvider());
            ripemd160Digest = MessageDigest.getInstance("RIPEMD160", "BC");
            sha3Digest      = MessageDigest.getInstance("SHA3-256", "BC");
        } catch (Throwable th){

            th.printStackTrace();
            exit(0);
        }
    }

    public static byte[] sha3(byte[] token){
       return sha3Digest.digest(token);
    }



    public static byte[] ripemd160(byte[] token){

        return ripemd160Digest.digest(token);
    }


    static X9ECParameters curvParams = SECNamedCurves.getByName("secp256k1");

    public static byte[] privToAddress(byte[] priv){

        /* address create howto

            token = "cow"
            step1 = sha3(token)             // generate 256 bit privkey
            step2 = privtopub(step1)[1:]    // generate 512 bit pubkey  with secp256k1
            step3 = sha3(step2)[12:]
        */

        // TODO: validity checks
       BigInteger privKey = new BigInteger(1, priv);


       ECPoint Q = curvParams.getG().multiply(privKey);
       byte[] pubKey = Q.getEncoded();

       // TODO: find a performance improvement here -  how to omit creation of new byte[]
       byte[] _pubKey = Arrays.copyOfRange(pubKey, 1, pubKey.length);

       byte[] _addr = Utils.sha3(_pubKey);

        // TODO: find a performance improvement here -  how to omit creation of new byte[]
       byte[] addr = Arrays.copyOfRange(_addr, 12, _addr.length);

       return addr;
    }


    public static ImageIcon getImageIcon(String resource){

        URL imageURL = ClassLoader.getSystemResource(resource);
        ImageIcon image = new ImageIcon(imageURL);

        return image;
    }


}
