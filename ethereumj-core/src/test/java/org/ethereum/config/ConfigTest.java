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

import com.typesafe.config.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 13.07.2015.
 */
public class ConfigTest {

    @Test
    public void simpleTest() {
        Config config = ConfigFactory.parseResources("ethereumj.conf");
        System.out.println(config.root().render(ConfigRenderOptions.defaults().setComments(false)));
        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
//            System.out.println("Name:  " + entry.getKey());
//            System.out.println(entry);
        }
        System.out.println("peer.listen.port: " + config.getInt("peer.listen.port"));
        System.out.println("peer.discovery.ip.list: " + config.getAnyRefList("peer.discovery.ip.list"));
        System.out.println("peer.discovery.ip.list: " + config.getAnyRefList("peer.active"));
        List<? extends ConfigObject> list = config.getObjectList("peer.active");
        for (ConfigObject configObject : list) {
            if (configObject.get("url") != null) {
                System.out.println("URL: " + configObject.get("url"));
            }
            if (configObject.get("ip") != null) {
                System.out.println("IP: " + configObject);
            }
        }

        System.out.println("blocks.loader = " + config.hasPath("blocks.loader"));
        System.out.println("blocks.loader = " + config.getAnyRef("blocks.loader"));
    }

    @Test
    public void fallbackTest() {
        System.setProperty("blocks.loader", "bla-bla");
        Config config = ConfigFactory.load("ethereumj.conf");
        // Ignore this assertion since the SystemProperties are loaded by the static initializer
        // so if the ConfigFactory was used prior to this test the setProperty() has no effect
//        Assert.assertEquals("bla-bla", config.getString("blocks.loader"));
        String string = config.getString("keyvalue.datasource");
        Assert.assertNotNull(string);

        Config overrides = ConfigFactory.parseString("blocks.loader=another, peer.active=[{url=sdfsfd}]");
        Config merged = overrides.withFallback(config);
        Assert.assertEquals("another", merged.getString("blocks.loader"));
        Assert.assertTrue(merged.getObjectList("peer.active").size() == 1);
        Assert.assertNotNull(merged.getString("keyvalue.datasource"));

        Config emptyConf = ConfigFactory.parseFile(new File("nosuchfile.conf"), ConfigParseOptions.defaults());
        Assert.assertFalse(emptyConf.hasPath("blocks.loader"));
    }

    @Test
    public void ethereumjConfTest() {
        System.out.println("'" + SystemProperties.getDefault().databaseDir() + "'");
        System.out.println(SystemProperties.getDefault().peerActive());
    }
}
