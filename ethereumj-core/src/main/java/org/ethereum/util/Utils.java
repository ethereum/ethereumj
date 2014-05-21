package org.ethereum.util;

import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.spongycastle.util.encoders.Hex;

public class Utils {

    private static SecureRandom random = new SecureRandom();

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
    
    public static ImageIcon getImageIcon(String resource){
        URL imageURL = ClassLoader.getSystemResource(resource);
        ImageIcon image = new ImageIcon(imageURL);
        return image;
    }

    static BigInteger thousand = new BigInteger("1000");
    public static String getValueShortString(BigInteger number){

        BigInteger result = number;
        int pow = 0;
        while (result.compareTo(thousand) == 1){
            result = result.divide(thousand);
            pow += 3;
        }
        return result.toString() + " (" + "10^" + pow + ")";
    }

    public static SecureRandom getRandom(){
        return random;
    }
}
