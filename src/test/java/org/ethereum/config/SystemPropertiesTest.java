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
package org.ethereum.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Anton Nashatyrev on 26.08.2015.
 */
public class SystemPropertiesTest {
    @Test
    public void punchBindIpTest() {
        SystemProperties.getDefault().overrideParams("peer.bind.ip", "");
        long st = System.currentTimeMillis();
        String ip = SystemProperties.getDefault().bindIp();
        long t = System.currentTimeMillis() - st;
        System.out.println(ip + " in " + t + " msec");
        Assert.assertTrue(t < 10 * 1000);
        Assert.assertFalse(ip.isEmpty());
    }

    @Test
    public void externalIpTest() {
        SystemProperties.getDefault().overrideParams("peer.discovery.external.ip", "");
        long st = System.currentTimeMillis();
        String ip = SystemProperties.getDefault().externalIp();
        long t = System.currentTimeMillis() - st;
        System.out.println(ip + " in " + t + " msec");
        Assert.assertTrue(t < 10 * 1000);
        Assert.assertFalse(ip.isEmpty());
    }

    @Test
    public void blockchainNetConfigTest() {
        SystemProperties systemProperties1 = new SystemProperties();
        systemProperties1.overrideParams("blockchain.config.name", "olympic");
        BlockchainNetConfig blockchainConfig1 = systemProperties1.getBlockchainConfig();
        SystemProperties systemProperties2 = new SystemProperties();
        systemProperties2.overrideParams("blockchain.config.name", "morden");
        BlockchainNetConfig blockchainConfig2= systemProperties2.getBlockchainConfig();
        Assert.assertNotEquals(blockchainConfig1.getClass(), blockchainConfig2.getClass());
    }
}
