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
package org.ethereum.util;

import java.math.BigInteger;

public class RlpTestData {

    /***********************************
     * https://github.com/ethereum/tests/blob/master/rlptest.txt
     */
    public static int test01 = 0;
    public static String result01 = "80";

    public static String test02 = "";
    public static String result02 = "80";

    public static String test03 = "d";
    public static String result03 = "64";

    public static String test04 = "cat";
    public static String result04 = "83636174";

    public static String test05 = "dog";
    public static String result05 = "83646f67";

    public static String[] test06 = new String[]{"cat", "dog"};
    public static String result06 = "c88363617483646f67";

    public static String[] test07 = new String[]{"dog", "god", "cat"};
    public static String result07 = "cc83646f6783676f6483636174";

    public static int test08 = 1;
    public static String result08 = "01";

    public static int test09 = 10;
    public static String result09 = "0a";

    public static int test10 = 100;
    public static String result10 = "64";

    public static int test11 = 1000;
    public static String result11 = "8203e8";

    public static BigInteger test12 = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935");
    public static String result12 = "a0ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

    public static BigInteger test13 = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639936");
    public static String result13 = "a1010000000000000000000000000000000000000000000000000000000000000000";

    public static Object[] test14 = new Object[]{1, 2, new Object[]{}};
    public static String result14 = "c30102c0";
    public static Object[] expected14 = new Object[]{new byte[]{1}, new byte[]{2}, new Object[]{}};

    public static Object[] test15 = new Object[]{new Object[]{new Object[]{}, new Object[]{}}, new Object[]{}};
    public static String result15 = "c4c2c0c0c0";

    public static Object[] test16 = new Object[]{"zw", new Object[]{4}, "wz"};
    public static String result16 = "c8827a77c10482777a";
    public static Object[] expected16 = new Object[]{new byte[]{122, 119}, new Object[]{new byte[]{4}}, new byte[]{119, 122}};
}
