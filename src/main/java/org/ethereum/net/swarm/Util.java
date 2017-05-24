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

import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.min;

/**
 * Created by Admin on 17.06.2015.
 */
public class Util {

    public static class ChunkConsumer extends LinkedBlockingQueue<Chunk> {
        ChunkStore destination;
        boolean synchronous = true;

        public ChunkConsumer(ChunkStore destination) {
            this.destination = destination;
        }

        @Override
        public boolean add(Chunk chunk) {
            if (synchronous) {
                destination.put(chunk);
                return true;
            } else {
                return super.add(chunk);
            }
        }
    }

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


    // for testing purposes when the timer might be changed
    // to manage current time according to test scenarios
    public static Timer TIMER = new Timer();

    public static class Timer {
        public long curTime() {
            return System.currentTimeMillis();
        }
    }

    public static String getCommonPrefix(String s1, String s2) {
        int pos = 0;
        while(pos < s1.length() && pos < s2.length() && s1.charAt(pos) == s2.charAt(pos)) pos++;
        return s1.substring(0, pos);
    }

    public static String ipBytesToString(byte[] ipAddr) {
        StringBuilder sip = new StringBuilder();
        for (int i = 0; i < ipAddr.length; i++) {
            sip.append(i == 0 ? "" : ".").append(0xFF & ipAddr[i]);
        }
        return sip.toString();
    }

    public static <P extends StringTrie.TrieNode<P>> String dumpTree(P n) {
        return dumpTree(n, 0);
    }

    private static <P extends StringTrie.TrieNode<P>> String dumpTree(P n, int indent) {
        String ret = Utils.repeat("  ", indent) + "[" + n.path + "] " + n + "\n";
        for (P c: n.getChildren()) {
            ret += dumpTree(c, indent + 1);
        }
        return ret;
    }

    public static byte[] uInt16ToBytes(int uInt16) {
        return new byte[] {(byte) ((uInt16 >> 8) & 0xFF), (byte) (uInt16 & 0xFF)};
    }

    public static long curTime() { return TIMER.curTime();}

    public static byte[] rlpEncodeLong(long n) {
        // TODO for now leaving int cast
        return RLP.encodeInt((int) n);
    }

    public static byte rlpDecodeByte(RLPElement elem) {
        return (byte) rlpDecodeInt(elem);
    }

    public static long rlpDecodeLong(RLPElement elem) {
        return rlpDecodeInt(elem);
    }

    public static int rlpDecodeInt(RLPElement elem) {
        byte[] b = elem.getRLPData();
        if (b == null) return 0;
        return ByteUtil.byteArrayToInt(b);
    }

    public static String rlpDecodeString(RLPElement elem) {
        byte[] b = elem.getRLPData();
        if (b == null) return null;
        return new String(b);
    }

    public static byte[] rlpEncodeList(Object ... elems) {
        byte[][] encodedElems = new byte[elems.length][];
        for (int i =0; i < elems.length; i++) {
            if (elems[i] instanceof Byte) {
                encodedElems[i] = RLP.encodeByte((Byte) elems[i]);
            } else if (elems[i] instanceof Integer) {
                encodedElems[i] = RLP.encodeInt((Integer) elems[i]);
            } else if (elems[i] instanceof Long) {
                encodedElems[i] = rlpEncodeLong((Long) elems[i]);
            } else if (elems[i] instanceof String) {
                encodedElems[i] = RLP.encodeString((String) elems[i]);
            } else if (elems[i] instanceof byte[]) {
                encodedElems[i] = ((byte[]) elems[i]);
            } else {
                throw new RuntimeException("Unsupported object: " + elems[i]);
            }
        }
        return RLP.encodeList(encodedElems);
    }

    public static SectionReader stringToReader(String s) {
        return new ArrayReader(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String readerToString(SectionReader sr) {
        byte[] bb = new byte[(int) sr.getSize()];
        sr.read(bb, 0);
        String s = new String(bb, StandardCharsets.UTF_8);
        return s;
    }
}
