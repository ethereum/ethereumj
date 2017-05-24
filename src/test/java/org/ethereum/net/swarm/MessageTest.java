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

import org.ethereum.net.client.Capability;
import org.ethereum.net.swarm.bzz.BzzStatusMessage;
import org.ethereum.net.swarm.bzz.PeerAddress;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Admin on 25.06.2015.
 */
public class MessageTest {

    @Test
    public void statusMessageTest() {
        BzzStatusMessage m1 = new BzzStatusMessage(777, "IdString",
                new PeerAddress(new byte[] {127,0,0, (byte) 255}, 1010, new byte[] {1,2,3,4}), 888,
                Arrays.asList(new Capability[] {
                        new Capability("bzz", (byte) 0),
                        new Capability("shh", (byte) 202),
                }));
        byte[] encoded = m1.getEncoded();
        BzzStatusMessage m2 = new BzzStatusMessage(encoded);
        System.out.println(m1);
        System.out.println(m2);

        Assert.assertEquals(m1.getVersion(), m2.getVersion());
        Assert.assertEquals(m1.getId(), m2.getId());
        Assert.assertTrue(Arrays.equals(m1.getAddr().getIp(), m2.getAddr().getIp()));
        Assert.assertTrue(Arrays.equals(m1.getAddr().getId(), m2.getAddr().getId()));
        Assert.assertEquals(m1.getAddr().getPort(), m2.getAddr().getPort());
        Assert.assertEquals(m1.getNetworkId(), m2.getNetworkId());
        Assert.assertEquals(m1.getCapabilities(), m2.getCapabilities());
    }


}
