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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Created by Admin on 11.06.2015.
 */
public class ManifestTest {

    static String testManifest = "{\"entries\":[\n" +
            "  {\"path\":\"a/b\"},\n" +
            "  {\"path\":\"a\"},\n" +
            "  {\"path\":\"a/bb\"},\n" +
            "  {\"path\":\"a/bd\"},\n" +
            "  {\"path\":\"a/bb/c\"}\n" +
            "]}";

    static DPA dpa = new SimpleDPA();


    @Test
    public void simpleTest() {
        Manifest mf = new Manifest(dpa);
        mf.add(new Manifest.ManifestEntry("a", "hash1", "image/jpeg", Manifest.Status.OK));
        mf.add(new Manifest.ManifestEntry("ab", "hash2", "image/jpeg", Manifest.Status.OK));
        System.out.println(mf.dump());
        String hash = mf.save();
        System.out.println("Hash: " + hash);
        System.out.println(dpa);

        Manifest mf1 = Manifest.loadManifest(dpa, hash);
        System.out.println(mf1.dump());

        Manifest.ManifestEntry ab = mf1.get("ab");
        System.out.println(ab);
        Manifest.ManifestEntry a = mf1.get("a");
        System.out.println(a);

        System.out.println(mf1.dump());
    }

    @Test
    public void readWriteReadTest() throws Exception {
        String testManiHash = dpa.store(Util.stringToReader(testManifest)).getHexString();
        Manifest m = Manifest.loadManifest(dpa, testManiHash);
        System.out.println(m.dump());

        String nHash = m.save();

        Manifest m1 = Manifest.loadManifest(dpa, nHash);
    }
}
