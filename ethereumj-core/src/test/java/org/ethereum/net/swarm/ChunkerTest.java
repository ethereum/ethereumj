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

    public static class ArrayReader implements SectionReader {
        byte[] arr;

        public ArrayReader(byte[] arr) {
            this.arr = arr;
        }

        @Override
        public long seek(long offset, int whence) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public int read(byte[] dest, int destOff) {
            return readAt(dest, destOff, 0);
        }

        @Override
        public int readAt(byte[] dest, int destOff, long readerOffset) {
            int len = min(dest.length - destOff, arr.length - (int)readerOffset);
            System.arraycopy(arr, (int) readerOffset, dest, destOff, len);
            return len;
        }

        @Override
        public long getSize() {
            return arr.length;
        }
    }

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
        ArrayReader r = new ArrayReader(arr);
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
