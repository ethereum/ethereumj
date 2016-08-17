package org.ethereum.util;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.net.MainNetConfig;
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
