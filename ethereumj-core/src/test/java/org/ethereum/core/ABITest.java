package org.ethereum.core;

import org.ethereum.crypto.SHA3Helper;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Anton Nashatyrev on 27.08.2015.
 */
public class ABITest {

    @Test
    public void testTransactionCreate() {
        // demo only
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson1);
        Transaction ctx = CallTransaction.createCallTransaction(1, 1_000_000_000, 1_000_000_000,
                "86e0497e32a8e1d79fe38ab87dc80140df5470d9", 0, function, "1234567890abcdef1234567890abcdef12345678");
        ctx.sign(SHA3Helper.sha3("974f963ee4571e86e5f9bc3b493e453db9c15e5bd19829a4ef9a790de0da0015".getBytes()));
    }

    static String funcJson1 = "{\"constant\":false,\n" +
            "   \"inputs\":[{\"name\":\"to\",\"type\":\"address\"}],\n" +
            "    \"name\":\"delegate\",\n" +
            "    \"outputs\":[],\n" +
            "    \"type\":\"function\"\n" +
            " }";
    @Test
    public void testSimple1() {
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

    static String funcJson2 = "{\"constant\":false,\n" +
            "   \"inputs\":[],\n" +
            "    \"name\":\"tst\",\n" +
            "    \"outputs\":[],\n" +
            "    \"type\":\"function\"\n" +
            " }";

    @Test
    public void testSimple2() {
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson2);
        Transaction ctx = CallTransaction.createCallTransaction(1, 1_000_000_000, 1_000_000_000,
                "86e0497e32a8e1d79fe38ab87dc80140df5470d9", 0, function);
        ctx.sign(SHA3Helper.sha3("974f963ee4571e86e5f9bc3b493e453db9c15e5bd19829a4ef9a790de0da0015".getBytes()));

        Assert.assertEquals("91888f2e", Hex.toHexString(ctx.getData()));
    }

    static String funcJson3 = "{\"constant\":false,\n" +
            "  \"inputs\":[\n" +
            "    {\"name\":\"i\",\"type\":\"int\"},\n" +
            "    {\"name\":\"u\",\"type\":\"uint\"},\n" +
            "    {\"name\":\"i8\",\"type\":\"int8\"},\n" +
            "    {\"name\":\"b2\",\"type\":\"bytes2\"},\n" +
            "    {\"name\":\"b32\",\"type\":\"bytes32\"}\n" +
            "  ],\n" +
            "  \"name\":\"f1\",\n" +
            "  \"outputs\":[],\n" +
            "  \"type\":\"function\"\n" +
            "}";

    @Test
    public void test3() {
        System.out.println(funcJson3);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson3);

        Assert.assertEquals("a4f72f5a" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffb2e" +
                "00000000000000000000000000000000000000000000000000000000000004d2" +
                "000000000000000000000000000000000000000000000000000000000000007b61" +
                "000000000000000000000000000000000000000000000000000000000000007468" +
                "6520737472696e6700000000000000000000000000000000000000000000",
                Hex.toHexString(function.encode(-1234, 1234, 123, "a", "the string")));
    }

    static String funcJson4 = "{\"constant\":false,\n" +
            "   \"inputs\":[{\"name\":\"i\",\"type\":\"int[3]\"}, {\"name\":\"j\",\"type\":\"int[]\"}],\n" +
            "    \"name\":\"f2\",\n" +
            "    \"outputs\":[],\n" +
            "    \"type\":\"function\"\n" +
            " }";

    @Test
    public void test4() {
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

    static String funcJson5 = "{\"constant\":false,\n" +
            "   \"inputs\":[{\"name\":\"i\",\"type\":\"int\"}, " +
            "               {\"name\":\"s\",\"type\":\"bytes\"},\n" +
            "               {\"name\":\"j\",\"type\":\"int\"}],\n" +
            "    \"name\":\"f4\",\n" +
            "    \"outputs\":[],\n" +
            "    \"type\":\"function\"\n" +
            " }";

    @Test
    public void test5() {
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson5);

        Assert.assertEquals(
                "3ed2792b000000000000000000000000000000000000000000000000000000000000006f" +
                        "0000000000000000000000000000000000000000000000000000000000000060" +
                        "00000000000000000000000000000000000000000000000000000000000000de" +
                        "0000000000000000000000000000000000000000000000000000000000000003" +
                        "abcdef0000000000000000000000000000000000000000000000000000000000",
            Hex.toHexString(function.encode(111, new byte[] {(byte) 0xab, (byte) 0xcd, (byte) 0xef}, 222)));

    }
}
