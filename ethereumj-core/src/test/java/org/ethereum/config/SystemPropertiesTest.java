package org.ethereum.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Anton Nashatyrev on 26.08.2015.
 */
public class SystemPropertiesTest {
    @Test
    public void punchBindIpTest() {
        SystemProperties config = SystemProperties.getDefault();
        config.overrideParams("peer.bind.ip", "");
        long st = System.currentTimeMillis();
        String ip = config.bindIp();
        long t = System.currentTimeMillis() - st;
        System.out.println(ip + " in " + t + " msec");
        Assert.assertTrue(t < 10 * 1000);
        Assert.assertFalse(ip.isEmpty());
    }

    @Test
    public void externalIpTest() {
        SystemProperties config = SystemProperties.getDefault();
        config.overrideParams("peer.discovery.external.ip", "");
        long st = System.currentTimeMillis();
        String ip = config.externalIp();
        long t = System.currentTimeMillis() - st;
        System.out.println(ip + " in " + t + " msec");
        Assert.assertTrue(t < 10 * 1000);
        Assert.assertFalse(ip.isEmpty());
    }
}
