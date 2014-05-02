package org.ethereum.crypto;

import junit.framework.Assert;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.util.Utils;
import org.junit.Test;

import java.math.BigInteger;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 17/04/14 15:02
 */

public class CryptoTest {



    @Test
    public void test1(){

        byte[] result = Utils.sha3("horse".getBytes());

        Assert.assertEquals("c87f65ff3f271bf5dc8643484f66b200109caffe4bf98c4cb393dc35740b28c0",
                Hex.toHexString(result));

        result = Utils.sha3("cow".getBytes());

        Assert.assertEquals("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4",
                Hex.toHexString(result));


    }

    @Test
    public void test2(){

        byte[] result = Utils.ripemd160("c87f65ff3f271bf5dc8643484f66b200109caffe4bf98c4cb393dc35740b28c0".getBytes());


                System.out.println(Hex.toHexString(result));

    }


    @Test
    public void test3(){

        BigInteger privKey = new BigInteger("cd244b3015703ddf545595da06ada5516628c5feadbf49dc66049c4b370cc5d8", 16);
        byte[] addr = Utils.privToAddress(privKey.toByteArray());
        Assert.assertEquals("89b44e4d3c81ede05d0f5de8d1a68f754d73d997", Hex.toHexString(addr));
    }

    @Test
    public void test4(){

        byte[] cowBytes = Utils.sha3("cow".getBytes());

        byte[] addr = Utils.privToAddress(cowBytes);
        Assert.assertEquals("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826", Hex.toHexString(addr).toUpperCase());
    }

    @Test
    public void test5(){

        byte[] horseBytes = Utils.sha3("horse".getBytes());

        byte[] addr = Utils.privToAddress(horseBytes);
        Assert.assertEquals("13978AEE95F38490E9769C39B2773ED763D9CD5F", Hex.toHexString(addr).toUpperCase());
    }


    @Test   /* performance test */
    public void test6(){


        long firstTime = System.currentTimeMillis();
        System.out.println(firstTime);
        for (int i = 0; i < 1000; ++i){

            byte[] horseBytes = Utils.sha3("horse".getBytes());

            byte[] addr = Utils.privToAddress(horseBytes);
            Assert.assertEquals("13978AEE95F38490E9769C39B2773ED763D9CD5F", Hex.toHexString(addr).toUpperCase());
        }
        long secondTime = System.currentTimeMillis();
        System.out.println(secondTime);

        System.out.println(secondTime - firstTime + " millisec");

        // 1) result: ~52 address calculation every second

    }


    @Test /* real tx hash calc */
    public void test7(){

        String txRaw = "F89D80809400000000000000000000000000000000000000008609184E72A000822710B3606956330C0D630000003359366000530A0D630000003359602060005301356000533557604060005301600054630000000C5884336069571CA07F6EB94576346488C6253197BDE6A7E59DDC36F2773672C849402AA9C402C3C4A06D254E662BF7450DD8D835160CBB053463FED0B53F2CDD7F3EA8731919C8E8CC";

        byte[] txHashB = Utils.sha3(Hex.decode(txRaw));
        String txHash = Utils.toHexString(txHashB);

        Assert.assertEquals("4b7d9670a92bf120d5b43400543b69304a14d767cf836a7f6abff4edde092895", txHash);

    }

    @Test /* real block hash calc */
    public void test8(){

        String blockRaw = "F885F8818080A01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D49347940000000000000000000000000000000000000000A0BCDDD284BF396739C224DBA0411566C891C32115FEB998A3E2B4E61F3F35582AA01DCC4DE8DEC75D7AAB85B567B6CCD41AD312451B948A7413F0A142FD40D4934783800000808080C0C0";

        byte[] blockHashB = Utils.sha3(Hex.decode(blockRaw));
        String blockHash = Utils.toHexString(blockHashB);

        System.out.println(blockHash);


    }


    @Test
    public void test9(){

        // todo: https://tools.ietf.org/html/rfc6979#section-2.2
        // todo: https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/signers/ECDSASigner.java


    }

        //
}
