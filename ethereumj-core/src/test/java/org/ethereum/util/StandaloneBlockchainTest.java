package org.ethereum.util;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 06.07.2016.
 */
public class StandaloneBlockchainTest {

    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Test
    public void constructorTest() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = sb.submitNewContract(
                "contract A {" +
                        "  uint public a;" +
                        "  uint public b;" +
                        "  function A(uint a_, uint b_) {a = a_; b = b_; }" +
                        "}",
                "A", 555, 777
        );
        Assert.assertEquals(BigInteger.valueOf(555), a.callConstFunction("a")[0]);
        Assert.assertEquals(BigInteger.valueOf(777), a.callConstFunction("b")[0]);

        SolidityContract b = sb.submitNewContract(
                "contract A {" +
                        "  string public a;" +
                        "  uint public b;" +
                        "  function A(string a_, uint b_) {a = a_; b = b_; }" +
                        "}",
                "A", "This string is longer than 32 bytes...", 777
        );
        Assert.assertEquals("This string is longer than 32 bytes...", b.callConstFunction("a")[0]);
        Assert.assertEquals(BigInteger.valueOf(777), b.callConstFunction("b")[0]);
    }

    @Test
    public void fixedSizeArrayTest() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        {
            SolidityContract a = sb.submitNewContract(
                    "contract A {" +
                            "  uint public a;" +
                            "  uint public b;" +
                            "  address public c;" +
                            "  address public d;" +
                            "  function f(uint[2] arr, address[2] arr2) {a = arr[0]; b = arr[1]; c = arr2[0]; d = arr2[1];}" +
                            "}");
            ECKey addr1 = new ECKey();
            ECKey addr2 = new ECKey();
            a.callFunction("f", new Integer[]{111, 222}, new byte[][] {addr1.getAddress(), addr2.getAddress()});
            Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("a")[0]);
            Assert.assertEquals(BigInteger.valueOf(222), a.callConstFunction("b")[0]);
            Assert.assertArrayEquals(addr1.getAddress(), (byte[])a.callConstFunction("c")[0]);
            Assert.assertArrayEquals(addr2.getAddress(), (byte[])a.callConstFunction("d")[0]);
        }

        {
            ECKey addr1 = new ECKey();
            ECKey addr2 = new ECKey();
            SolidityContract a = sb.submitNewContract(
                    "contract A {" +
                            "  uint public a;" +
                            "  uint public b;" +
                            "  address public c;" +
                            "  address public d;" +
                            "  function A(uint[2] arr, address a1, address a2) {a = arr[0]; b = arr[1]; c = a1; d = a2;}" +
                            "}", "A",
                    new Integer[]{111, 222}, addr1.getAddress(), addr2.getAddress());
            Assert.assertEquals(BigInteger.valueOf(111), a.callConstFunction("a")[0]);
            Assert.assertEquals(BigInteger.valueOf(222), a.callConstFunction("b")[0]);
            Assert.assertArrayEquals(addr1.getAddress(), (byte[]) a.callConstFunction("c")[0]);
            Assert.assertArrayEquals(addr2.getAddress(), (byte[]) a.callConstFunction("d")[0]);

            String a1 = "0x1111111111111111111111111111111111111111";
            String a2 = "0x2222222222222222222222222222222222222222";
        }
    }

    @Test
    public void encodeTest1() {
        StandaloneBlockchain sb = new StandaloneBlockchain().withAutoblock(true);
        SolidityContract a = sb.submitNewContract(
                "contract A {" +
                        "  uint public a;" +
                        "  function f(uint a_) {a = a_;}" +
                        "}");
        a.callFunction("f", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        BigInteger r = (BigInteger) a.callConstFunction("a")[0];
        System.out.println(r.toString(16));
        Assert.assertEquals(new BigInteger(Hex.decode("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")), r);
    }

}
