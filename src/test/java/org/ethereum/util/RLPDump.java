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
package org.ethereum.util;

import org.ethereum.crypto.ECKey;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Anton Nashatyrev on 29.09.2015.
 */
public class RLPDump {
    @Test
    public void dumpTest() {
        System.out.println(Hex.toHexString(new ECKey().getPubKey()));
        String hexRlp = "f872f870845609a1ba64c0b8660480136e573eb81ac4a664f8f76e4887ba927f791a053ec5ff580b1037a8633320ca70f8ec0cdea59167acaa1debc07bc0a0b3a5b41bdf0cb4346c18ddbbd2cf222f54fed795dde94417d2e57f85a580d87238efc75394ca4a92cfe6eb9debcc3583c26fee8580";
        System.out.println(dump(RLP.decode2(Hex.decode(hexRlp)), 0));
        hexRlp = "f8d1f8cf845605846c3cc58479a94c49b8c0800b0b2d39d7c59778edb5166bfd0415c5e02417955ef4ef7f7d8c1dfc7f59a0141d97dd798bde6b972090390758b67457e93c2acb11ed4941d4443f87cedbc09c1b0476ca17f4f04da3d69cfb6470969f73d401ee7692293a00a2ff2d7f3fac87d43d85aed19c9e6ecbfe7e5f8268209477ffda58c7a481eec5c50abd313d10b6554e6e04a04fd93b9bf781d600f4ceb3060002ce1eddbbd51a9a902a970d9b41a9627141c0c52742b1179d83e17f1a273adf0a4a1d0346c68686a51428dd9a01";
        System.out.println(dump(RLP.decode2(Hex.decode(hexRlp)), 0));
        hexRlp = "dedd84560586f03cc58479a94c498e0c48656c6c6f205768697370657281bc";
        System.out.println(dump(RLP.decode2(Hex.decode(hexRlp)), 0));
        hexRlp = "dedd84560586f03cc58479a94c498e0c48656c6c6f205768697370657281bc";
        System.out.println(dump(RLP.decode2(Hex.decode(hexRlp)), 0));
    }

    public static String dump(RLPElement el, int indent) {
        String ret = "";
        if (el instanceof RLPList) {
            ret = Utils.repeat("  ", indent) + "[\n";
            for (RLPElement element : ((RLPList) el)) {
                ret += dump(element, indent + 1);
            }
            ret += Utils.repeat("  ", indent) + "]\n";
        } else {
            ret += Utils.repeat("  ", indent) +
                    (el.getRLPData() == null ? "<null>" : Hex.toHexString(el.getRLPData())) + "\n";
        }
        return ret;
    }
}
