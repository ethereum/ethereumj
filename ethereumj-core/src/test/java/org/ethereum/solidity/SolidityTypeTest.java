package org.ethereum.solidity;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

public class SolidityTypeTest
{
    @Test
    public void ensureUnsignedInteger_isDecodedWithCorrectSignum()
    {
        byte[] bigNumberByteArray = {-13, -75, 19, 86, -119, 67, 112, -4, 118, -86, 98, -46, 103, -42, -126, 63, -60, -15, -87, 57, 43, 11, -17, -52, 0, 3, -65, 14, -67, -40, 65, 119};
        SolidityType testObject = new SolidityType.UnsignedIntType("uint256");
        Object decode = testObject.decode(bigNumberByteArray);
        assertEquals(decode.getClass(), BigInteger.class);
        BigInteger actualBigInteger = (BigInteger) decode;
        BigInteger expectedBigInteger = new BigInteger("f3b51356894370fc76aa62d267d6823fc4f1a9392b0befcc0003bf0ebdd84177", 16);
        assertEquals(expectedBigInteger, actualBigInteger);
    }
    @Test
    public void ensureSignedInteger_isDecoded()
    {
        byte[] bigNumberByteArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 127, -1, -1, -1, -1, -1, -1, -1}; 
        SolidityType testObject = new SolidityType.IntType("int256");
        Object decode = testObject.decode(bigNumberByteArray);
        assertEquals(decode.getClass(), BigInteger.class);
        BigInteger actualBigInteger = (BigInteger) decode;
        BigInteger expectedBigInteger = new BigInteger(bigNumberByteArray);
        assertEquals(expectedBigInteger, actualBigInteger);
    }
}
