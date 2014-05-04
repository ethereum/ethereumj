package org.ethereum.util;

import java.math.BigInteger;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.spongycastle.util.encoders.Hex;

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

    public static ImageIcon getImageIcon(String resource){

        URL imageURL = ClassLoader.getSystemResource(resource);
        ImageIcon image = new ImageIcon(imageURL);
        return image;
    }
}
