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
package org.ethereum.mine;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.intToBytes;
import static org.ethereum.util.ByteUtil.intsToBytes;
import static org.ethereum.util.ByteUtil.longToBytes;
import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;
import static org.junit.Assert.assertArrayEquals;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.TestUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Anton Nashatyrev on 02.12.2015.
 */
public class EthashTest {
    @BeforeClass
    public static void setup() {
        SystemProperties.getDefault().setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }


    @Test // check exact values
    public void test_0() {
        byte[] rlp = Hex.decode(
                "f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000086015a1c28ae5e82bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        Block b = new Block(rlp);

        EthashAlgo ethash = new EthashAlgo();
        long cacheSize = ethash.getParams().getCacheSize(b.getNumber());
        long fullSize = ethash.getParams().getFullSize(b.getNumber());
        byte[] seedHash = ethash.getSeedHash(b.getNumber());
        int[] cache = ethash.makeCache(cacheSize, seedHash);
        byte[] blockTrunkHash = sha3(b.getHeader().getEncodedWithoutNonce());

        long nonce = ByteUtil.byteArrayToLong(b.getNonce());
        long timeSum = 0;
        for (int i = 0; i < 100; i++) {
            long s = System.currentTimeMillis();
            Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockTrunkHash, longToBytes(nonce));
            timeSum += System.currentTimeMillis() - s;
            System.out.println("Time: " + (System.currentTimeMillis() - s));
            nonce++;
        }

        Assert.assertTrue("hashimotoLigt took > 500ms in avrg", timeSum / 100 < 500);

        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockTrunkHash, b.getNonce());

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));

        byte[] boundary = b.getHeader().getPowBoundary();
        byte[] pow = b.getHeader().calcPowValue();

        assertArrayEquals(Hex.decode("0000000000bd59a74a8619f14c3d793747f1989a29ed6c83a5a488bac185679b"), boundary);
        assertArrayEquals(Hex.decode("000000000017f78925469f2f18fe7866ef6d3ed28d36fb013bc93d081e05809c"), pow);
        assertArrayEquals(pow, pair.getRight());
    }

    @Test
    public void cacheTest() {
        EthashAlgo ethash = new EthashAlgo();
        byte[] seed = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes();
        long cacheSize = 1024;
        long fullSize = 1024 * 32;
        int[] cache = ethash.makeCache(cacheSize, seed);

        Assert.assertArrayEquals(intsToBytes(cache, false),
                                 Hex.decode(
                                         "2da2b506f21070e1143d908e867962486d6b0a02e31d468fd5e3a7143aafa76a14201f63374314e2a6aaf84ad2eb57105dea3378378965a1b3873453bb2b78f9a8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995ca8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995ca8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995c259440b89fa3481c2c33171477c305c8e1e421f8d8f6d59585449d0034f3e421808d8da6bbd0b6378f567647cc6c4ba6c434592b198ad444e7284905b7c6adaf70bf43ec2daa7bd5e8951aa609ab472c124cf9eba3d38cff5091dc3f58409edcc386c743c3bd66f92408796ee1e82dd149eaefbf52b00ce33014a6eb3e50625413b072a58bc01da28262f42cbe4f87d4abc2bf287d15618405a1fe4e386fcdafbb171064bd99901d8f81dd6789396ce5e364ac944bbbd75a7827291c70b42d26385910cd53ca535ab29433dd5c5714d26e0dce95514c5ef866329c12e958097e84462197c2b32087849dab33e88b11da61d52f9dbc0b92cc61f742c07dbbf751c49d7678624ee60dfbe62e5e8c47a03d8247643f3d16ad8c8e663953bcda1f59d7e2d4a9bf0768e789432212621967a8f41121ad1df6ae1fa78782530695414c6213942865b2730375019105cae91a4c17a558d4b63059661d9f108362143107babe0b848de412e4da59168cce82bfbff3c99e022dd6ac1e559db991f2e3f7bb910cefd173e65ed00a8d5d416534e2c8416ff23977dbf3eb7180b75c71580d08ce95efeb9b0afe904ea12285a392aff0c8561ff79fca67f694a62b9e52377485c57cc3598d84cac0a9d27960de0cc31ff9bbfe455acaa62c8aa5d2cce96f345da9afe843d258a99c4eaf3650fc62efd81c7b81cd0d534d2d71eeda7a6e315d540b4473c80f8730037dc2ae3e47b986240cfc65ccc565f0d8cde0bc68a57e39a271dda57440b3598bee19f799611d25731a96b5dbbbefdff6f4f656161462633030d62560ea4e9c161cf78fc96a2ca5aaa32453a6c5dea206f766244e8c9d9a8dc61185ce37f1fc804459c5f07434f8ecb34141b8dcae7eae704c950b55556c5f40140c3714b45eddb02637513268778cbf937a33e4e33183685f9deb31ef54e90161e76d969587dd782eaa94e289420e7c2ee908517f5893a26fdb5873d68f92d118d4bcf98d7a4916794d6ab290045e30f9ea00ca547c584b8482b0331ba1539a0f2714fddc3a0b06b0cfbb6a607b8339c39bcfd6640b1f653e9d70ef6c985b"));
        int[] bytes = ethash.calcDatasetItem(cache, 0);
        Assert.assertArrayEquals(intsToBytes(bytes, false),
                                 Hex.decode(
                                         "b1698f829f90b35455804e5185d78f549fcb1bdce2bee006d4d7e68eb154b596be1427769eb1c3c3e93180c760af75f81d1023da6a0ffbe321c153a7c0103597"));

        byte[] blockHash = "~~~X~~~~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes();
        long nonce = 0x7c7c597cL;
        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockHash, longToBytes(nonce));

        // comparing mix hash
        Assert.assertArrayEquals(pair.getLeft(),
                                 Hex.decode("d7b668b90c2f26961d98d7dd244f5966368165edbce8cb8162dd282b6e5a8eae"));
        // comparing the final hash
        Assert.assertArrayEquals(pair.getRight(),
                                 Hex.decode("b8cb1cb3ac1a7a6e12c4bc90f2779ef97e661f7957619e677636509d2f26055c"));

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));
    }

    @Test
    public void cacheTestFast() {
        EthashAlgo ethash = new EthashAlgo();
        byte[] seed = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes();
        long cacheSize = 1024;
        long fullSize = 1024 * 32;
        int[] cache = ethash.makeCache(cacheSize, seed);


        Assert.assertArrayEquals(intsToBytes(cache, false),
                                 Hex.decode(
                                         "2da2b506f21070e1143d908e867962486d6b0a02e31d468fd5e3a7143aafa76a14201f63374314e2a6aaf84ad2eb57105dea3378378965a1b3873453bb2b78f9a8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995ca8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995ca8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995c259440b89fa3481c2c33171477c305c8e1e421f8d8f6d59585449d0034f3e421808d8da6bbd0b6378f567647cc6c4ba6c434592b198ad444e7284905b7c6adaf70bf43ec2daa7bd5e8951aa609ab472c124cf9eba3d38cff5091dc3f58409edcc386c743c3bd66f92408796ee1e82dd149eaefbf52b00ce33014a6eb3e50625413b072a58bc01da28262f42cbe4f87d4abc2bf287d15618405a1fe4e386fcdafbb171064bd99901d8f81dd6789396ce5e364ac944bbbd75a7827291c70b42d26385910cd53ca535ab29433dd5c5714d26e0dce95514c5ef866329c12e958097e84462197c2b32087849dab33e88b11da61d52f9dbc0b92cc61f742c07dbbf751c49d7678624ee60dfbe62e5e8c47a03d8247643f3d16ad8c8e663953bcda1f59d7e2d4a9bf0768e789432212621967a8f41121ad1df6ae1fa78782530695414c6213942865b2730375019105cae91a4c17a558d4b63059661d9f108362143107babe0b848de412e4da59168cce82bfbff3c99e022dd6ac1e559db991f2e3f7bb910cefd173e65ed00a8d5d416534e2c8416ff23977dbf3eb7180b75c71580d08ce95efeb9b0afe904ea12285a392aff0c8561ff79fca67f694a62b9e52377485c57cc3598d84cac0a9d27960de0cc31ff9bbfe455acaa62c8aa5d2cce96f345da9afe843d258a99c4eaf3650fc62efd81c7b81cd0d534d2d71eeda7a6e315d540b4473c80f8730037dc2ae3e47b986240cfc65ccc565f0d8cde0bc68a57e39a271dda57440b3598bee19f799611d25731a96b5dbbbefdff6f4f656161462633030d62560ea4e9c161cf78fc96a2ca5aaa32453a6c5dea206f766244e8c9d9a8dc61185ce37f1fc804459c5f07434f8ecb34141b8dcae7eae704c950b55556c5f40140c3714b45eddb02637513268778cbf937a33e4e33183685f9deb31ef54e90161e76d969587dd782eaa94e289420e7c2ee908517f5893a26fdb5873d68f92d118d4bcf98d7a4916794d6ab290045e30f9ea00ca547c584b8482b0331ba1539a0f2714fddc3a0b06b0cfbb6a607b8339c39bcfd6640b1f653e9d70ef6c985b"));
        int[] i = ethash.calcDatasetItem(cache, 0);
        Assert.assertArrayEquals(intsToBytes(i, false),
                                 Hex.decode(
                                         "b1698f829f90b35455804e5185d78f549fcb1bdce2bee006d4d7e68eb154b596be1427769eb1c3c3e93180c760af75f81d1023da6a0ffbe321c153a7c0103597"));
        //
        byte[] blockHash = "~~~X~~~~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes();
        long nonce = 0x7c7c597cL;
        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockHash, longToBytes(nonce));

        // comparing mix hash
        Assert.assertArrayEquals(pair.getLeft(),
                                 Hex.decode("d7b668b90c2f26961d98d7dd244f5966368165edbce8cb8162dd282b6e5a8eae"));
        // comparing the final hash
        Assert.assertArrayEquals(pair.getRight(),
                                 Hex.decode("b8cb1cb3ac1a7a6e12c4bc90f2779ef97e661f7957619e677636509d2f26055c"));

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));
    }

    @Test
    public void realBlockValidateTest1() {
        byte[] rlp = Hex.decode(
                "f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000086015a1c28ae5e82bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");

        Block b = new Block(rlp);

        EthashAlgo ethash = new EthashAlgo();
        long cacheSize = ethash.getParams().getCacheSize(b.getNumber());
        long fullSize = ethash.getParams().getFullSize(b.getNumber());
        byte[] seedHash = ethash.getSeedHash(b.getNumber());
        long s = System.currentTimeMillis();
        int[] cache = ethash.makeCache(cacheSize, seedHash);
        System.out.println("Cache generation took: " + (System.currentTimeMillis() - s) + " ms");
        byte[] blockTruncHash = sha3(b.getHeader().getEncodedWithoutNonce());

        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockTruncHash, b.getNonce());

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));

        byte[] boundary = b.getHeader().getPowBoundary();

        Assert.assertTrue(FastByteComparisons.compareTo(pair.getRight(), 0, 32, boundary, 0, 32) < 0);
    }

    @Test
    public void realBlockValidateTest2() {
        byte[] rlp = Hex.decode(
                "f9021af90215a06b42cf11dbb8a448a118939d1a68773f3deca05f8063d26113dac5f9f8ce6713a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479452bc44d5378309ee2abf1539bf71de1b7d7be3b5a037b5b65861017992bd33375bb71e0752d57eb94972a9496177f056aa340a2843a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b6e75611ca830a2ace832fefd880845668803198d783010203844765746887676f312e342e32856c696e7578a08eddfce4ba14ac38363b0534d12ed7ad4c224897dd443730256f04c6f835449f88108919adc0f2952bc0c0");
        Block b = new Block(rlp);
        System.out.println(b);
        boolean valid = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader());

        Assert.assertTrue(valid);
    }

    @Test
    public void realBlockValidateTest3() {
        String blocks =
                "f9021af90215a0cc395ced01d7af387640ac1258ab8819b84ca3e59ff476d933441c3800c63928a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794738db714c08b8a32a29e0e68af00215079aa9c5ca03665d3f9edac25c8a8cdad22945e0fb2e6812f679bea3445639b3890f310ced9a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b4637b26e7830a2ac9832fefd8808456687ffc98d783010203844765746887676f312e352e31856c696e7578a0a64edb0df18caa7d36fcb8ca740fee12d4d82b39883ddb3d729414122ba7410688524d07ed4c40ab09c0c0\n" +
                        "f9021af90215a0c49ccf0465b0222c815aed70ec9a8317ffe2cfe3539e9340c6bbeb06699a80dea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347942a65aca4d5fc5b5c859090a6c34d164135398226a00a550ad67f02eeb6030abf46f9c88435a05b058374ea1ee4c177ef28c4d2f475a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b53a07965b830a2aca832fefd880845668800198d783010302844765746887676f312e352e31856c696e7578a035f6b345260d8c3807bb0e805f23866c5ba9431a9e9a5e240101f541990f47f5886796ab8d23f3c3c2c0c0\n" +
                        "f9021af90215a076e41437e45099f2a0872a8df8623a644ead1136afe751358c56781513ecb74ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479452bc44d5378309ee2abf1539bf71de1b7d7be3b5a09b0dcb614329b921c4ef22eb4f8a913539806da3cbe43c1eafa37cfa3943a845a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b610aed75d830a2acb832fefd880845668800598d783010203844765746887676f312e342e32856c696e7578a056ac10c8b2378f46b610db89631968bc5f409f32934f287b95d5cfbe12cefd05886ba4adf9b970445ec0c0\n" +
                        "f902fdf90217a09daf27b854a6e1272c2f3070f7729ba5f4891a9dc324c360a0ad32d1d62419f7a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794790b8a3ce86e707ed0ed32bf89b3269692a23cc1a02b7e61e61837adb79b36eee4e1589b2c2140852bb105dc034d5cfa052c6ef5b8a0b36ebb69537e04d3bcd94a20bebcb12a8ef4715eb26fc1d19382392cc98db9b1a0c86bea989ed46040c5d1cbc39da108fb249bb9bf0804a5889e897f7e0c710864b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b539ecc193830a2acc832fefd882a410845668801a98d783010302844765746887676f312e352e31856c696e7578a0d82d30ce9d68e733738eee0c6f37c95968622878334f4f010e95acae000bb40188b34e28af786cc323f8e0f86f82d65e850ba43b740083015f9094ff45159c63fb01f45cd86767a020406f06faa8528820d3ff69fe230800801ba0d0ea8a650bf50b61b421e8275a79219988889c3e86460b323fb3d4d60bd80d3ca05a653410e7fd97786b3b503936127ece61ed3dcfabdbcbe8d7f06066f4a41687f86d822a3b850ba43b740082520894c47aaa860008be6f65b58c6c6e02a84e666efe318742087a56e84c00801ca0bc2b89b75b68e7ad8eb1db4de4aa550db79df3bd6430f2258fe55805ca7d7fe7a07d8b53c48f24788f21e3a26cfbd007cdab71a8ef7f28f6f6fae6ed4dcd4a0df1c0\n" +
                        "f9021af90215a07cda9a6543dfcafccba0aa0a1a78156ecc7f4b679b8401aec57472294db63a06a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479452bc44d5378309ee2abf1539bf71de1b7d7be3b5a0ac89cd6a0d58b13786cef698cfeda1f21edbfbd00b67db034cf2f62497b95359a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b61093ff3b830a2acd832fefd880845668802598d783010203844765746887676f312e342e32856c696e7578a0b969e4528849b36fc9c019e83e53cee58e5ef6c3fb3b00105570e602b2a39f1f88aabdc28bdf9e4799c0c0\n" +
                        "f9021af90215a06b42cf11dbb8a448a118939d1a68773f3deca05f8063d26113dac5f9f8ce6713a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479452bc44d5378309ee2abf1539bf71de1b7d7be3b5a037b5b65861017992bd33375bb71e0752d57eb94972a9496177f056aa340a2843a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b6e75611ca830a2ace832fefd880845668803198d783010203844765746887676f312e342e32856c696e7578a08eddfce4ba14ac38363b0534d12ed7ad4c224897dd443730256f04c6f835449f88108919adc0f2952bc0c0\n" +
                        "f9036ef90217a0cb927dc709468a107fac77151c6dff1ab73eabcccc57d82d238a7b5554f6db51a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794580992b51e3925e23280efb93d3047c82f17e038a03a68272caba0b4a825667d3d223bb8c18f6f793bd8151822b27798716c0b23cba01e360dfc633f5d2edad4e75cfa36555ae480ce346dcce8f253bb0d298e043dcaa0644b51189a7f9d4287e78fb923ced0b8b30edee234d6756fd06b45abc7ec12bdb90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008606b7be32fc9c830a2acf832fefd882f618845668803a98d783010400844765746887676f312e352e31856c696e7578a09a1a1cb1182fa1c8baeb7f2abd3f109d85b136f5968c76c3c26977dad5dba1ec88de7908cc14f57037f90150f86e82b609850ba43b740083015f909454cec426307c1acaa63b25aa48fade5df9e5d418872fe2475ad71000801ba0884206762dfebbdc69d5fb92ca1c33999a419f9c9ec116e02594f9535a30c9a0a05daf584f1b480ff599e4ed62278bac9c9d4c4f16af1d65504c13dbb713aa3539f86f82b60a850ba43b740083015f9094f442c4ab4d8cf106bcda6b1f7994485f3f4291a9880df897c536bccc00801ba0a7c7855917b5f8319651d241f3ca2ac5d728a76c83c43cb9e60adc9af1988051a009f117ce8cdfccc762d7a221abc19eb8773176c6f56b75c8ce2a1b907e0a3babf86d81d6850ba43b740082562294fbb1b73c4f0bda4f67dca266ce6ef42f520fbb988820d04471d11f6800801ba065586cc9545ea639580de624574644c34ea9bd0d2e7bfd8bab8f5ed3de572d86a0431d8fc8fcc803413f9f0ded6eaf267cb072bfed75fff97286623dd78e0e3d40c0";

        for (String s : blocks.split("\\n")) {
            Block b = new Block(Hex.decode(s));
            System.out.println(b);
            boolean valid = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader());

            Assert.assertTrue(valid);
        }
        System.out.println("OK");
    }


    @Test
    public void blockMineTest() throws Exception {
        byte[] rlp = Hex.decode(
                "f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000000010082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        Block b = new Block(rlp);

        System.out.println(b);

        long nonce = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get().nonce;
        b.setNonce(longToBytes(nonce));

        Assert.assertTrue(Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader()));

    }

    @Test
    public void changeEpochTestLight() throws Exception {
        List<Block> blocks = TestUtils.getRandomChain(new byte[32], 29999, 3);

        for (Block b : blocks) {
            b.getHeader().setDifficulty(ByteUtil.intToBytes(100));
            b.setNonce(new byte[0]);
            long nonce = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get().nonce;
            b.setNonce(longToBytes(nonce));

            Assert.assertTrue(Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader()));
        }
    }

    @Ignore // takes ~20 min
    @Test
    public void changeEpochTest() throws Exception {
        List<Block> blocks = TestUtils.getRandomChain(new byte[32], 29999, 3);

        for (Block b : blocks) {
            b.getHeader().setDifficulty(ByteUtil.intToBytes(100));
            b.setNonce(new byte[0]);
            long nonce = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mine(b).get().nonce;
            b.setNonce(longToBytes(nonce));

            Assert.assertTrue(Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader()));
        }
    }

    @Test
    public void mineCancelTest() throws Exception {
        byte[] rlp = Hex.decode(
                "f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000000010082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        // small difficulty
        Block b = new Block(rlp);

        // large difficulty
        Block difficultBlock = new Block(Hex.decode(
                "f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000001000082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0"));

        // first warming up for the cache to be created
        System.out.println("Warming...");
        long res = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get().nonce;

        System.out.println("Submitting...");
        Future<MinerIfc.MiningResult> light =
                Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(difficultBlock, 8);

        Thread.sleep(200);

        long s = System.nanoTime();
        boolean cancel = light.cancel(true);
        try {
            System.out.println("Waiting");
            light.get();
            Assert.assertTrue(false);
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            System.out.println("Exception: ok");
        }

        long t = System.nanoTime() - s;
        System.out.println("Time: " + (t / 1000) + " usec");
        Assert.assertTrue(cancel);
        Assert.assertTrue(t < 500_000_000);
        Assert.assertTrue(light.isCancelled());

        b.setNonce(new byte[0]);
        Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b, 8).get();
        boolean validate = Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).validate(b.getHeader());
        Assert.assertTrue(validate);
    }

    @Test
    @Ignore
    public void fullDagTime() {
        EthashAlgo ethashAlgo = new EthashAlgo();
        System.out.println("Calculating cache...");
        int[] cache = ethashAlgo.makeCache(16_000_000, ethashAlgo.getSeedHash(0));
        long s = System.currentTimeMillis();
        System.out.println("Calculating full DAG...");
        //        ethashAlgo.calcDataset(1_000_000_000, cache);
        int[][] ret = new int[(1_000_000_000 / ethashAlgo.getParams().getHASH_BYTES())][];
        long ss = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ethashAlgo.calcDatasetItem(cache, i);
            if (i % 10000 == 0 && i > 0) {
                System.out.println(
                        "Calculated " + i + " of " + ret.length + " in " + (System.currentTimeMillis() - s) / 1000 +
                                " sec " + "Speed: " + (i - 100000) / ((System.currentTimeMillis() - ss) / 1000d) +
                                " items/sec");
                if (i == 100000) { ss = System.currentTimeMillis(); }
            }
        }
        System.out.println("Calculated in " + (System.currentTimeMillis() - s) / 1000 + " sec");
    }

    @Test
    @Ignore
    public void fullDagMineTime() throws ExecutionException, InterruptedException {
        Ethash.fileCacheEnabled = true;
        byte[] rlp = Hex.decode(
                "f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000000010082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        // small difficulty
        Block b = new Block(rlp);
        b.getHeader().setDifficulty(longToBytesNoLeadZeroes(0x20000));
        b.setExtraData(new byte[]{});
        Ethash ethash = Ethash.getForBlock(SystemProperties.getDefault(), 0);
        System.out.println("Generating DAG...");
        ethash.mine(b).get();
        System.out.println("DAG generated...");

        System.out.println("Mining block with diff: " + b.getDifficultyBI());
        long s = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            b.setExtraData(intToBytes(i));
            ethash.mine(b, 8).get();
            if (!ethash.validate(b.getHeader())) {
                throw new RuntimeException("Not validated: " + b);
            }
            System.out.print(".");
        }
        System.out.println();
        System.out.println("Mined 100 blocks in " + (System.currentTimeMillis() - s) / 1000 + " sec");
    }
}
