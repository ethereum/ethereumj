/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm;

import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class DataWordTest {

    @Test
    public void dataWordImmutabilitySanityCheck() throws InvocationTargetException, IllegalAccessException {
        DataWord original = new DataWord(Hex.decode("fafafa"));
        DataWord copy = new DataWord(original.getData());
        DataWord another = new DataWord(Hex.decode("123456"));
        assertEquals(original, copy);

        for (Method method : original.getClass().getDeclaredMethods()) {
            switch (method.getName()) {
                case "addmod":
                case "mulmod":
                    method.invoke(original, another, another);
                    continue;
                case "withByte":
                    method.invoke(original, (byte)3, 4);
                    continue;
                default:
                    // Fallback to general
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (Modifier.isPrivate(method.getModifiers())) {
                continue;
            } else if (parameters.length == 1) {
                if (parameters[0].equals(original.getClass())) {
                    method.invoke(original, another);
                } else if (parameters[0].getSimpleName().equals("byte")) {
                    method.invoke(original, (byte)3);
                }
            } else if (parameters.length == 0) {
                method.invoke(original);
            } else {
                fail(method.getName() + " not tested for immutability");
            }
            assertEquals(original, copy);
        }
    }

    @Test
    public void testAdd3() {
        byte[] three = new byte[32];
        for (int i = 0; i < three.length; i++) {
            three[i] = (byte) 0xff;
        }

        DataWord x = new DataWord(three);
        DataWord result = x.add(new DataWord(three));
        assertEquals(32, result.getData().length);
        System.out.println(Hex.toHexString(result.getData()));

        // FAIL
//      DataWord y = new DataWord(three);
//      y.add2(new DataWord(three));
//      System.out.println(Hex.toHexString(y.getData()));
    }

    @Test
    public void testMod() {
        String expected = "000000000000000000000000000000000000000000000000000000000000001a";

        byte[] one = new byte[32];
        one[31] = 0x1e; // 0x000000000000000000000000000000000000000000000000000000000000001e

        byte[] two = new byte[32];
        for (int i = 0; i < two.length; i++) {
            two[i] = (byte) 0xff;
        }
        two[31] = 0x56; // 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff56

        DataWord x = new DataWord(one);// System.out.println(x.value());
        DataWord y = new DataWord(two);// System.out.println(y.value());
        DataWord result = y.mod(x);
        assertEquals(32, result.getData().length);
        assertEquals(expected, Hex.toHexString(result.getData()));
    }

    @Test
    public void testMul() {
        byte[] one = new byte[32];
        one[31] = 0x1; // 0x0000000000000000000000000000000000000000000000000000000000000001

        byte[] two = new byte[32];
        two[11] = 0x1; // 0x0000000000000000000000010000000000000000000000000000000000000000

        DataWord x = new DataWord(one);// System.out.println(x.value());
        DataWord y = new DataWord(two);// System.out.println(y.value());
        DataWord result = x.mul(y);
        assertEquals(32, result.getData().length);
        assertEquals("0000000000000000000000010000000000000000000000000000000000000000", Hex.toHexString(result.getData()));
    }

    // TODO: this asserted on the wrong variable and didn't pass the immutability refactor. Fix test
    @Ignore
    @Test
    public void testMulOverflow() {

        byte[] one = new byte[32];
        one[30] = 0x1; // 0x0000000000000000000000000000000000000000000000000000000000000100

        byte[] two = new byte[32];
        two[0] = 0x1; //  0x1000000000000000000000000000000000000000000000000000000000000000

        DataWord x = new DataWord(one); //System.out.println(x.value());
        DataWord y = new DataWord(two); //System.out.println(y.value());
        DataWord result = x.mul(y);
        assertEquals(32, y.getData().length);
        assertEquals("0100000000000000000000000000000000000000000000000000000000000000", Hex.toHexString(y.getData()));
    }

    @Test
    public void testDiv() {
        byte[] one = new byte[32];
        one[30] = 0x01;
        one[31] = 0x2c; // 0x000000000000000000000000000000000000000000000000000000000000012c

        byte[] two = new byte[32];
        two[31] = 0x0f; // 0x000000000000000000000000000000000000000000000000000000000000000f

        DataWord x = new DataWord(one);
        DataWord y = new DataWord(two);
        DataWord result = x.div(y);

        assertEquals(32, result.getData().length);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000014", Hex.toHexString(result.getData()));
    }

    @Test
    public void testDivZero() {
        byte[] one = new byte[32];
        one[30] = 0x05; // 0x0000000000000000000000000000000000000000000000000000000000000500

        byte[] two = new byte[32];

        DataWord x = new DataWord(one);
        DataWord y = new DataWord(two);
        DataWord result = x.div(y);

        assertEquals(32, result.getData().length);
        assertTrue(result.isZero());
    }

    @Test
    public void testSDivNegative() {

        // one is -300 as 256-bit signed integer:
        byte[] one = Hex.decode("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffed4");

        byte[] two = new byte[32];
        two[31] = 0x0f;

        DataWord x = new DataWord(one);
        DataWord y = new DataWord(two);
        DataWord result = x.sDiv(y);

        assertEquals(32, result.getData().length);
        assertEquals("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffec", result.toString());
    }

    @Test
    public void testPow() {

        BigInteger x = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger y = BigInteger.valueOf(1000);

        BigInteger result1 = x.modPow(x, y);
        BigInteger result2 = pow(x, y);
        System.out.println(result1);
        System.out.println(result2);
    }

    @Test
    public void testSignExtend1() {

        DataWord x = new DataWord(Hex.decode("f2"));
        byte k = 0;
        String expected = "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff2";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend2() {
        DataWord x = new DataWord(Hex.decode("f2"));
        byte k = 1;
        String expected = "00000000000000000000000000000000000000000000000000000000000000f2";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend3() {

        byte k = 1;
        DataWord x = new DataWord(Hex.decode("0f00ab"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000ab";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend4() {

        byte k = 1;
        DataWord x = new DataWord(Hex.decode("ffff"));
        String expected = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend5() {

        byte k = 3;
        DataWord x = new DataWord(Hex.decode("ffffffff"));
        String expected = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend6() {

        byte k = 3;
        DataWord x = new DataWord(Hex.decode("ab02345678"));
        String expected = "0000000000000000000000000000000000000000000000000000000002345678";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend7() {

        byte k = 3;
        DataWord x = new DataWord(Hex.decode("ab82345678"));
        String expected = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffff82345678";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test
    public void testSignExtend8() {

        byte k = 30;
        DataWord x = new DataWord(Hex.decode("ff34567882345678823456788234567882345678823456788234567882345678"));
        String expected = "0034567882345678823456788234567882345678823456788234567882345678";

        DataWord result = x.signExtend(k);
        System.out.println(result.toString());
        assertEquals(expected, result.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSignExtendException1() {

        byte k = -1;
        DataWord x = new DataWord();

        x.signExtend(k); // should throw an exception
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSignExtendException2() {

        byte k = 32;
        DataWord x = new DataWord();

        x.signExtend(k); // should throw an exception
    }

    @Test
    public void testAddModOverflow() {
        testAddMod("9999999999999999999999999999999999999999999999999999999999999999",
                "8888888888888888888888888888888888888888888888888888888888888888",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        testAddMod("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    }

    void testAddMod(String v1, String v2, String v3) {
        DataWord dv1 = new DataWord(Hex.decode(v1));
        DataWord dv2 = new DataWord(Hex.decode(v2));
        DataWord dv3 = new DataWord(Hex.decode(v3));
        BigInteger bv1 = new BigInteger(v1, 16);
        BigInteger bv2 = new BigInteger(v2, 16);
        BigInteger bv3 = new BigInteger(v3, 16);

        DataWord result = dv1.addmod(dv2, dv3);
        BigInteger br = bv1.add(bv2).mod(bv3);
        assertEquals(result.value(), br);
    }

    @Test
    public void testMulMod1() {
        DataWord wr = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = new DataWord(Hex.decode("01"));
        DataWord w2 = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999998"));

        DataWord result = wr.mulmod(w1, w2);

        assertEquals(32, result.getData().length);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(result.getData()));
    }

    @Test
    public void testMulMod2() {
        DataWord wr = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = new DataWord(Hex.decode("01"));
        DataWord w2 = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999999"));

        DataWord result = wr.mulmod(w1, w2);

        assertEquals(32, result.getData().length);
        assertTrue(result.isZero());
    }

    @Test
    public void testMulModZero() {
        DataWord wr = new DataWord(Hex.decode("00"));
        DataWord w1 = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w2 = new DataWord(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        DataWord result = wr.mulmod(w1, w2);

        assertEquals(32, result.getData().length);
        assertTrue(result.isZero());
    }

    @Test
    public void testMulModZeroWord1() {
        DataWord wr = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = new DataWord(Hex.decode("00"));
        DataWord w2 = new DataWord(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        DataWord result = wr.mulmod(w1, w2);

        assertEquals(32, result.getData().length);
        assertTrue(result.isZero());
    }

    @Test
    public void testMulModZeroWord2() {
        DataWord wr = new DataWord(Hex.decode("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = new DataWord(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        DataWord w2 = new DataWord(Hex.decode("00"));

        DataWord result = wr.mulmod(w1, w2);

        assertEquals(32, result.getData().length);
        assertTrue(result.isZero());
    }

    @Test
    public void testMulModOverflow() {
        DataWord wr = new DataWord(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        DataWord w1 = new DataWord(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        DataWord w2 = new DataWord(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        DataWord result = wr.mulmod(w1, w2);

        assertEquals(32, result.getData().length);
        assertTrue(result.isZero());
    }

    public static BigInteger pow(BigInteger x, BigInteger y) {
        if (y.compareTo(BigInteger.ZERO) < 0)
            throw new IllegalArgumentException();
        BigInteger z = x; // z will successively become x^2, x^4, x^8, x^16,
        // x^32...
        BigInteger result = BigInteger.ONE;
        byte[] bytes = y.toByteArray();
        for (int i = bytes.length - 1; i >= 0; i--) {
            byte bits = bytes[i];
            for (int j = 0; j < 8; j++) {
                if ((bits & 1) != 0)
                    result = result.multiply(z);
                // short cut out if there are no more bits to handle:
                if ((bits >>= 1) == 0 && i == 0)
                    return result;
                z = z.multiply(z);
            }
        }
        return result;
    }
}
