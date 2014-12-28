package test.ethereum.core;

import org.ethereum.core.Bloom;
import org.ethereum.crypto.SHA3Helper;

import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 20.11.2014
 */
public class BloomTest {


    @Test
    public void test1() {

        byte[] key = SHA3Helper.sha3(Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826"));

        Bloom bloom = Bloom.create(key);
        System.out.println(bloom);

    }

    @Test
    public void test2() {

        byte[] key = Hex.decode("0954D2BEF0CA79C1A988AE5FF3072C2AEA90F3967A9596065123F2A15AA37EF3");

        Bloom bloom = Bloom.create(key);
        System.out.println(bloom);
    }

    @Test
    public void test3() {

        byte[] key = SHA3Helper.sha3(Hex.decode("22341AE42D6DD7384BC8584E50419EA3AC75B83F "));

        Bloom bloom = Bloom.create(key);
        System.out.println(bloom);
    }


    @Test
    public void test4() {

        byte[] key = SHA3Helper.sha3(Hex.decode("0f572e5295c57f15886f9b263e2f6d2d6c7b5ec6"));

        Bloom bloom = Bloom.create(key);
        System.out.println(bloom);

    }

}
