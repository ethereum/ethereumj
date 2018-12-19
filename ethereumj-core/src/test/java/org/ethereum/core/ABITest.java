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
package org.ethereum.core;

import static org.ethereum.crypto.HashUtil.sha3;

import org.ethereum.solidity.SolidityType;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @author Anton Nashatyrev
 */
public class ABITest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    @Test
    public void testTransactionCreate() {
        // demo only
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson1);
        Transaction ctx = CallTransaction.createCallTransaction(1, 1_000_000_000, 1_000_000_000,
                "86e0497e32a8e1d79fe38ab87dc80140df5470d9", 0, function, "1234567890abcdef1234567890abcdef12345678");
        ctx.sign(sha3("974f963ee4571e86e5f9bc3b493e453db9c15e5bd19829a4ef9a790de0da0015".getBytes()));
    }

    static String funcJson1 = "{ \n" +
                            "  'constant': false, \n" +
                            "  'inputs': [{'name':'to', 'type':'address'}], \n" +
                            "  'name': 'delegate', \n" +
                            "  'outputs': [], \n" +
                            "  'type': 'function' \n" +
                            "} \n";
    static {funcJson1 = funcJson1.replaceAll("'", "\"");}

    @Test
    public void testSimple1() {

        logger.info("\n{}", funcJson1);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson1);

        Assert.assertEquals("5c19a95c0000000000000000000000001234567890abcdef1234567890abcdef12345678",
                Hex.toHexString(function.encode("1234567890abcdef1234567890abcdef12345678")));
        Assert.assertEquals("5c19a95c0000000000000000000000001234567890abcdef1234567890abcdef12345678",
                Hex.toHexString(function.encode("0x1234567890abcdef1234567890abcdef12345678")));
        try {
            Hex.toHexString(function.encode("0xa1234567890abcdef1234567890abcdef12345678"));
            Assert.assertTrue(false);
        } catch (Exception e) {}

        try {
            Hex.toHexString(function.encode("blabla"));
            Assert.assertTrue(false);
        } catch (Exception e) {}
    }

    static String funcJson2 = "{\n" +
            " 'constant':false, \n" +
            " 'inputs':[], \n" +
            " 'name':'tst', \n" +
            " 'outputs':[], \n" +
            " 'type':'function' \n" +
            "}";
    static {funcJson2 = funcJson2.replaceAll("'", "\"");}

    @Test
    public void testSimple2() {

        logger.info("\n{}", funcJson2);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson2);
        Transaction ctx = CallTransaction.createCallTransaction(1, 1_000_000_000, 1_000_000_000,
                "86e0497e32a8e1d79fe38ab87dc80140df5470d9", 0, function);
        ctx.sign(sha3("974f963ee4571e86e5f9bc3b493e453db9c15e5bd19829a4ef9a790de0da0015".getBytes()));

        Assert.assertEquals("91888f2e", Hex.toHexString(ctx.getData()));
    }

    static String funcJson3 = "{\n" +
            " 'constant':false, \n" +
            " 'inputs':[ \n" +
            "   {'name':'i','type':'int'}, \n" +
            "   {'name':'u','type':'uint'}, \n" +
            "   {'name':'i8','type':'int8'}, \n" +
            "   {'name':'b2','type':'bytes2'}, \n" +
            "   {'name':'b32','type':'bytes32'} \n" +
            "  ], \n" +
            "  'name':'f1', \n" +
            "  'outputs':[], \n" +
            "  'type':'function' \n" +
            "}\n";
    static {funcJson3 = funcJson3.replaceAll("'", "\"");}

    @Test
    public void test3() {

        logger.info("\n{}", funcJson3);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson3);

        Assert.assertEquals("a4f72f5a" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffb2e" +
                "00000000000000000000000000000000000000000000000000000000000004d2" +
                "000000000000000000000000000000000000000000000000000000000000007b61" +
                "000000000000000000000000000000000000000000000000000000000000007468" +
                "6520737472696e6700000000000000000000000000000000000000000000",
                Hex.toHexString(function.encode(-1234, 1234, 123, "a", "the string")));
    }

    static String funcJson4 = "{\n" +
            " 'constant':false, \n" +
            " 'inputs':[{'name':'i','type':'int[3]'}, {'name':'j','type':'int[]'}], \n" +
            " 'name':'f2', \n" +
            " 'outputs':[], \n" +
            " 'type':'function' \n" +
            "}\n";
    static {funcJson4 = funcJson4.replaceAll("'", "\"");};


    @Test
    public void test4() {

        logger.info("\n{}", funcJson4);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson4);
        Assert.assertEquals("d383b9f6" +
                        "0000000000000000000000000000000000000000000000000000000000000001" +
                        "0000000000000000000000000000000000000000000000000000000000000002" +
                        "0000000000000000000000000000000000000000000000000000000000000003",
                Hex.toHexString(function.encode(new int[] {1,2,3})));

        Assert.assertEquals(
                "d383b9f60000000000000000000000000000000000000000000000000000000000000001" +
                        "0000000000000000000000000000000000000000000000000000000000000002" +
                        "0000000000000000000000000000000000000000000000000000000000000003" +
                        "0000000000000000000000000000000000000000000000000000000000000080" +
                        "0000000000000000000000000000000000000000000000000000000000000002" +
                        "0000000000000000000000000000000000000000000000000000000000000004" +
                        "0000000000000000000000000000000000000000000000000000000000000005",
                Hex.toHexString(function.encode(new int[]{1, 2, 3}, new int[]{4, 5})));

    }

    static String funcJson5 = "{\n" +
            "   'constant':false, \n" +
            "   'inputs':[{'name':'i','type':'int'}, \n" +
            "               {'name':'s','type':'bytes'}, \n" +
            "               {'name':'j','type':'int'}], \n" +
            "    'name':'f4', \n" +
            "    'outputs':[], \n" +
            "    'type':'function' \n" +
            "}\n";
    static {funcJson5 = funcJson5.replaceAll("'", "\"");};

    @Test
    public void test5() {

        logger.info("\n{}", funcJson5);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson5);

        Assert.assertEquals(
                "3ed2792b000000000000000000000000000000000000000000000000000000000000006f" +
                        "0000000000000000000000000000000000000000000000000000000000000060" +
                        "00000000000000000000000000000000000000000000000000000000000000de" +
                        "0000000000000000000000000000000000000000000000000000000000000003" +
                        "abcdef0000000000000000000000000000000000000000000000000000000000",
            Hex.toHexString(function.encode(111, new byte[] {(byte) 0xab, (byte) 0xcd, (byte) 0xef}, 222)));

    }

    @Test
    public void decodeDynamicTest1() {
        String funcJson = "{\n" +
                "   'constant':false, \n" +
                "   'inputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'bytes'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'name':'f4', \n" +
                "   'outputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'bytes'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'type':'function' \n" +
                "}\n";
        funcJson = funcJson.replaceAll("'", "\"");

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        byte[] bytes = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};
        byte[] encoded = function.encodeArguments(111, bytes, 222);
        Object[] objects = function.decodeResult(encoded);
//        System.out.println(Arrays.toString(objects));
        Assert.assertEquals(((Number) objects[0]).intValue(), 111);
        Assert.assertArrayEquals((byte[]) objects[1], bytes);
        Assert.assertEquals(((Number) objects[2]).intValue(), 222);
    }
    @Test
    public void decodeDynamicTest2() {
        String funcJson = "{\n" +
                "   'constant':false, \n" +
                "   'inputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'name':'f4', \n" +
                "   'outputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'type':'function' \n" +
                "}\n";
        funcJson = funcJson.replaceAll("'", "\"");

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        String[] strings = new String[] {"aaa", "long string: 123456789012345678901234567890", "ccc"};
        byte[] encoded = function.encodeArguments(111, strings, 222);
        Object[] objects = function.decodeResult(encoded);
//        System.out.println(Arrays.toString(objects));
        Assert.assertEquals(((Number) objects[0]).intValue(), 111);
        Assert.assertArrayEquals((Object[]) objects[1], strings);
        Assert.assertEquals(((Number) objects[2]).intValue(), 222);
    }

    @Test
    public void decodeWithUnknownPropertiesTest() {
        String funcJson = "{\n" +
                "   'constant':false, \n" +
                "   'inputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'name':'f4', \n" +
                "   'outputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'type':'function', \n" +
                "    'test':'test' \n" +
                "}\n";
        funcJson = funcJson.replaceAll("'", "\"");

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        String[] strings = new String[] {"aaa", "long string: 123456789012345678901234567890", "ccc"};
        byte[] encoded = function.encodeArguments(111, strings, 222);
        Object[] objects = function.decodeResult(encoded);
        Assert.assertEquals(((Number) objects[0]).intValue(), 111);
        Assert.assertArrayEquals((Object[]) objects[1], strings);
        Assert.assertEquals(((Number) objects[2]).intValue(), 222);
    }

    @Test
    public void decodeWithPayablePropertyTest() {
        String funcJson = "{\n" +
                "   'constant':false, \n" +
                "   'inputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'name':'f4', \n" +
                "   'outputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'type':'function', \n" +
                "    'payable':true \n" +
                "}\n";
        funcJson = funcJson.replaceAll("'", "\"");

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        Assert.assertTrue(function.payable);
        String[] strings = new String[] {"aaa", "long string: 123456789012345678901234567890", "ccc"};
        byte[] encoded = function.encodeArguments(111, strings, 222);
        Object[] objects = function.decodeResult(encoded);
        Assert.assertEquals(((Number) objects[0]).intValue(), 111);
        Assert.assertArrayEquals((Object[]) objects[1], strings);
        Assert.assertEquals(((Number) objects[2]).intValue(), 222);
    }

    @Test
    public void decodeWithFunctionTypeFallbackTest() {
        String funcJson = "{\n" +
                "   'constant':false, \n" +
                "   'inputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'name':'f4', \n" +
                "   'outputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'type':'fallback' \n" +
                "}\n";
        funcJson = funcJson.replaceAll("'", "\"");

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        Assert.assertEquals(CallTransaction.FunctionType.fallback, function.type);
        String[] strings = new String[] {"aaa", "long string: 123456789012345678901234567890", "ccc"};
        byte[] encoded = function.encodeArguments(111, strings, 222);
        Object[] objects = function.decodeResult(encoded);
        Assert.assertEquals(((Number) objects[0]).intValue(), 111);
        Assert.assertArrayEquals((Object[]) objects[1], strings);
        Assert.assertEquals(((Number) objects[2]).intValue(), 222);
    }

    @Test
    public void decodeWithUnknownFunctionTypeTest() {
        String funcJson = "{\n" +
                "   'constant':false, \n" +
                "   'inputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'name':'f4', \n" +
                "   'outputs':[{'name':'i','type':'int'}, \n" +
                "               {'name':'s','type':'string[]'}, \n" +
                "               {'name':'j','type':'int'}], \n" +
                "    'type':'test' \n" +
                "}\n";
        funcJson = funcJson.replaceAll("'", "\"");
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        Assert.assertEquals(null, function.type);
        String[] strings = new String[] {"aaa", "long string: 123456789012345678901234567890", "ccc"};
        byte[] encoded = function.encodeArguments(111, strings, 222);
        Object[] objects = function.decodeResult(encoded);
        Assert.assertEquals(((Number) objects[0]).intValue(), 111);
        Assert.assertArrayEquals((Object[]) objects[1], strings);
        Assert.assertEquals(((Number) objects[2]).intValue(), 222);
    }

    @Test
    public void twoDimensionalArrayType_hasDimensionDefinitionInCorrectOrder() {
        String funcJson = "{  \n" +
                "      'constant':false,\n" +
                "      'inputs':[  \n" +
                "         {  \n" +
                "            'name':'param1',\n" +
                "            'type':'address[5][]'\n" +
                "         },\n" +
                "         {  \n" +
                "            'name':'param2',\n" +
                "            'type':'uint256[6][2]'\n" +
                "         },\n" +
                "         {  \n" +
                "            'name':'param2',\n" +
                "            'type':'uint256[][]'\n" +
                "         },\n" +
                "         {  \n" +
                "            'name':'param3',\n" +
                "            'type':'uint256[][2]'\n" +
                "         }\n" +
                "      ],\n" +
                "      'name':'testTwoDimArray',\n" +
                "      'outputs':[],\n" +
                "      'payable':false,\n" +
                "      'type':'function'\n" +
                "}";
        funcJson = funcJson.replaceAll("'", "\"");
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        String expected = "testTwoDimArray(address[5][],uint256[6][2],uint256[][],uint256[][2])";
        String actual = function.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void twoDimensionalArrayTypeAsParameter_isDecoded() {
        String funcJson = "{   " +
                "      'constant':false, " +
                "      'inputs':[   " +
                "         {   " +
                "            'name':'orderAddresses', " +
                "            'type':'address[5][]' " +
                "         }, " +
                "         {   " +
                "            'name':'orderValues', " +
                "            'type':'uint256[6][]' " +
                "         }, " +
                "         {   " +
                "            'name':'fillTakerTokenAmounts', " +
                "            'type':'uint256[]' " +
                "         }, " +
                "         {   " +
                "            'name':'v', " +
                "            'type':'uint8[]' " +
                "         }, " +
                "         {   " +
                "            'name':'r', " +
                "            'type':'bytes32[]' " +
                "         }, " +
                "         {   " +
                "            'name':'s', " +
                "            'type':'bytes32[]' " +
                "         } " +
                "      ], " +
                "      'name':'batchFillOrKillOrders', " +
                "      'outputs':[], " +
                "      'payable':false, " +
                "      'type':'function' " +
                "   }";
        funcJson = funcJson.replaceAll("'", "\"");
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);

        Object[] args = new Object[]{
                new byte[][][]{
                        new byte[][]{
                                Hex.decode("1b2a9cc5ea11c11b70908d75207b5b1f0ac4a839"),
                                Hex.decode("e697a9f14f182c5291287dbeb47d41773091f035"),
                                Hex.decode("c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"),
                                Hex.decode("2d0ea9f9591205a642eb01826ba4fa019eb0efc6"),
                                Hex.decode("8124071f810d533ff63de61d0c98db99eeb99d64")
                        },
                        new byte[][]{
                                Hex.decode("1b2a9cc5ea11c11b70908d75207b5b1f0ac4a839"),
                                Hex.decode("e697a9f14f182c5291287dbeb47d41773091f035"),
                                Hex.decode("2d0ea9f9591205a642eb01826ba4fa019eb0efc6"),
                                Hex.decode("c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"),
                                Hex.decode("8124071f810d533ff63de61d0c98db99eeb99d64")
                        }
                }, new BigInteger[][]{
                new BigInteger[]{
                            new BigInteger("15920000000000000000"),
                            new BigInteger("1592000000000000000000"),
                            BigInteger.valueOf(0),
                            BigInteger.valueOf(0),
                            BigInteger.valueOf(1537516391517L),
                            new BigInteger("88416929899962839058958574884878701761157019606353286750292520499350182621314")
                },
                new BigInteger[]{
                            new BigInteger("1642000000000000000000"),
                            new BigInteger("16420000000000002000"),
                            BigInteger.valueOf(0),
                            BigInteger.valueOf(0),
                            BigInteger.valueOf(1537517358153L),
                            new BigInteger("93513067008724755490443777049125356883124657581213787456489051336421643029820")
                        }
                },
                new BigInteger[]{
                        new BigInteger("14000000000000000000"),
                        new BigInteger("140000000000000017")
                },
                new BigInteger[]{
                        BigInteger.valueOf(27),
                        BigInteger.valueOf(28)
                },
                new byte[][]{
                        Hex.decode("9202d3602753ffdb469e9dbae74cbe7528c648f708334f7791acc6fe0ce8182b"),
                        Hex.decode("ef362daf1bc2c805797761ae93a6c46ed53d73483a2bcc5b499ab65a8ba7f16c")
                },
                new byte[][]{
                        Hex.decode("0b43ad3ff547ebf5089802a74e764692bdc092190438b31be34d1d79406a75ba"),
                        Hex.decode("4e87fcd4ead36423d5bbcc7f1b41616235a17ec1f053a66827281bab104b718b")
                }
        };
        byte[] bytes = function.encode(args);

        String input = "4f15078700000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000000000000000000000000000000000000000022000000000000000000000000000000000000000000000000000000000000003c00000000000000000000000000000000000000000000000000000000000000420000000000000000000000000000000000000000000000000000000000000048000000000000000000000000000000000000000000000000000000000000004e000000000000000000000000000000000000000000000000000000000000000020000000000000000000000001b2a9cc5ea11c11b70908d75207b5b1f0ac4a839000000000000000000000000e697a9f14f182c5291287dbeb47d41773091f035000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc20000000000000000000000002d0ea9f9591205a642eb01826ba4fa019eb0efc60000000000000000000000008124071f810d533ff63de61d0c98db99eeb99d640000000000000000000000001b2a9cc5ea11c11b70908d75207b5b1f0ac4a839000000000000000000000000e697a9f14f182c5291287dbeb47d41773091f0350000000000000000000000002d0ea9f9591205a642eb01826ba4fa019eb0efc6000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc20000000000000000000000008124071f810d533ff63de61d0c98db99eeb99d640000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000dcef33a6f83800000000000000000000000000000000000000000000000000564d702d38f5e000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000165fb1e4c5dc37a357a19313983db8360977d0c3cfe274a9deb63fc1a994f3c290b2644f0820000000000000000000000000000000000000000000000590353dc4fa7680000000000000000000000000000000000000000000000000000e3df8f00cbea07d00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000165fb2d0c49cebe85312f1f1cc97430bc1315aff0fbb0d7f1219c8759774672c9939e168d3c0000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000c249fdd32778000000000000000000000000000000000000000000000000000001f161421c8e00110000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000001b000000000000000000000000000000000000000000000000000000000000001c00000000000000000000000000000000000000000000000000000000000000029202d3602753ffdb469e9dbae74cbe7528c648f708334f7791acc6fe0ce8182bef362daf1bc2c805797761ae93a6c46ed53d73483a2bcc5b499ab65a8ba7f16c00000000000000000000000000000000000000000000000000000000000000020b43ad3ff547ebf5089802a74e764692bdc092190438b31be34d1d79406a75ba4e87fcd4ead36423d5bbcc7f1b41616235a17ec1f053a66827281bab104b718b";
        Assert.assertEquals(input, Hex.toHexString(bytes));

        Object[] decode = function.decode(Hex.decode(input));
        Assert.assertArrayEquals(args, decode);
    }

    @Test
    public void staticArrayWithDynamicElements() {
        // static array with dynamic elements is itself dynamic type
        String funcJson = "{   " +
                "      'constant':false, " +
                "      'inputs':[{   " +
                "            'name':'p1', " +
                "            'type':'address[][2]' " +
                "          }]," +
                "      'name':'f1', " +
                "      'outputs':[], " +
                "      'payable':false, " +
                "      'type':'function' " +
                "}";
        funcJson = funcJson.replaceAll("'", "\"");

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
        Assert.assertTrue(function.inputs[0].type instanceof SolidityType.StaticArrayType);
        Assert.assertTrue(function.inputs[0].type.isDynamicType());

        try {
            function.encode((Object) new byte[][][]{
                    new byte[][]{
                            Hex.decode("1111111111111111111111111111111111111111"),
                    }}
            );
            throw new RuntimeException("Exception should be thrown");
        } catch (Exception e) {
            System.out.println("Expected exception: " + e);
        }

        Object[] args = new Object[]{
                new byte[][][]{
                        new byte[][]{
                                Hex.decode("1111111111111111111111111111111111111111"),
                                Hex.decode("2222222222222222222222222222222222222222"),
                                Hex.decode("3333333333333333333333333333333333333333"),
                        },
                        new byte[][]{
                                Hex.decode("4444444444444444444444444444444444444444"),
                        }
                }
        };

        byte[] bytes = function.encode(args);
        String input = "7e5f5dc50000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000000000000000000000000000000000000000000300000000000000000000000011111111111111111111111111111111111111110000000000000000000000002222222222222222222222222222222222222222000000000000000000000000333333333333333333333333333333333333333300000000000000000000000000000000000000000000000000000000000000010000000000000000000000004444444444444444444444444444444444444444";
        System.out.println(Hex.toHexString(bytes));

        Assert.assertEquals(input, Hex.toHexString(bytes));
        Object[] decode = function.decode(bytes);
        Assert.assertArrayEquals(args, decode);
    }

    @Test
    public void staticArrayWithDynamicElementsSolidity() {
        String contract =
                "pragma solidity ^0.4.3;\n" +
                "pragma experimental ABIEncoderV2;\n"+
                "contract A {" +
                        "  function call(uint[][2] arr) public returns (uint) {" +
                        "    if (arr.length != 2) return 2;" +
                        "    if (arr[0].length != 3) return 3;" +
                        "    if (arr[1].length != 2) return 4;" +
                        "    if (arr[0][0] != 10) return 5;" +
                        "    if (arr[0][1] != 11) return 6;" +
                        "    if (arr[0][2] != 12) return 7;" +
                        "    if (arr[1][0] != 13) return 8;" +
                        "    if (arr[1][1] != 14) return 9;" +
                        "    return 1;" +
                        "  }" +
                        "  function ret() public returns (uint[][2]) {" +
                        "    uint[][2] a1;" +
                        "    a1[0] = [uint(3),uint(4),uint(5)];" +
                        "    a1[1] = [uint(6),uint(7)];" +
                        "    return a1;" +
                        "  }" +
                        "}";

        StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = bc.submitNewContract(contract);
        SolidityCallResult res = a.callFunction("call",
                (Object) new BigInteger[][]{
                        new BigInteger[]{
                                BigInteger.valueOf(10),
                                BigInteger.valueOf(11),
                                BigInteger.valueOf(12),
                        },
                        new BigInteger[]{
                                BigInteger.valueOf(13),
                                BigInteger.valueOf(14),
                        },
                }
        );
        Assert.assertTrue(res.isSuccessful());
        Assert.assertEquals(BigInteger.valueOf(1), res.getReturnValue());

        Object[] ret = a.callConstFunction("ret");
        Assert.assertArrayEquals(
                new Object[] {
            new BigInteger[][]{
                new BigInteger[]{
                        BigInteger.valueOf(3),
                        BigInteger.valueOf(4),
                        BigInteger.valueOf(5),
                },
                new BigInteger[]{
                        BigInteger.valueOf(6),
                        BigInteger.valueOf(7),
                },
        }}, ret);
    }

}
