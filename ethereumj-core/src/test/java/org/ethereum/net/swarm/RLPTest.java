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
package org.ethereum.net.swarm;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * Created by Admin on 06.07.2015.
 */
public class RLPTest {
    @Test
    public void simpleTest() {
        for (int i = 0; i < 255; i++) {
            byte data = (byte) i;
            byte[] bytes1 = RLP.encodeElement(new byte[]{data});
            byte[] bytes2 = RLP.encodeByte(data);

            System.out.println(i + ": " + Arrays.toString(bytes1) + Arrays.toString(bytes2));
        }
    }

    @Test
    public void zeroTest() {
        {
            byte[] e = RLP.encodeList(
                    RLP.encodeString("aaa"),
                    RLP.encodeInt((byte) 0)
            );

            System.out.println(Hex.toHexString(e));

            RLPList l1 = (RLPList) RLP.decode2(e).get(0);

            System.out.println(new String (l1.get(0).getRLPData()));
            System.out.println(l1.get(1).getRLPData());

            byte[] rlpData = l1.get(1).getRLPData();
            byte ourByte = rlpData == null ? 0 : rlpData[0];

        }
        {
            byte[] e = RLP.encodeList(
                    //                RLP.encodeString("aaa"),
                    RLP.encodeElement(new byte[] {1}),
                    RLP.encodeElement(new byte[] {0})
            );

            System.out.println(Hex.toHexString(e));

        }
    }

    @Test
    public void frameHaderTest() {
        byte[] bytes = Hex.decode("c28080");
        RLPList list = RLP.decode2(bytes);
        System.out.println(list.size());
        System.out.println(list.get(0));

        byte[] bytes1 = RLP.encodeList(RLP.encodeInt(0));
        System.out.println(Arrays.toString(bytes1));
    }
}
