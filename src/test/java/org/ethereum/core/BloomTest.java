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

        Assert.assertTrue(totalBloom.matches(addressBloom));
        Assert.assertTrue(totalBloom.matches(topicBloom));
        Assert.assertFalse(totalBloom.matches(Bloom.create(HashUtil.sha3(Hex.decode("1000000000000000000000000000000000000000000000000000000000000000")))));
        Assert.assertFalse(totalBloom.matches(Bloom.create(HashUtil.sha3(Hex.decode("195e7baea6a6c7c4c2dfeb977efac326af552d87")))));
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
