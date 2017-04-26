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

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.min;

/**
 * Created by Admin on 19.06.2015.
 */
public class ChunkerTest {

    public static class SimpleChunkStore implements ChunkStore {
        Map<Key, byte[]> map = new HashMap<>();
        @Override
        public void put(Chunk chunk) {
            map.put(chunk.getKey(), chunk.getData());
        }
        @Override
        public Chunk get(Key key) {
            byte[] bytes = map.get(key);
            return bytes == null ? null : new Chunk(key, bytes);
        }
    }

    @Test
    public void simpleTest() {
        byte[] arr = new byte[200];
        new Random(0).nextBytes(arr);
        Util.ArrayReader r = new Util.ArrayReader(arr);
        TreeChunker tc = new TreeChunker(4, TreeChunker.DEFAULT_HASHER);
        ArrayList<Chunk> l = new ArrayList<>();
        Key root = tc.split(r, l);

        SimpleChunkStore chunkStore = new SimpleChunkStore();
        for (Chunk chunk : l) {
            chunkStore.put(chunk);
        }

        SectionReader reader = tc.join(chunkStore, root);
        long size = reader.getSize();
        int off = 30;
        byte[] arrOut = new byte[(int) size];
        int readLen = reader.read(arrOut, off);

        System.out.println("Read len: " + readLen);
        for (int i = 0; i < arr.length && off + i < arrOut.length; i++) {
            if (arr[i] != arrOut[off+ i]) throw new RuntimeException("Not equal at " + i);
        }
        System.out.println("Done.");
    }
}
