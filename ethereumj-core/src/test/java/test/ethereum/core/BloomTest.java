package test.ethereum.core;

import org.ethereum.core.Bloom;
import org.ethereum.crypto.HashUtil;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 20.11.2014
 */
public class BloomTest {


    @Test /// based on http://bit.ly/1MtXxFg
    public void test1(){

        byte[] address = Hex.decode("095e7baea6a6c7c4c2dfeb977efac326af552d87");
        Bloom addressBloom = Bloom.create(HashUtil.sha3(address));

        byte[] topic = Hex.decode("0000000000000000000000000000000000000000000000000000000000000000");
        Bloom topicBloom = Bloom.create(HashUtil.sha3(topic));

        Bloom totalBloom = new Bloom();
        totalBloom.or(addressBloom);
        totalBloom.or(topicBloom);


        Assert.assertEquals(
                "00000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020000000000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020000000000040000000000000000000000000000000000000000000000000000000",
                totalBloom.toString()
        );
    }


    @Test
    public void test2() {
        // todo: more testing
    }

    @Test
    public void test3() {
        // todo: more testing
    }


    @Test
    public void test4() {
        // todo: more testing
    }

}
