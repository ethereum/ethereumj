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
package org.ethereum.db;

import org.ethereum.util.FastByteComparisons;

import com.google.common.primitives.UnsignedBytes;

import org.junit.BeforeClass;
import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.*;

public class ByteArrayWrapperTest {

    static ByteArrayWrapper wrapper1;
    static ByteArrayWrapper wrapper2;
    static ByteArrayWrapper wrapper3;
    static ByteArrayWrapper wrapper4;

    @BeforeClass
    public static void loadByteArrays() {

        String block = "f9072df8d3a077ef4fdaf389dca53236bcf7f72698e154eab2828f86fbc4fc6c"
                + "d9225d285c89a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0"
                + "a142fd40d493479476f5eabe4b342ee56b8ceba6ab2a770c3e2198e7a0faa0ca"
                + "43105f667dceb168eb4e0cdc98ef28a9da5c381edef70d843207601719a06785"
                + "f3860460b2aa29122698e83a5151b270e82532c1663e89e3df8c5445b8ca833f"
                + "f000018609184e72a000830f3e6f8227d2845387c58f80a00000000000000000"
                + "0000000000000000000000000000000094148d7738f78c04f90654f8c6f8a080"
                + "8609184e72a00082271094000000000000000000000000000000000000000080"
                + "b83a33604557602a5160106000396000f200604556330e0f602a59366000530a"
                + "0f602a596020600053013560005335576040600053016000546009581ca033a6"
                + "bfa5eb2f4b63f1b98bed9a987d096d32e56deecb050367c84955508f5365a015"
                + "034e7574ec073f0c448aac1d9f844387610dfef5342834b6825fbc35df5913a0"
                + "ee258e73d41ada73d8d6071ba7d236fbbe24fcfb9627fbd4310e24ffd87b961a"
                + "8203e9f90194f9016d018609184e72a000822710940000000000000000000000"
                + "00000000000000000080b901067f4e616d655265670000000000000000000000"
                + "00000000000000000000000000003057307f4e616d6552656700000000000000"
                + "000000000000000000000000000000000000577f436f6e666967000000000000"
                + "000000000000000000000000000000000000000073ccdeac59d35627b7de0933"
                + "2e819d5159e7bb72505773ccdeac59d35627b7de09332e819d5159e7bb72507f"
                + "436f6e6669670000000000000000000000000000000000000000000000000000"
                + "57336045576041516100c56000396000f20036602259604556330e0f600f5933"
                + "ff33560f601e5960003356576000335700604158600035560f602b590033560f"
                + "603659600033565733600035576000353357001ca0f3c527e484ea5546189979"
                + "c767b69aa9f1ad5a6f4b6077d4bccf5142723a67c9a069a4a29a2a315102fcd0"
                + "822d39ad696a6d7988c993bb2b911cc2a78bb8902d91a01ebe4782ea3ed224cc"
                + "bb777f5de9ee7b5bbb282ac08f7fa0ef95d3d1c1c6d1a1820ef7f8ccf8a60286"
                + "09184e72a00082271094ccdeac59d35627b7de09332e819d5159e7bb725080b8"
                + "4000000000000000000000000000000000000000000000000000000000000000"
                + "000000000000000000000000002d0aceee7e5ab874e22ccf8d1a649f59106d74"
                + "e81ba095ad45bf574c080e4d72da2cfd3dbe06cc814c1c662b5f74561f13e1e7"
                + "5058f2a057745a3db5482bccb5db462922b074f4b79244c4b1fa811ed094d728"
                + "e7b6da92a08599ea5d6cb6b9ad3311f0d82a3337125e05f4a82b9b0556cb3776"
                + "a6e1a02f8782132df8abf885038609184e72a000822710942d0aceee7e5ab874"
                + "e22ccf8d1a649f59106d74e880a0476176000000000000000000000000000000"
                + "00000000000000000000000000001ca09b5fdabd54ebc284249d2d2df6d43875"
                + "cb86c52bd2bac196d4f064c8ade054f2a07b33f5c8b277a408ec38d2457441d2"
                + "af32e55681c8ecb28eef3d2a152e8db5a9a0227a67fceb1bf4ddd31a7047e24b"
                + "e93c947ab3b539471555bb3509ed6e393c8e82178df90277f90250048609184e"
                + "72a0008246dd94000000000000000000000000000000000000000080b901e961"
                + "010033577f476176436f696e0000000000000000000000000000000000000000"
                + "000000000060005460006000600760006000732d0aceee7e5ab874e22ccf8d1a"
                + "649f59106d74e860645c03f150436000576000600157620f424060025761017d"
                + "5161006c6000396000f2006020360e0f61013f59602060006000374360205460"
                + "0056600054602056602054437f6e000000000000000000000000000000000000"
                + "00000000000000000000000000560e0f0f61008059437f6e0000000000000000"
                + "0000000000000000000000000000000000000000000000576000602054610400"
                + "60005304600053036000547f6400000000000000000000000000000000000000"
                + "0000000000000000000000005660016000030460406000200a0f61013e596001"
                + "60205301602054600a6020530b0f6100f45961040060005304600053017f6400"
                + "0000000000000000000000000000000000000000000000000000000000005760"
                + "20537f6900000000000000000000000000000000000000000000000000000000"
                + "000000576000537f640000000000000000000000000000000000000000000000"
                + "000000000000000057006040360e0f0f61014a59003356604054600035566060"
                + "546020356080546080536040530a0f6101695900608053604053033357608053"
                + "60605301600035571ba0190fc7ab634dc497fe1656fde523a4c26926d51a93db"
                + "2ba37af8e83c3741225da066ae0ec1217b0ca698a5369d4881e1c4cbde56af99"
                + "31ebf9281580a23b659c08a051f947cb2315d0259f55848c630caa10cd91d6e4"
                + "4ff8bad7758c65b25e2191308227d2c0";

        byte[] test1 = Hex.decode(block);
        byte[] test2 = Hex.decode(block);
        byte[] test3 = Hex.decode("4ff8bad7758c65b25e2191308227d2c0");
        byte[] test4 = Hex.decode("");

        wrapper1 = new ByteArrayWrapper(test1);
        wrapper2 = new ByteArrayWrapper(test2);
        wrapper3 = new ByteArrayWrapper(test3);
        wrapper4 = new ByteArrayWrapper(test4);
    }

    @Test
    public void testEqualsObject() {
        assertTrue(wrapper1.equals(wrapper2));
        assertFalse(wrapper1.equals(wrapper3));
        assertFalse(wrapper1.equals(wrapper4));
        assertFalse(wrapper1.equals(null));
        assertFalse(wrapper2.equals(wrapper3));
    }

    @Test
    public void testCompareTo() {
        assertTrue(wrapper1.compareTo(wrapper2) == 0);
        assertTrue(wrapper1.compareTo(wrapper3) > 1);
        assertTrue(wrapper1.compareTo(wrapper4) > 1);
        assertTrue(wrapper2.compareTo(wrapper3) > 1);
    }

    @Test
    public void testEqualsPerformance() {
        boolean testEnabled = false;

        if (testEnabled) {
            final int ITERATIONS = 10000000;
            long start1 = System.currentTimeMillis();
            for (int i = 0; i < ITERATIONS; i++) {
                Comparator<byte[]> comparator = UnsignedBytes
                        .lexicographicalComparator();

                comparator.compare(wrapper1.getData(),
                        wrapper2.getData());
            }
            System.out.println(System.currentTimeMillis() - start1 + "ms");

            long start2 = System.currentTimeMillis();
            for (int i = 0; i < ITERATIONS; i++) {
                Arrays.equals(wrapper1.getData(), wrapper2.getData());
            }
            System.out.println(System.currentTimeMillis() - start2 + "ms");

            long start3 = System.currentTimeMillis();
            for (int i = 0; i < ITERATIONS; i++) {
                FastByteComparisons.compareTo(wrapper1.getData(), 0, wrapper1.getData().length, wrapper2.getData(), 0, wrapper1.getData().length);
            }
            System.out.println(System.currentTimeMillis() - start3 + "ms");
        }
    }
}
