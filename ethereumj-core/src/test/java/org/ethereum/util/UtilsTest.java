package org.ethereum.util;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/05/14 15:38
 */
public class UtilsTest {

    @Test
    public void getValueShortString1(){

        String expected = "123 (10^24)";
        String result = Utils.getValueShortString(new BigInteger("123456789123445654363653463"));

        assertEquals(expected, result);
    }

    @Test
    public void getValueShortString2(){

        String expected = "123 (10^3)";
        String result = Utils.getValueShortString(new BigInteger("123456"));

        assertEquals(expected, result);
    }

    @Test
    public void getValueShortString3(){

        String expected = "1 (10^3)";
        String result = Utils.getValueShortString(new BigInteger("1234"));

        assertEquals(expected, result);
    }

    @Test
    public void getValueShortString4(){

        String expected = "123 (10^0)";
        String result = Utils.getValueShortString(new BigInteger("123"));

        assertEquals(expected, result);
    }

    @Test
    public void getValueShortString5(){

        byte[] decimal = Hex.decode("3913517ebd3c0c65000000");
        String expected = "69 (10^24)";
        String result = Utils.getValueShortString(new BigInteger(decimal));

        assertEquals(expected, result);
    }


}
