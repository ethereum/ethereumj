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

import org.ethereum.crypto.HashUtil;

import com.cedarsoftware.util.DeepEquals;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.net.client.Capability;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.swarm.Util;
import org.junit.Ignore;
import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.math.BigInteger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.*;

import static org.ethereum.util.ByteUtil.byteArrayToInt;
import static org.ethereum.util.ByteUtil.wrap;
import static org.ethereum.util.RLP.*;
import static org.junit.Assert.*;
import static org.ethereum.util.RlpTestData.*;

public class RLPTest {

    @Test
    public void test1() throws UnknownHostException {

        String peersPacket = "F8 4E 11 F8 4B C5 36 81 " +
                "CC 0A 29 82 76 5F B8 40 D8 D6 0C 25 80 FA 79 5C " +
                "FC 03 13 EF DE BA 86 9D 21 94 E7 9E 7C B2 B5 22 " +
                "F7 82 FF A0 39 2C BB AB 8D 1B AC 30 12 08 B1 37 " +
                "E0 DE 49 98 33 4F 3B CF 73 FA 11 7E F2 13 F8 74 " +
                "17 08 9F EA F8 4C 21 B0";

        byte[] payload = Hex.decode(peersPacket);

        byte[] ip = decodeIP4Bytes(payload, 5);

        assertEquals(InetAddress.getByAddress(ip).toString(), ("/54.204.10.41"));
    }

    @Test
    public void test2() throws UnknownHostException {

        String peersPacket = "F8 4E 11 F8 4B C5 36 81 " +
                "CC 0A 29 82 76 5F B8 40 D8 D6 0C 25 80 FA 79 5C " +
                "FC 03 13 EF DE BA 86 9D 21 94 E7 9E 7C B2 B5 22 " +
                "F7 82 FF A0 39 2C BB AB 8D 1B AC 30 12 08 B1 37 " +
                "E0 DE 49 98 33 4F 3B CF 73 FA 11 7E F2 13 F8 74 " +
                "17 08 9F EA F8 4C 21 B0";

        byte[] payload = Hex.decode(peersPacket);
        int oneInt = decodeInt(payload, 11);

        assertEquals(oneInt, 30303);
    }

    @Test
    public void test3() throws UnknownHostException {

        String peersPacket = "F8 9A 11 F8 4B C5 36 81 " +
                "CC 0A 29 82 76 5F B8 40 D8 D6 0C 25 80 FA 79 5C " +
                "FC 03 13 EF DE BA 86 9D 21 94 E7 9E 7C B2 B5 22 " +
                "F7 82 FF A0 39 2C BB AB 8D 1B AC 30 12 08 B1 37 " +
                "E0 DE 49 98 33 4F 3B CF 73 FA 11 7E F2 13 F8 74 " +
                "17 08 9F EA F8 4C 21 B0 F8 4A C4 36 02 0A 29 " +
                "82 76 5F B8 40 D8 D6 0C 25 80 FA 79 5C FC 03 13 " +
                "EF DE BA 86 9D 21 94 E7 9E 7C B2 B5 22 F7 82 FF " +
                "A0 39 2C BB AB 8D 1B AC 30 12 08 B1 37 E0 DE 49 " +
                "98 33 4F 3B CF 73 FA 11 7E F2 13 F8 74 17 08 9F " +
                "EA F8 4C 21 B0 ";

        byte[] payload = Hex.decode(peersPacket);

        int nextIndex = 5;
        byte[] ip = decodeIP4Bytes(payload, nextIndex);
        assertEquals("/54.204.10.41", InetAddress.getByAddress(ip).toString());

        nextIndex = getNextElementIndex(payload, nextIndex);
        int port = decodeInt(payload, nextIndex);
        assertEquals(30303, port);

        nextIndex = getNextElementIndex(payload, nextIndex);
        BigInteger peerId = decodeBigInteger(payload, nextIndex);

        BigInteger expectedPeerId =
                new BigInteger("11356629247358725515654715129711890958861491612873043044752814241820167155109073064559464053586837011802513611263556758124445676272172838679152022396871088");
        assertEquals(expectedPeerId, peerId);

        nextIndex = getNextElementIndex(payload, nextIndex);
        nextIndex = getFirstListElement(payload, nextIndex);
        ip = decodeIP4Bytes(payload, nextIndex);
        assertEquals("/54.2.10.41", InetAddress.getByAddress(ip).toString());

        nextIndex = getNextElementIndex(payload, nextIndex);
        port = decodeInt(payload, nextIndex);
        assertEquals(30303, port);

        nextIndex = getNextElementIndex(payload, nextIndex);
        peerId = decodeBigInteger(payload, nextIndex);

        expectedPeerId =
                new BigInteger("11356629247358725515654715129711890958861491612873043044752814241820167155109073064559464053586837011802513611263556758124445676272172838679152022396871088");

        assertEquals(expectedPeerId, peerId);

        nextIndex = getNextElementIndex(payload, nextIndex);
        nextIndex = getFirstListElement(payload, nextIndex);
        assertEquals(-1, nextIndex);
    }

    @Test
    /** encode byte */
    public void test4() {

        byte[] expected = {(byte) 0x80};
        byte[] data = encodeByte((byte) 0);
        assertArrayEquals(expected, data);

        byte[] expected2 = {(byte) 0x78};
        data = encodeByte((byte) 120);
        assertArrayEquals(expected2, data);

        byte[] expected3 = {(byte) 0x7F};
        data = encodeByte((byte) 127);
        assertArrayEquals(expected3, data);
    }

    @Test
    /** encode short */
    public void test5() {

        byte[] expected = {(byte) 0x80};
        byte[] data = encodeShort((byte) 0);
        assertArrayEquals(expected, data);

        byte[] expected2 = {(byte) 0x78};
        data = encodeShort((byte) 120);
        assertArrayEquals(expected2, data);

        byte[] expected3 = { (byte) 0x7F};
        data = encodeShort((byte) 127);
        assertArrayEquals(expected3, data);

        byte[] expected4 = {(byte) 0x82, (byte) 0x76, (byte) 0x5F};
        data = encodeShort((short) 30303);
        assertArrayEquals(expected4, data);

        byte[] expected5 = {(byte) 0x82, (byte) 0x4E, (byte) 0xEA};
        data = encodeShort((short) 20202);
        assertArrayEquals(expected5, data);
    }

    @Test
    /** encode int */
    public void testEncodeInt() {

        byte[] expected = {(byte) 0x80};
        byte[] data = encodeInt(0);
        assertArrayEquals(expected, data);
        assertEquals(0, RLP.decodeInt(data, 0));

        byte[] expected2 = {(byte) 0x78};
        data = encodeInt(120);
        assertArrayEquals(expected2, data);
        assertEquals(120, RLP.decodeInt(data, 0));

        byte[] expected3 = {(byte) 0x7F};
        data = encodeInt(127);
        assertArrayEquals(expected3, data);
        assertEquals(127, RLP.decodeInt(data, 0));

        assertEquals(256, RLP.decodeInt(RLP.encodeInt(256), 0));
        assertEquals(255, RLP.decodeInt(RLP.encodeInt(255), 0));
        assertEquals(127, RLP.decodeInt(RLP.encodeInt(127), 0));
        assertEquals(128, RLP.decodeInt(RLP.encodeInt(128), 0));

        data = RLP.encodeInt(1024);
        assertEquals(1024, RLP.decodeInt(data, 0));

        byte[] expected4 = {(byte) 0x82, (byte) 0x76, (byte) 0x5F};
        data = encodeInt(30303);
        assertArrayEquals(expected4, data);
        assertEquals(30303, RLP.decodeInt(data, 0));

        byte[] expected5 = {(byte) 0x82, (byte) 0x4E, (byte) 0xEA};
        data = encodeInt(20202);
        assertArrayEquals(expected5, data);
        assertEquals(20202, RLP.decodeInt(data, 0));

        byte[] expected6 = {(byte) 0x83, 1, 0, 0};
        data = encodeInt(65536);
        assertArrayEquals(expected6, data);
        assertEquals(65536, RLP.decodeInt(data, 0));

        byte[] expected8 = {(byte) 0x84, (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        data = encodeInt(Integer.MAX_VALUE);
        assertArrayEquals(expected8, data);
        assertEquals(Integer.MAX_VALUE, RLP.decodeInt(data, 0));
    }

    @Test(expected = RuntimeException.class)
    public void incorrectZero() {
        RLP.decodeInt(new byte[]{0x00}, 0);
    }

    /**
     * NOTE: While negative numbers are not used in RLP, we usually use RLP
     * for encoding all data and sometime use -1 in primitive fields as null.
     * So, currently negative numbers encoding is allowed
     */
    @Ignore
    @Test(expected = RuntimeException.class)
    public void cannotEncodeNegativeNumbers() {
        encodeInt(Integer.MIN_VALUE);
    }

    @Test
    public void testMaxNumerics() {
        int expected1 = Integer.MAX_VALUE;
        assertEquals(expected1, decodeInt(encodeInt(expected1), 0));
        short expected2 = Short.MAX_VALUE;
        assertEquals(expected2, decodeShort(encodeShort(expected2), 0));
        long expected3 = Long.MAX_VALUE;
        assertEquals(expected3, decodeLong(encodeBigInteger(BigInteger.valueOf(expected3)), 0));
    }

    @Test
    /** encode BigInteger */
    public void test6() {

        byte[] expected = new byte[]{(byte) 0x80};
        byte[] data = encodeBigInteger(BigInteger.ZERO);
        assertArrayEquals(expected, data);
    }

    @Test
    /** encode string */
    public void test7() {

        byte[] data = encodeString("");
        assertArrayEquals(new byte[]{(byte) 0x80}, data);

        byte[] expected = {(byte) 0x90, (byte) 0x45, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x65,
                (byte) 0x75, (byte) 0x6D, (byte) 0x4A, (byte) 0x20, (byte) 0x43, (byte) 0x6C,
                (byte) 0x69, (byte) 0x65, (byte) 0x6E, (byte) 0x74};

        String test = "EthereumJ Client";
        data = encodeString(test);

        assertArrayEquals(expected, data);

        String test2 = "Ethereum(++)/ZeroGox/v0.5.0/ncurses/Linux/g++";

        byte[] expected2 = {(byte) 0xAD, (byte) 0x45, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x65,
                (byte) 0x75, (byte) 0x6D, (byte) 0x28, (byte) 0x2B, (byte) 0x2B, (byte) 0x29, (byte) 0x2F,
                (byte) 0x5A, (byte) 0x65, (byte) 0x72, (byte) 0x6F, (byte) 0x47, (byte) 0x6F, (byte) 0x78,
                (byte) 0x2F, (byte) 0x76, (byte) 0x30, (byte) 0x2E, (byte) 0x35, (byte) 0x2E, (byte) 0x30,
                (byte) 0x2F, (byte) 0x6E, (byte) 0x63, (byte) 0x75, (byte) 0x72, (byte) 0x73, (byte) 0x65,
                (byte) 0x73, (byte) 0x2F, (byte) 0x4C, (byte) 0x69, (byte) 0x6E, (byte) 0x75, (byte) 0x78,
                (byte) 0x2F, (byte) 0x67, (byte) 0x2B, (byte) 0x2B};

        data = encodeString(test2);
        assertArrayEquals(expected2, data);

        String test3 = "Ethereum(++)/ZeroGox/v0.5.0/ncurses/Linux/g++Ethereum(++)/ZeroGox/v0.5.0/ncurses/Linux/g++";

        byte[] expected3 = {(byte) 0xB8, (byte) 0x5A,
                (byte) 0x45, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x65,
                (byte) 0x75, (byte) 0x6D, (byte) 0x28, (byte) 0x2B, (byte) 0x2B, (byte) 0x29, (byte) 0x2F,
                (byte) 0x5A, (byte) 0x65, (byte) 0x72, (byte) 0x6F, (byte) 0x47, (byte) 0x6F, (byte) 0x78,
                (byte) 0x2F, (byte) 0x76, (byte) 0x30, (byte) 0x2E, (byte) 0x35, (byte) 0x2E, (byte) 0x30,
                (byte) 0x2F, (byte) 0x6E, (byte) 0x63, (byte) 0x75, (byte) 0x72, (byte) 0x73, (byte) 0x65,
                (byte) 0x73, (byte) 0x2F, (byte) 0x4C, (byte) 0x69, (byte) 0x6E, (byte) 0x75, (byte) 0x78,
                (byte) 0x2F, (byte) 0x67, (byte) 0x2B, (byte) 0x2B,

                (byte) 0x45, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x65,
                (byte) 0x75, (byte) 0x6D, (byte) 0x28, (byte) 0x2B, (byte) 0x2B, (byte) 0x29, (byte) 0x2F,
                (byte) 0x5A, (byte) 0x65, (byte) 0x72, (byte) 0x6F, (byte) 0x47, (byte) 0x6F, (byte) 0x78,
                (byte) 0x2F, (byte) 0x76, (byte) 0x30, (byte) 0x2E, (byte) 0x35, (byte) 0x2E, (byte) 0x30,
                (byte) 0x2F, (byte) 0x6E, (byte) 0x63, (byte) 0x75, (byte) 0x72, (byte) 0x73, (byte) 0x65,
                (byte) 0x73, (byte) 0x2F, (byte) 0x4C, (byte) 0x69, (byte) 0x6E, (byte) 0x75, (byte) 0x78,
                (byte) 0x2F, (byte) 0x67, (byte) 0x2B, (byte) 0x2B};

        data = encodeString(test3);
        assertArrayEquals(expected3, data);
    }

    @Test
    /** encode byte array */
    public void test8() {

        String byteArr = "ce73660a06626c1b3fda7b18ef7ba3ce17b6bf604f9541d3c6c654b7ae88b239"
                + "407f659c78f419025d785727ed017b6add21952d7e12007373e321dbc31824ba";

        byte[] byteArray = Hex.decode(byteArr);

        String expected = "b840" + byteArr;

        assertEquals(expected, Hex.toHexString(encodeElement(byteArray)));
    }

    @Test
    /** encode list */
    public void test9() {

        byte[] actuals = encodeList();
        assertArrayEquals(new byte[]{(byte) 0xc0}, actuals);
    }

    @Test
    /** encode null value */
    public void testEncodeElementNull() {

        byte[] actuals = encodeElement(null);
        assertArrayEquals(new byte[]{(byte) 0x80}, actuals);
    }

    @Test
    /** encode single byte 0x00 */
    public void testEncodeElementZero() {

        byte[] actuals = encodeElement(new byte[]{0x00});
        assertArrayEquals(new byte[]{0x00}, actuals);
    }

    @Test
    /** encode single byte 0x01 */
    public void testEncodeElementOne() {

        byte[] actuals = encodeElement(new byte[]{0x01});
        assertArrayEquals(new byte[]{(byte) 0x01}, actuals);
    }

    @Test
    /** found bug encode list affects element value,
     hhh... not really at  the end but keep the test */
    public void test10() {

   /* 2 */
        byte[] prevHash =
                {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        prevHash = encodeElement(prevHash);

   /* 2 */
        byte[] uncleList = HashUtil.sha3(encodeList(new byte[]{}));

   /* 3 */
        byte[] coinbase =
                {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00};
        coinbase = encodeElement(coinbase);

        byte[] header = encodeList(
                prevHash, uncleList, coinbase);

        assertEquals("f856a000000000000000000000000000000000000000000000000000000000000000001dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000",
                Hex.toHexString(header));
    }

    @Test
    public void test11() {
//        2240089100000070
        String tx = "F86E12F86B80881BC16D674EC8000094CD2A3D9F938E13CD947EC05ABC7FE734DF8DD8268609184E72A00064801BA0C52C114D4F5A3BA904A9B3036E5E118FE0DBB987FE3955DA20F2CD8F6C21AB9CA06BA4C2874299A55AD947DBC98A25EE895AABF6B625C26C435E84BFD70EDF2F69";
        byte[] payload = Hex.decode(tx);

        Queue<Integer> index = new LinkedList<>();
        fullTraverse(payload, 0, 0, payload.length, 1, index);

        // TODO: assert lenght overflow while parsing list in RLP
    }

    @Test
    public void test12() {

        String tx = "F86E12F86B80881BC16D674EC8000094CD2A3D9F938E13CD947EC05ABC7FE734DF8DD8268609184E72A00064801BA0C52C114D4F5A3BA904A9B3036E5E118FE0DBB987FE3955DA20F2CD8F6C21AB9CA06BA4C2874299A55AD947DBC98A25EE895AABF6B625C26C435E84BFD70EDF2F69";
        byte[] payload = Hex.decode(tx);

        RLPList rlpList = decode2(payload);

        RLPList.recursivePrint(rlpList);
        // TODO: add some asserts in place of just printing the rlpList
    }

    @Test /* very long peers msg */
    public void test13() {

        String peers = "f9 14 90 11 f8 4c c6 81 83 68 81 fc 04 82 76 5f b8 40 07 7e 53 7a 8b 36 73 e8 f1 b6 25 db cc " +
                "90 2e a7 d4 ce d9 40 2e 46 64 e8 73 67 95 12 cc 23 60 69 8e 53 42 56 52 a0 46 24 fc f7 8c db a1 a3 " +
                "23 30 87 a9 19 a3 4d 11 ae da ce ee b7 d8 33 fc bf 26 f8 4c c6 63 81 e7 58 81 af 82 76 5f b8 40 0a " +
                "b2 cd e8 3a 09 84 03 dd c2 ea 54 14 74 0d 8a 01 93 e4 49 c9 6e 11 24 19 96 7a bc 62 eb 17 cd ce d7 " +
                "7a e0 ab 07 5e 04 f7 dd dc d4 3f b9 04 8b e5 32 06 a0 40 62 0b de 26 cb 74 3f a3 12 31 9f f8 4d c7 " +
                "81 cf 81 db 45 81 9a 82 76 5f b8 40 19 c3 3d a7 03 1c ff 17 7e fa 84 2f aa 3d 31 bd 83 e1 76 4e c6 " +
                "10 f2 36 94 4a 9f 8a 21 c1 c5 1a 04 f4 7f 6b 5f c3 ef e6 5c af 36 94 43 63 5a fc 58 d8 f5 d4 e2 f1 " +
                "2a f9 ee ec 3c 6e 30 bf 0a 2b f8 4c c6 44 30 81 ad 81 a3 82 76 5f b8 40 1e 59 c2 82 08 12 94 80 84 " +
                "97 ae 7a 7e 97 67 98 c4 2b 8b cc e1 3c 9d 8b 0e cf 8a fe cd b5 df d4 ef a8 77 0f c0 d1 f7 de 63 c9 " +
                "16 40 e7 e8 b4 35 8c 9e 3e d0 f3 d6 c9 86 20 ad 7e a4 24 18 c9 ec f8 4b c5 1f 12 81 9e 48 82 76 5f " +
                "b8 40 1f 68 c0 75 c1 d8 7b c0 47 65 43 0f df b1 e5 d0 0f 1b 78 4e d6 be 72 1e 4c af f7 be b5 7b 4b " +
                "21 7b 95 da 19 b5 ec 66 04 58 68 b3 9a ac 2e 08 76 cf 80 f0 b6 8d 0f a2 0b db 90 36 be aa 70 61 ea " +
                "f8 4c c6 81 bf 81 ea 39 37 82 76 5f b8 40 21 78 0c 55 b4 7d b4 b1 14 67 b5 f5 5b 0b 55 5e 08 87 ce " +
                "36 fb d9 75 e2 24 b1 c7 0e ac 7a b8 e8 c2 db 37 f0 a4 8b 90 ff dd 5a 37 9a da 99 b6 a0 f6 42 9c 4a " +
                "53 c2 55 58 19 1a 68 26 36 ae f4 f2 f8 4c c6 44 30 81 ad 81 a3 82 76 5f b8 40 23 15 cb 7c f4 9b 8e " +
                "ab 21 2c 5a 45 79 0b 50 79 77 39 73 8f 5f 73 34 39 b1 90 11 97 37 ee 8c 09 bc 72 37 94 71 2a a8 2f " +
                "26 70 bc 58 1a b0 75 7e f2 31 37 ac 0f df 0f 8c 89 65 e7 dd 6b a7 9f 8c f8 4e c8 81 bf 81 b1 81 d1 " +
                "81 9f 82 76 5f b8 40 24 9a 36 41 e5 a8 d0 8e 41 a5 cf c8 da e1 1f 17 61 25 4f 4f d4 7d 9b 13 33 8d " +
                "b8 e6 e3 72 9e 6f 2a c9 ec 09 7a 5c 80 96 84 d6 2a 41 e6 df c2 ff f7 2d c3 db d9 7e a2 61 32 bb 97 " +
                "64 05 65 bb 0c f8 4a c4 55 41 7e 2d 82 76 5f b8 40 2a 38 ea 5d 9a 7e fd 7f ff c0 a8 1d 8e a7 ed 28 " +
                "31 1c 40 12 bb ab 14 07 c8 da d2 68 51 29 e0 42 17 27 34 a3 28 e8 90 7f 90 54 b8 22 5f e7 70 41 d8 " +
                "a4 86 a9 79 76 d2 83 72 42 ab 6c 8c 59 05 e4 f8 4c c6 81 83 68 81 fc 04 82 76 5f b8 40 32 4d d9 36 " +
                "38 4d 8c 0d de fd e0 4b a7 40 29 98 ab bd 63 d7 9c 0b f8 58 6b 3d d2 c7 db f6 c9 1e b8 0a 7b 6d e8 " +
                "f1 6a 50 04 4f 14 9c 7b 39 aa fb 9c 3a d7 f2 ca a4 03 55 aa b0 98 88 18 6f cc a2 f8 4c c6 44 30 81 " +
                "ad 81 a3 82 76 5f b8 40 39 42 45 c0 99 16 33 ed 06 0b af b9 64 68 53 d3 44 18 8b 80 4f e3 7e 25 a5 " +
                "bc ac 44 ed 44 3a 84 a6 8b 3a af 15 5e fe 48 61 e8 4b 4b 51 5f 9a 5d ec db d7 da e9 81 92 d7 a3 20 " +
                "a7 92 c7 d4 df af f8 4d c7 56 81 b7 81 e7 81 cd 82 76 5f b8 40 39 86 50 f6 7b 22 92 93 9d e3 4c 0e " +
                "ae b9 14 1f 94 84 a0 fb 17 3f a3 3f 81 a1 f7 31 5d 0e b7 7b de 3a 76 c3 86 36 fa e6 6f a1 4b f2 af " +
                "df d6 3e 60 ab d4 0e 29 b0 2a 91 4e 65 de 57 89 98 3f d4 f8 4c c6 44 81 b9 81 ea 40 82 76 5f b8 40 " +
                "3a 15 58 7a 1c 3a da bf 02 91 b3 07 f7 1b 2c 04 d1 98 aa e3 6b 83 49 95 d3 30 5d ff 42 f1 ab 86 f4 " +
                "83 ae 12 9e 92 03 fb c6 ef 21 87 c8 62 1e dd 18 f6 1d 53 ea a5 b5 87 ff de a4 d9 26 48 90 38 f8 4d " +
                "c7 81 cf 81 db 45 81 9a 82 76 5f b8 40 3b 14 62 04 0e a7 78 e3 f7 5e 65 ce 24 53 41 8a 66 2e 62 12 " +
                "c9 f6 5b 02 ea b5 8d 22 b2 87 e4 50 53 bd e5 eb f0 60 96 0c bf a0 d9 dc 85 bf 51 ba 7a a1 f2 ca a2 " +
                "c1 36 82 d9 32 77 64 1d 60 db eb f8 4c c6 6a 81 a8 0e 81 f9 82 76 5f b8 40 3e cc 97 ab 15 d2 2f 7b " +
                "9e df 19 c0 4c e3 b6 09 5f a2 50 42 14 00 2b 35 98 9c 6f 81 ee 4b 96 1c c2 a8 99 c4 94 15 c9 14 e3 " +
                "13 90 83 40 04 7d 1d 3b 25 d7 4f 5b 9c 85 a0 6a fa 26 59 a5 39 99 2e f8 4b c5 2e 04 81 c1 09 82 76 " +
                "5f b8 40 40 7c 22 00 3f 3b ba a6 cb eb 8e 4b 0a b7 07 30 73 fe ab 85 18 2b 40 55 25 f8 bd 28 32 55 " +
                "04 3d 71 35 18 f7 47 48 d9 2c 43 fb b9 9e cc 7c 3f ba b9 5d 59 80 06 51 3a a8 e5 9c 48 04 1c 8b 41 " +
                "c2 f8 4b c5 32 7e 56 81 c2 82 76 5f b8 40 40 8c 93 24 20 3b d8 26 2f ce 65 06 ba 59 dc dd 56 70 89 " +
                "b0 eb 9a 5b b1 83 47 7b ab bf 61 63 91 4a cd c7 f4 95 f8 96 4d 8a c1 2f e2 40 18 87 b8 cd 8d 97 c0 " +
                "c9 dc cf ad db b2 0a 3c 31 47 a7 89 f8 4a c4 26 6c 4f 68 82 76 5f b8 40 42 3e 40 04 da 2f a7 50 0b " +
                "c0 12 c0 67 4a a6 57 15 02 c5 3a a4 d9 1e fa 6e 2b 5c b1 e4 68 c4 62 ca 31 14 a2 e2 eb 09 65 b7 04 " +
                "4f 9c 95 75 96 5b 47 e4 7a 41 f1 3f 1a dc 03 a2 a4 b3 42 d7 12 8d f8 4b c5 40 81 e7 08 2d 82 76 5f " +
                "b8 40 42 83 93 75 27 2c 2f 3d ea db 28 08 5d 06 05 5e 35 31 35 c6 c8 d8 96 09 7a 1b c4 80 c4 88 4f " +
                "d1 60 45 18 cb df 73 1a c1 8f 09 84 b7 f0 21 48 e8 82 90 d1 3c 22 4d 82 46 43 14 e2 b5 96 2e 3f 89 " +
                "f8 4d c7 32 81 aa 81 d8 81 c8 82 76 5f b8 40 44 cf 19 44 6c a4 65 01 8e 4d e6 c6 0f c0 df 52 9e ba " +
                "25 02 92 ef 74 41 e1 db 59 84 1c 69 f0 22 f6 09 28 10 c9 a5 a7 f2 74 f2 f9 7c 4b d6 c7 6e ad c0 64 " +
                "c7 d6 59 7c ae b1 7e d8 7c b2 57 73 5f f8 4b c5 32 81 9c 5a 53 82 76 5f b8 40 46 1c 9b 54 e9 19 53 " +
                "c5 bb c3 1c 67 12 a9 17 38 2b e6 7d 60 f7 5e b7 f5 06 51 be a3 e5 94 d0 d1 9c 22 29 d8 f6 6a db 3f " +
                "20 3f 60 00 38 e7 cc 93 4d c9 27 87 fa c4 39 2b 9b fa 7c bc 78 6f d0 5b f8 4b c5 81 86 64 7d 29 82 " +
                "76 5f b8 40 48 35 3a 00 58 e2 64 48 d9 4e 59 33 6c ca 9d 28 a9 37 41 20 de f7 6c 4b cc fe e1 8b 01 " +
                "23 e5 91 92 39 3a 2e e3 04 4d 80 e0 ee cb b0 94 76 be 62 fd e1 e8 74 f9 3d 05 ea 5c 4a 9a 45 c0 6e " +
                "8f e1 f8 4b c5 4e 08 05 81 bb 82 76 5f b8 40 48 e8 95 09 49 d4 c0 0b cd bb e9 39 c5 bf 07 8f 2c bf " +
                "f1 08 84 af 16 60 b1 c3 22 b9 ca a3 ba 35 7b b4 15 7f c6 b0 03 9a f9 43 8d fe 51 ec 27 8a 47 fc d3 " +
                "b7 26 fa 0a 08 7d 4c 3c 01 a6 2f 33 5e f8 4a c6 58 45 81 c6 81 c6 07 b8 40 4a 02 55 fa 46 73 fa a3 " +
                "0f c5 ab fd 3c 55 0b fd bc 0d 3c 97 3d 35 f7 26 46 3a f8 1c 54 a0 32 81 cf ff 22 c5 f5 96 5b 38 ac " +
                "63 01 52 98 77 57 a3 17 82 47 85 49 c3 6f 7c 84 cb 44 36 ba 79 d6 d9 f8 4b c5 40 81 e7 08 2d 82 76 " +
                "5f b8 40 4c 75 47 ab 4d 54 1e 10 16 4c d3 74 1f 34 76 ed 19 4b 0a b9 a1 36 df ca c3 94 3f 97 35 8c " +
                "9b 05 14 14 27 36 ca 2f 17 0f 12 52 29 05 7b 47 32 44 a6 23 0b f5 47 1a d1 68 18 85 24 b2 b5 cd 8b " +
                "7b f8 4c c6 44 30 81 ad 81 a3 82 76 5f b8 40 4d 5e 48 75 d6 0e b4 ee af b6 b2 a7 d3 93 6e d3 c9 bc " +
                "58 ac aa de 6a 7f 3c 5f 25 59 8c 20 b3 64 f1 2b ea 2f b1 db 3b 2c 2e f6 47 85 a4 7d 6b 6b 5b 10 34 " +
                "27 cb ac 0c 88 b1 8f e9 2a 9f 53 93 f8 f8 4b c5 52 0c 81 e3 54 82 76 5f b8 40 4f d8 98 62 75 74 d3 " +
                "e8 6b 3f 5a 65 c3 ed c2 e5 da 84 53 59 26 e4 a2 88 20 b0 03 8b 19 63 6e 07 db 5e b0 04 d7 91 f8 04 " +
                "1a 00 6e 33 e1 08 e4 ec 53 54 99 d1 28 d8 d9 c5 ca f6 bb dc 22 04 f7 6a f8 4b c5 81 b4 20 2b 08 82 " +
                "76 5f b8 40 53 cc f2 5a b5 94 09 ec bb 90 3d 2e c3 a9 aa 2e b3 9d 7c c4 c7 db 7e 6f 68 fd 71 1a 7c " +
                "eb c6 06 21 6d e7 37 82 6d a4 20 93 e3 e6 52 1e e4 77 0e b2 d6 69 dc 4b f3 54 6c c7 57 c3 40 12 69 " +
                "6e ae f8 4c c6 6a 81 a8 0e 81 f9 82 76 5f b8 40 54 b3 93 15 69 91 39 87 80 50 2f a8 f4 14 13 79 bc " +
                "e2 69 31 be 87 ba 8e 0b 74 9b a9 05 a9 e9 76 e5 de 6d 39 c9 8c f0 48 f2 5c 3c bb b8 c7 f3 02 c4 e6 " +
                "04 ad 5b f7 2c db 06 10 0f 50 0d e3 a6 86 f8 4a c4 4c 67 37 47 82 76 5f b8 40 60 0a 77 fb 14 e7 92 " +
                "c0 c7 0d c4 ad e3 82 ed 60 43 62 b9 78 b1 9b 94 c4 ed 18 83 38 a1 79 5d 2d b4 5f 7f 22 3b 66 ba eb " +
                "a3 91 c5 9b 55 88 b4 4e ba f7 1c 7e b3 97 55 c2 72 29 c7 fd e6 41 be ce f8 4b c5 6d 2b 81 9a 42 82 " +
                "76 5f b8 40 69 dd 44 5f 67 c3 be f3 94 f9 54 9f da e1 62 3d bc 20 88 4a 62 fd 56 16 dd bb 49 f8 4b " +
                "a8 7e 14 7c b8 a5 0b a9 71 d7 30 c4 62 1d 0e b6 51 33 49 4e 94 fa 5e a2 e6 9c 66 1f 6b 12 e7 ed 2a " +
                "8d 4e f8 4b c5 18 09 3d 81 9b 82 76 5f b8 40 6b 5d 4c 35 ff d1 f5 a1 98 03 8a 90 83 4d 29 a1 b8 8b " +
                "e0 d5 ef ca 08 bc 8a 2d 58 81 18 0b 0b 41 6b e0 06 29 aa be 45 0a 50 82 8b 8d 1e e8 2d 98 f5 52 81 " +
                "87 ee 67 ed 6e 07 3b ce ef cd fb 2b c9 f8 4a c4 55 41 7e 2d 82 76 5f b8 40 6c bb 1e d5 36 dc 38 58 " +
                "c1 f0 63 42 9b d3 95 2a 5d 32 ef 8e 11 52 6c df e7 2f 41 fe a1 ac e9 60 18 7c 99 75 ab bc 23 78 35 " +
                "11 c0 0f 26 98 35 47 47 f9 05 aa ac 11 dc d2 b7 47 8b 3e af 32 7a c6 f8 4b c5 40 81 e7 08 2d 82 76 " +
                "5f b8 40 6e a2 8f 64 ea 1c c3 b6 57 25 44 fd 5b f7 43 b0 ea ab e0 17 f5 14 73 0c 89 7d a3 c7 7f 03 " +
                "c5 16 f1 e5 f3 1d 79 3b 4b ce 3c aa 1d ed 56 35 6d 20 b2 eb b5 5a 70 66 f4 1c 25 b7 c3 d5 66 14 e0 " +
                "6b f8 4a c4 55 41 7e 2d 82 76 5f b8 40 72 53 24 08 e8 be 6d 5e 2c 9f 65 0f b9 c9 f9 96 50 cc 1f a0 " +
                "62 a4 a4 f2 cf e4 e6 ae 69 cd d2 e8 b2 3e d1 4a fe 66 95 5c 23 fa 04 8f 3a 97 6e 3c e8 16 9e 50 5b " +
                "6a 89 cc 53 d4 fa c2 0c 2a 11 bf f8 4c c6 52 81 d9 48 81 a9 82 76 5f b8 40 7a ee a4 33 60 b9 36 8b " +
                "30 e7 f4 82 86 61 3f d1 e3 b0 20 7f b7 1f 03 08 d5 04 12 11 44 63 e7 7a b8 30 27 c0 d4 0c ad aa b8 " +
                "bb f6 12 fc 5b 69 67 fa 1c 40 73 29 d4 7e c6 1f b0 dc 3d a1 08 68 32 f8 4c c6 81 a6 81 93 53 4f 82 " +
                "76 5f b8 40 7b 3c dd e0 58 d5 b4 5d 8d b2 24 36 60 cf ea 02 e0 74 ec 21 31 14 c2 51 d7 c0 c3 2d 04 " +
                "03 bb 7a b4 77 13 d2 49 2f f6 c8 81 cf c2 aa c3 f5 2c b2 69 76 8c 89 68 f3 b6 b1 8b ac 97 22 d0 53 " +
                "31 f6 f8 4c c6 6a 81 a8 0e 81 f9 82 76 5f b8 40 87 ab 58 1b b9 7c 21 2a 2d a7 ef 0d 6e 10 5e 41 b5 " +
                "5e 4e 42 cb b6 a1 af 9a 76 1a 01 ca 8c 65 06 9a b4 b5 82 7e 32 2c f2 c5 f5 9e 7f 59 2b e2 a8 17 c4 " +
                "5a b6 41 f5 a9 dd 36 89 63 c7 3f 9e e6 88 f8 4c c6 52 81 d9 48 81 a9 82 76 5f b8 40 8c 66 0d bc 6d " +
                "3d b0 18 6a d1 0f 05 fd 4f 2f 06 43 77 8e c5 14 e8 45 2a 75 50 c6 30 da 21 17 1a 29 b1 bb 67 c2 e8 " +
                "e1 01 ea 1d b3 97 43 f3 e7 8c 4d 26 76 a1 3d 15 51 51 21 51 5f c3 8b 04 8f 37 f8 4c c6 63 81 e7 58 " +
                "81 af 82 76 5f b8 40 94 fe 3d 52 a2 89 4c ed c6 b1 54 24 15 6e b8 73 8a 84 41 dd 74 ba 9c ed 66 64 " +
                "ed 30 a3 32 a9 5b 57 4d 89 26 2e a3 67 fa 90 0a e9 70 6f b8 1a 40 82 87 bd de f3 a9 dd 9f f4 4e 3a " +
                "41 bc 09 0f dc f8 4d c7 81 d5 81 81 81 e6 0a 82 76 5f b8 40 95 21 14 f1 10 e8 ac 00 df ea 5f 05 0d " +
                "95 5e 76 4c 7c ba 8f b2 07 c0 5a 7a a5 ae 84 91 68 64 0a 2b 4e 31 43 91 fc 3a 76 79 5b 38 27 05 54 " +
                "62 63 9c ff 4a e2 d6 4a b8 0e 95 27 44 28 31 3e 36 6a f8 4c c6 58 45 81 c6 81 c6 82 76 5f b8 40 96 " +
                "f3 47 b0 96 ed 16 30 f4 74 b9 76 23 e4 5e 8d 47 1b 1d 43 c2 2f 59 96 07 c8 b2 e3 ed 0d 7b 79 05 d8 " +
                "55 4a d3 99 db d7 39 c7 61 26 40 44 24 d8 db 0d c7 d2 b0 47 c1 a3 28 ae 27 d4 09 06 c5 83 f8 4c c6 " +
                "81 83 68 81 fc 04 82 76 5f b8 40 9a 22 c8 fb 1b d8 bb d0 2f 0e 74 ed 9d 3d 55 b0 f5 b0 96 72 bc 43 " +
                "a2 d4 7b 1e d0 42 38 c1 c3 2b 6a 65 74 26 52 5b 15 51 82 36 e9 78 9b 54 6a 4a 07 2a 60 5e 13 73 fe " +
                "5b 99 6b ae dc 30 35 94 28 f8 4b c5 52 0c 81 e3 54 82 76 5f b8 40 9b 1a 3a 8d 77 1b 3d 94 9c a3 94 " +
                "a8 8e b5 dc 29 a9 53 b0 2c 81 f0 17 36 1f fc 0a fe 09 ab ce 30 69 17 1a 87 d4 74 52 36 87 fc c9 a9 " +
                "d3 2c c0 2c fa b4 13 22 56 fe aa bf e0 5f 7a c7 47 19 4e 88 f8 4b c5 42 81 d7 78 1c 82 76 5f b8 40 " +
                "9f a7 e5 5b 2d 98 f1 d7 44 c7 62 32 e4 fd a2 42 fe 9f d3 d5 74 3d 16 d3 ca d2 e5 48 a0 7c b5 af 06 " +
                "fe 60 eb ae b8 c6 09 50 28 17 92 34 dc dd d3 cd cf 1f cf e6 ed aa 2a 53 30 7f d1 03 da 4a f0 f8 4a " +
                "c4 55 41 7e 2d 82 76 5f b8 40 a0 1f 83 4e 9d 1a 61 3c 3c 74 7e 56 1c ac 19 cb 12 d8 79 c1 a5 74 20 " +
                "a4 9c 23 65 2b 8f 51 28 8c 8b 11 1a a3 88 89 98 b0 5e 32 7f 47 a2 35 c6 a4 a3 77 f8 88 e3 00 5a 2d " +
                "4b 03 ec b7 26 86 08 d3 f8 4c c6 44 30 81 ad 81 a3 82 7a 51 b8 40 a5 fd 77 c0 d4 32 fb fa 33 17 08 " +
                "49 14 c2 e8 a8 82 1e 4b a1 dc ba 44 96 1f f7 48 0e 6d b6 08 78 9c ab 62 91 41 63 60 ea 8c dc 26 b0 " +
                "d2 f0 87 7c 50 e8 9a 70 c1 bc f5 d6 dd 8b 18 2e 0a 9e 37 d3 f8 4d c7 81 88 81 a0 81 98 31 82 76 5f " +
                "b8 40 ae 31 bd 02 54 ee 7d 10 b8 0f c9 0e 74 ba 06 ba 76 11 87 df 31 38 a9 79 9d e5 82 8d 01 63 52 " +
                "4c 44 ba c7 d2 a9 b5 c4 1b e5 be 82 89 a1 72 36 1f 0b a9 04 10 c9 4f 57 9b f7 eb d2 8f 18 aa a1 cd " +
                "f8 4a c4 55 41 7e 2d 82 76 5f b8 40 ba 3d 21 67 72 cd c7 45 58 d2 54 56 24 a2 d6 2d cb cf d2 72 30 " +
                "57 30 c7 46 43 c7 a7 e8 19 af a6 cd d8 22 23 e2 b5 50 1e b6 d4 ea e5 db f2 1e 55 8c 76 8a ca ec 2c " +
                "1c a1 0e 74 c4 c8 7a 57 4b 53 f8 4a c4 55 41 7e 2d 82 76 5f b8 40 bd b4 9c 01 87 2d 91 bd 1e a9 90 " +
                "bd 2e df 16 c4 81 71 a6 06 7f 9a 6f 7f 48 bf b1 94 63 0b 5a e9 03 1b 5d c2 63 f5 9c 66 ad a4 44 cb " +
                "4e 6f 9d f6 2b 30 17 ce 61 2c ab 7b 53 da 08 d3 56 f7 8d 30 f8 4c c6 63 81 e7 58 81 af 82 76 5f b8 " +
                "40 c1 2b a9 1f 95 04 4d 78 ee d1 d3 a9 53 5e bd 64 71 52 44 18 13 5e eb 46 ad 5d 5c 6e cc 2f 51 68 " +
                "b4 ab 3a 06 2b b0 74 2a ea 65 ff ea 76 7f ab 8d cc 21 78 3c b2 9b f3 2e 2c d6 22 22 09 fa 71 fd f8 " +
                "4c c6 44 30 81 ad 81 a3 82 7a 51 b8 40 c2 e2 69 e6 4a a8 c9 be 2d 41 81 2a 48 af a2 34 6b d4 1a 1a " +
                "b2 e4 64 62 41 ae 3b 8d 0c cd 41 f2 d6 82 b1 5a 02 5f 75 9c 0d 95 5a 60 71 d4 e8 ea 7d 4d e3 97 d6 " +
                "e0 52 23 09 20 11 3b 6e b7 4c 09 f8 4a c4 4a 4f 17 77 82 76 5f b8 40 c3 03 b8 3f 6a 16 1f 99 67 36 " +
                "34 44 80 ae 9d 88 fd c1 d9 c6 75 bf ac a8 88 f7 0f 24 89 72 65 62 82 09 da 53 74 1e 03 c0 f6 59 21 " +
                "f6 8f 60 2d c9 f3 34 a3 c4 5b cb 92 af 85 44 a6 fb 11 9b d8 87 f8 4b c5 0c 81 fa 61 1a 82 76 5f b8 " +
                "40 c7 6e 7c 15 7b 77 35 51 11 53 d1 f9 50 81 a1 44 e0 88 a9 89 17 1f 3d 43 2c c5 d8 29 3e ce 9c fa " +
                "a4 83 c0 32 15 5d 7b 53 65 6a 6e 33 a3 d7 5c d0 62 4e 09 a2 f9 49 c1 56 09 3d ba a8 3f 11 11 f2 f8 " +
                "4b c5 52 0c 81 e3 54 82 76 5f b8 40 c7 d5 a3 69 1a 59 59 9d e3 33 48 9c bf 8a 47 a7 43 3e 92 c7 27 " +
                "06 e1 3d 94 ed 21 12 96 d3 5c 97 d8 35 7d 7e 07 b3 85 85 64 d7 26 8e d7 aa 09 7f 37 58 9c 27 77 0f " +
                "90 dd 0b 07 63 5b e3 f5 33 64 f8 4c c6 4e 09 81 92 81 b2 82 76 5f b8 40 c8 81 97 a8 2b 0a cf 0a 87 " +
                "24 94 d1 df ac 9d e8 46 da a7 de 08 b2 40 64 7a 96 ba 72 fb e0 8f d5 2b 55 c6 c9 45 14 a4 7e c5 1b " +
                "a4 9a 97 54 89 eb c9 38 3b 48 f5 e2 40 93 90 68 ce 58 36 ff 24 f1 f8 4b c5 81 b4 20 2b 08 82 76 5f " +
                "b8 40 c9 e0 39 d8 a8 b9 e4 35 be f2 f4 5f c7 cb 7e 78 87 16 e8 c7 af c1 ba cc 64 e1 24 6d 2a b5 06 " +
                "d3 60 73 79 2a e6 96 e4 1a d6 ba 0c 8a bd 2e c0 d5 45 b0 75 7f 94 a9 f3 53 82 80 e5 6d b5 f5 d8 ec " +
                "f8 4b c5 4e 68 81 a3 51 82 76 5f b8 40 ca 27 68 37 02 a8 e9 bf 32 01 65 6f f8 4a 60 d5 b1 dd 81 42 " +
                "73 99 3c f1 a0 25 b0 54 45 4e 40 d5 30 92 f4 85 18 ee 05 be ad 4f 18 02 1f 4f 54 0c 0b 7c 7d 26 eb " +
                "a5 0e a4 89 0b 9e 5e 49 a7 6c 5f f8 4a c4 55 41 7e 2d 82 76 5f b8 40 cb 72 be 9e 2e 5d 4a 1f 25 72 " +
                "96 c7 39 39 10 4e ce 80 31 32 15 26 5a f0 6b c7 ea f4 42 ab ff 4f 0b 48 fc fc 6f 43 f4 df 46 30 c7 " +
                "12 b5 e7 ef db 75 4a 86 e4 0c f2 02 16 6e b6 9e ea a6 ad 3a 2d f8 4a c4 36 48 1f 37 82 76 5f b8 40 " +
                "ce 73 66 0a 06 62 6c 1b 3f da 7b 18 ef 7b a3 ce 17 b6 bf 60 4f 95 41 d3 c6 c6 54 b7 ae 88 b2 39 40 " +
                "7f 65 9c 78 f4 19 02 5d 78 57 27 ed 01 7b 6a dd 21 95 2d 7e 12 00 73 73 e3 21 db c3 18 24 ba f8 4a " +
                "c4 55 41 7e 2d 82 76 5f b8 40 ce 73 f1 f1 f1 f1 6c 1b 3f da 7b 18 ef 7b a3 ce 17 b6 f1 f1 f1 f1 41 " +
                "d3 c6 c6 54 b7 ae 88 b2 39 40 7f f1 f1 f1 f1 19 02 5d 78 57 27 ed 01 7b 6a dd 21 f1 f1 f1 f1 00 00 " +
                "01 e3 21 db c3 18 24 ba f8 4c c6 81 bf 81 ea 39 37 82 76 5f b8 40 d2 30 30 60 35 99 b7 6f 64 0b 8f " +
                "7c 11 99 12 bb 04 66 e7 ee f3 38 cd 9d e5 67 d2 b6 df ba 81 72 8d b2 e9 8f 29 38 25 bb 00 a9 a6 ac " +
                "93 66 83 fc 82 c8 bc 38 7a df 3a 4a 5f e1 cc ca dd 1a 74 59 f8 4c c6 6b 81 aa 39 81 f7 82 76 5f b8 " +
                "40 e0 2b 18 fb a6 b8 87 fb 92 58 46 9c 3a f8 e4 45 cc 9a e2 b5 38 6c ac 5f 60 c4 17 0f 82 20 86 22 " +
                "4e 38 76 55 5c 74 5a 7e c8 ac 18 1c 7f 97 01 77 6d 94 a7 79 60 4e a1 26 51 de 5f 4a 74 8d 29 e1 f8 " +
                "4c c6 40 81 e7 0a 81 d0 82 76 5f b8 40 e3 11 15 a7 6f a7 fb 2e fd 3c fa f4 6a d0 0b 05 fc 34 98 e1 " +
                "ba f1 78 5d ff e6 ca 69 91 3d 25 65 31 d1 80 56 42 35 fd 3d 3c 10 40 9c d1 1f c2 59 cf 7c fd a9 b6 " +
                "bb 25 33 40 41 2d 82 87 8f 3b d3 f8 4b c5 41 5e 31 81 97 82 76 5f b8 40 e5 e8 d8 c2 d7 62 d2 1c a1 " +
                "e9 bc ee 8a dc 53 60 0f 2d 89 40 97 54 26 66 d6 b5 f4 1b 23 58 4b 07 f6 09 01 ab 40 9d df 91 e0 cd " +
                "25 62 da ff f2 cb 0f 22 1e b9 f1 15 6f 78 1a 5d 99 31 a0 2a 2e 07 f8 4a c4 55 41 7e 2d 82 76 5f b8 " +
                "40 ea 99 2c 13 68 7c 20 e7 90 a9 ff a6 df 8b 1a 16 86 88 e2 a8 87 36 5d 7a 50 21 86 fa 0d 62 20 e8 " +
                "3e 11 3a 1f e7 7d c0 68 9d 55 ba 2e 8a 83 aa 8e 20 42 18 f4 d8 e7 32 82 5b d7 80 cf 94 ed 5c c3 f8 " +
                "4b c5 56 7c 52 81 fe 82 76 5f b8 40 f6 15 5f 1a 60 14 3b 7d 9d 5d 1a 44 0d 7d 52 fe 68 09 f6 9e 0c " +
                "6f 1e 00 24 45 7e 0d 71 dd 88 ad e3 b1 3a aa 94 0c 89 ac 06 10 95 2b 48 bd 83 2c 42 e3 43 a1 3e 61 " +
                "ff db 06 01 0c ff c3 45 e0 53 f8 4c c6 63 81 e7 58 81 af 82 76 5f b8 40 fa 56 85 61 b7 d5 28 8d f7 " +
                "a5 06 c9 bc 1c 95 12 ab 39 6e 68 c4 6f 0e 62 c2 1d c1 aa 58 4b 84 4a 8a 7e 94 4f 69 71 30 36 65 fd " +
                "37 b1 38 d9 a5 f6 37 e6 72 ed b9 89 69 66 4c 4e 7f d1 c4 12 6d ef";
        byte[] payload = Hex.decode(peers);

        RLPList rlpList = decode2(payload);

        RLPList.recursivePrint(rlpList);
        // TODO: add some asserts in place of just printing the rlpList
    }

    @Test /* very very very long blocks msg */
    public void test14() {

        String blocksMsg = "f91c1c13f90150f8c4a07df3d35d4df0a56fcf1d6344d5315cb56b9bf83bb96ad17c7b96a9cd14133c5da01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a064afb6284fa35f26d7b2c5a26afaa5483072fbcb575221b34ce002a991b7a223a04a8abe6d802797dc80b497584f898c2d4fd561cc185828cfa1b92f6f38ee348e833fbfe484533f201c80a000000000000000000000000000000000000000000000000000cfccb5cfd4667cf887f8850380942d0aceee7e5ab874e22ccf8d1a649f59106d74e88609184e72a000822710a047617600000000000000000000000000000000000000000000000000000000001ca08691ab40e258de3c4f55c868c0c34e780e747158a1d96ca50186dfd3305abd78a042269c981d048a7b791aafc8f4e644232740c1a1cceb5b6d05568827a64c0664c0f8c8f8c4a0637c8a6cdb907fac6f752334ab79065bcc4e46cd4f4358dbc2a653544a20eb31a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a022a36c1a1e807e6afc22e6bb53a31111f56e7ee7dbb2ee571cefb152b514db4da01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fcfd784533f1cf980a0000000000000000000000000000000000000000000000000e153d743fa040b18c0c0f8c8f8c4a07b2536237cbf114a043b0f9b27c76f84ac160ea5b87b53e42c7e76148964d450a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a07a3be0ee10ece4b03097bf74aabac628aa0fae617377d30ab1b97376ee31f41aa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fbfe884533f1ce880a0000000000000000000000000000000000000000000000000f3deea84969b6e95c0c0f8c8f8c4a0d2ae3f5dd931926de428d99611980e7cdd7c1b838273e43fcad1b94da987cfb8a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a00b5b11fcf4ee12c6812f9d01cf0dff07c72cd7e02e48b35682f67c595407be14a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833faffd84533f1ce280a00000000000000000000000000000000000000000000000005fcbc97b2eb8ffb3c0c0f8c8f8c4a094d615d3cb4b306d20985028c78f0c8413d509a75e8bb84eda95f734debad2a0a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a04b8fd1b98cf5758bd303ff86393eb6d944c1058124bddce5d4e04b5395254d5ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fbfec84533f1c7680a000000000000000000000000000000000000000000000000079fe3710116b32aac0c0f8c8f8c4a09424a07a0e4c05bb872906c40844a75b76f6517467b79c12fa9cc6d79ae09934a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a02dbe9ff9cbbc4c5a6ff26971f75b405891141f4e9bce3c2dc4200a305138e584a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fcfdf84533f1c3b80a0000000000000000000000000000000000000000000000000e0a6f8cf1d56031bc0c0f8c8f8c4a009aabea60cf7eaa9df4afdf4e1b5f3e684dab34fc9a9180a050085a4131ceedfa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0436da067f9683029e717edf92da46c3443e8c342974f47a563302a0678efe702a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fdfd684533f1bfc80a00000000000000000000000000000000000000000000000005bc88c041662ffdac0c0f8c8f8c4a0f8b104093483b7c0182e1bba2ce3340d14469d3a3ee7646821223a676c680ac1a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0d482e71cde61190a33ca5aeb88b6b06276984e5a14253a98df232e8767167221a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fefd184533f1bce80a00000000000000000000000000000000000000000000000004aeb31823f6a1950c0c0f8c8f8c4a0dd1f0aba02c2bb3b5a2b6cb1cc907ea70912bd46dc7a78577f2cae6cdbcbe5f3a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a058ab6df33d7cbeb6a735a7e4ccf4f28143e6a1742e45dda8f8cf48af43cb66c0a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fffd084533f1b9f80a0000000000000000000000000000000000000000000000000577042b0858b510bc0c0f8c8f8c4a0a287bb7da30f04344976abe569bd719f69c1cbea65533e5311ca5862e6eaa504a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a07e0537009c23cb1152caf84a52272431f74b6140866b15805622b7bcb607cd42a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934783400fd384533f1b6180a000000000000000000000000000000000000000000000000083d31378a0993e1ac0c0f8c8f8c4a063483cff8fbd489e6ce273326d8dc1d54a31c67f936ca84bf500e5419d3e9805a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a07737d08564819d51f8f834a6ee4278c23a0c2f29a3f485b21002c1f24f04d8e4a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fffd484533f1b5780a0000000000000000000000000000000000000000000000000bb586fe6de016e14c0c0f8c8f8c4a0975c8ed0c9197b7c018e02e1c95f315acf82b25e4a812140f4515e8bc827650ca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0ad51229abead59e93c80db5ba160f0939bc49dcee65d1c081f6eb040ff1f571fa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fefd984533f1b4e80a0000000000000000000000000000000000000000000000000548f02c6ceb26fd4c0c0f8c8f8c4a05844082e41f7c1f34485c7902afa0aa0979a6a849100fc553cd946f4a663929ca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a01bc726443437c4c062be18d052278e4ef735b8fe84387c8a4fc85fb70d5078e0a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fffd884533f1b1080a0000000000000000000000000000000000000000000000000cc1e528f54f22bdac0c0f8c8f8c4a0ba06ba81c93faaf98ea2d83cbdc0788958d938b29a9eb2a92ffbd4a628b3d52ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a05053bfe1c0f1f0dd341c6df35e5a659989be041e8521027cc90f7605eb15fbb9a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fefdd84533f1b0380a0000000000000000000000000000000000000000000000000bcf9df2fec615ecac0c0f8c8f8c4a083732d997db15109e90464c24b7c959a78881d827c55a0d668a66a2736be5d87a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a054f4012cba33a2b80b0dca9dd52f56b2c588133bd71700863f8cb95127176634a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fffdc84533f1a4680a00000000000000000000000000000000000000000000000006920a1dc9d915d0ec0c0f8c8f8c4a052e2fba761c2d0643170ef041c017391e781190fe715ae87cdae8eee1d45d95ba01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0ee2c82f77d7afd1f8dbe4f791df8477496c23e5504b9d66814172077f65f81f2a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fefe184533f1a3880a0000000000000000000000000000000000000000000000000ae86da9012398fc4c0c0f8c8f8c4a055703ba09544f386966b6d63bfc31033b761a4d1a6bb86b0cf49b4bb0526744ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a01684c03a675b53786f0077d1651c3d169a009b13a6ee2b5047be6dbbe6d957ffa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fdfea84533f1a2f80a00000000000000000000000000000000000000000000000003247320d0eb639dfc0c0f8c8f8c4a05109a79b33d81f4ee4814b550fb0002f03368d67570f6d4e6105fce2874d8b72a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0ae72e8c60a3dcfd53deecdb2790d18f0cc710f77cf2c1ed76e7da829bde619dca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fcff784533f1a1d80a000000000000000000000000000000000000000000000000040e0bc9bc9bcf295c0c0f8c8f8c4a03961e4bbba5c95fad3db0cffa3a16b9106f9ea3e8957993eab576b683c22f416a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0e9c6cf457bbe64d6bda67852a276cdbadb4f384a36d496e81801a496cfd9b7b5a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fdfee84533f19df80a0000000000000000000000000000000000000000000000000dbb3fd6c816776d8c0c0f8c8f8c4a06b8265a357cb3ad744e19f04eb15377f660c10c900cc352d24f9b09073a363d6a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a07ba07e1bc6a20ffa44ae6080d30982b9faa148faf6b1ec15e32d89ac853ac291a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fefe984533f198d80a00000000000000000000000000000000000000000000000005171325b6a2f17f1c0c0f8c8f8c4a0dcdc0347bb87ce72d49ac2e4e11f89069509b129a2536bf3d57c6bca30894032a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0ca24447aa0cedb4b561c7810461eef19b16a827c27352e5e01f914e9d7c78247a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fffe884533f194680a0000000000000000000000000000000000000000000000000da4714cfed9d8bbcc0c0f8c8f8c4a047f2dd6c15ea4082b3e11e5cf6b925b27e51d9de68051a093e52ef465cffbb8ca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a05a7206edddf50fcfeeaa97348a7112fc6edd0b5eacb44cf43d6a6c6b6609b459a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fefed84533f193e80a0000000000000000000000000000000000000000000000000ffafba4bf8dc944ec0c0f8c8f8c4a04d5ad6d860772145872f6660ecefcb0b0b2056e0aa3509a48bf4c175459e5121a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a00f4659d09bb2ced56e7fd9c4d3d90daca8b4f471307b7c4385fd34a41016b0b2a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fdff684533f192580a000000000000000000000000000000000000000000000000090620e5e59a39fe5c0c0f8c8f8c4a0c1725c58d1bf023af468e0088db3cf642ae097cf2c58c2ece2fc746980acc7e6a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0be19a182ea1584050deb0a79abdc11be896ce8d00a282bcfaf9ffcd65fd64d6aa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833feff184533f189080a000000000000000000000000000000000000000000000000076f17f4199bccd12c0c0f8c8f8c4a0bd521a20a18ca6ca7115065065a1aa20067ee580fb11e2963d1e3a681e8302afa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a011be45633350e39475a1a07712ba72de4602d9eebf639ccd5422a389095ccaf1a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fdffa84533f187b80a00000000000000000000000000000000000000000000000000c71b81c4a4cb82cc0c0f8c8f8c4a07c6d2d56e9c87f1553e4d06705af61a7c19a6046d2c39f8ed1417988783d3b1da01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a012f5f0668063509e33a45a64eb6a072b2d84aa19f430f49f159be5008a786b2ea01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fd00684533f186080a0000000000000000000000000000000000000000000000000b3f962892cfec9e6c0c0f8c8f8c4a07154f0f8ecc7f791d22eec06ec86d87a44b2704f015b3d2cff3571a3d01ae0f6a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a079536abf8e163cf8aa97f0d52866d04363902d591fd7c36aa35fc983d45fefd6a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fdffd84533f182f80a0000000000000000000000000000000000000000000000000736716e42499890fc0c0f8c8f8c4a0bf2fb1ee988ac4e17eae221a24176649774333fab25b6fc96c6081527fb6f121a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a041578daae7bcccd4976340aeb19e4132d2fe4193a0d92f87744c82bfe113502fa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fd00984533f182b80a00000000000000000000000000000000000000000000000001c62fa76645942c6c0c0f8c8f8c4a07f84873e2679d40458b9dda9900478a78871044e08f6b47dad659b9b60ff8d48a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0597d3f4160770c0492333f90bad739dc05117d0e478a91f09573742e432904e8a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fe00184533f17f680a0000000000000000000000000000000000000000000000000e24d8b1140fb34d5c0c0f8c8f8c4a0fd77bd13a8cde1766537febe751a27a2a31310a04638a1afcd5e8ad3c5485453a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0473b2b248d91010ba9aec2696ffc93c11c415ed132832be0fd0578f184862e13a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833feffc84533f17ca80a0000000000000000000000000000000000000000000000000fb5b65bac3f0d947c0c0f8c8f8c4a0518916dfb79c390bd7bff75712174512c2f96bec42a3f573355507ad1588ce0ca01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a08599d2ec9e95ec62f41a4975b655d8445d6767035f94eb235ed5ebea976fb9eaa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347833fe00484533f17b880a0000000000000000000000000000000000000000000000000bc27f4b8a201476bc0c0f90319f8c4a0ab6b9a5613970faa771b12d449b2e9bb925ab7a369f0a4b86b286e9d540099cfa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347943854aaf203ba5f8d49b1ec221329c7aebcf050d3a0990dc3b5acbee04124361d958fe51acb582593613fc290683940a0769549d3eda09bfe4817d274ea3eb8672e9fe848c3885b53bbbd1d7c26e6039f90fb96b942b0833ff00084533f16b780a000000000000000000000000000000000000000000000000077377adff6c227dbf9024ff89d80809400000000000000000000000000000000000000008609184e72a000822710b3606956330c0d630000003359366000530a0d630000003359602060005301356000533557604060005301600054630000000c5884336069571ca07f6eb94576346488c6253197bde6a7e59ddc36f2773672c849402aa9c402c3c4a06d254e662bf7450dd8d835160cbb053463fed0b53f2cdd7f3ea8731919c8e8ccf9010501809400000000000000000000000000000000000000008609184e72a000822710b85336630000002e59606956330c0d63000000155933ff33560d63000000275960003356576000335700630000005358600035560d630000003a590033560d63000000485960003356573360003557600035335700b84a7f4e616d65526567000000000000000000000000000000000000000000000000003057307f4e616d655265670000000000000000000000000000000000000000000000000057336069571ba04af15a0ec494aeac5b243c8a2690833faa74c0f73db1f439d521c49c381513e9a05802e64939be5a1f9d4d614038fbd5479538c48795614ef9c551477ecbdb49d2f8a6028094ccdeac59d35627b7de09332e819d5159e7bb72508609184e72a000822710b84000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002d0aceee7e5ab874e22ccf8d1a649f59106d74e81ba0d05887574456c6de8f7a0d172342c2cbdd4cf7afe15d9dbb8b75b748ba6791c9a01e87172a861f6c37b5a9e3a5d0d7393152a7fbe41530e5bb8ac8f35433e5931bc0";
        byte[] payload = Hex.decode(blocksMsg);

        RLPList rlpList = decode2(payload);

        RLPList.recursivePrint(rlpList);
        // TODO: add some asserts in place of just printing the rlpList
    }

    @Test /* hello msg */
    public void test15() {

        String helloMsg = "f8 91 80 0b 80 b8 46 45 74 68 65 72 65 75 6d 28 2b 2b 29 2f 5a 65 72 6f 47 6f 78 2e 70 72 " +
                "69 63 6b 6c 79 5f 6d 6f 72 73 65 2f 76 30 2e 34 2e 32 2f 52 65 6c 65 61 73 65 2d 57 69 6e 33 32 2f " +
                "57 69 6e 64 6f 77 73 2f 56 53 32 30 31 33 07 82 76 5f b8 40 ea 99 2c 13 68 7c 20 e7 90 a9 ff a6 df " +
                "8b 1a 16 86 88 e2 a8 87 36 5d 7a 50 21 86 fa 0d 62 20 e8 3e 11 3a 1f e7 7d c0 68 9d 55 ba 2e 8a 83 " +
                "aa 8e 20 42 18 f4 d8 e7 32 82 5b d7 80 cf 94 ed 5c c3";
        byte[] payload = Hex.decode(helloMsg);

        RLPList rlpList = decode2(payload);

        RLPList.recursivePrint(rlpList);
        // TODO: add some asserts in place of just printing the rlpList
    }

    /************************************
     * Test data from: https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-RLP
     *
     * Using assertEquals(String, String) instead of assertArrayEquals to see the actual content when the test fails.
     */
    @Test(expected = RuntimeException.class)
    public void testEncodeNull() {
        encode(null);
    }

    @Test
    public void testEncodeEmptyString() {
        String test = "";
        String expected = "80";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        String decodeResult = (String) decode(encoderesult, 0).getDecoded();
        assertEquals(test, decodeResult);
    }

    @Test
    public void testEncodeShortString() {
        String test = "dog";
        String expected = "83646f67";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        byte[] decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        assertEquals(test, bytesToAscii(decodeResult));
    }

    @Test
    public void testEncodeSingleCharacter() {
        String test = "d";
        String expected = "64";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        byte[] decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        assertEquals(test, bytesToAscii(decodeResult));
    }

    @Test
    public void testEncodeLongString() {
        String test = "Lorem ipsum dolor sit amet, consectetur adipisicing elit"; // length = 56
        String expected = "b8384c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e7365637465747572206164697069736963696e6720656c6974";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        byte[] decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        assertEquals(test, bytesToAscii(decodeResult));
    }

    @Test
    public void testEncodeZero() {
        Integer test = 0;
        String expected = "80";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        String decodeResult = (String) decode(encoderesult, 0).getDecoded();
        assertEquals("", decodeResult);
    }

    @Test
    public void testEncodeSmallInteger() {
        Integer test = 15;
        String expected = "0f";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        byte[] decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        int result = byteArrayToInt(decodeResult);
        assertEquals(test, Integer.valueOf(result));
    }

    @Test
    public void testEncodeMediumInteger() {
        Integer test = 1000;
        String expected = "8203e8";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        byte[] decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        int result = byteArrayToInt(decodeResult);
        assertEquals(test, Integer.valueOf(result));

        test = 1024;
        expected = "820400";
        encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        result = byteArrayToInt(decodeResult);
        assertEquals(test, Integer.valueOf(result));
    }

    @Test
    public void testEncodeBigInteger() {
        BigInteger test = new BigInteger("100102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", 16);
        String expected = "a0100102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        byte[] decodeResult = (byte[]) decode(encoderesult, 0).getDecoded();
        assertEquals(test, new BigInteger(1, decodeResult));
    }

    @Test
    public void TestEncodeEmptyList() {
        Object[] test = new Object[0];
        String expected = "c0";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        Object[] decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertTrue(decodeResult.length == 0);
    }

    @Test
    public void testEncodeShortStringList() {
        String[] test = new String[]{"cat", "dog"};
        String expected = "c88363617483646f67";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        Object[] decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertEquals("cat", bytesToAscii((byte[]) decodeResult[0]));
        assertEquals("dog", bytesToAscii((byte[]) decodeResult[1]));

        test = new String[]{"dog", "god", "cat"};
        expected = "cc83646f6783676f6483636174";
        encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertEquals("dog", bytesToAscii((byte[]) decodeResult[0]));
        assertEquals("god", bytesToAscii((byte[]) decodeResult[1]));
        assertEquals("cat", bytesToAscii((byte[]) decodeResult[2]));
    }

    @Test
    public void testEncodeLongStringList() {
        String element1 = "cat";
        String element2 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit";
        String[] test = new String[]{element1, element2};
        String expected = "f83e83636174b8384c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e7365637465747572206164697069736963696e6720656c6974";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        Object[] decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertEquals(element1, bytesToAscii((byte[]) decodeResult[0]));
        assertEquals(element2, bytesToAscii((byte[]) decodeResult[1]));
    }

    //multilist:
    //in: [ 1, ["cat"], "dog", [ 2 ] ],
    //out: "cc01c48363617483646f67c102"
    //in: [ [ ["cat"], ["dog"] ], [ [1] [2] ], [] ],
    //out: "cdc88363617483646f67c20102c0"
    @Test
    public void testEncodeMultiList() {
        Object[] test = new Object[]{1, new Object[]{"cat"}, "dog", new Object[]{2}};
        String expected = "cc01c48363617483646f67c102";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        Object[] decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertEquals(1, byteArrayToInt((byte[]) decodeResult[0]));
        assertEquals("cat", bytesToAscii(((byte[]) ((Object[]) decodeResult[1])[0])));
        assertEquals("dog", bytesToAscii((byte[]) decodeResult[2]));
        assertEquals(2, byteArrayToInt(((byte[]) ((Object[]) decodeResult[3])[0])));

        test = new Object[]{new Object[]{"cat", "dog"}, new Object[]{1, 2}, new Object[]{}};
        expected = "cdc88363617483646f67c20102c0";
        encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertEquals("cat", bytesToAscii(((byte[]) ((Object[]) decodeResult[0])[0])));
        assertEquals("dog", bytesToAscii(((byte[]) ((Object[]) decodeResult[0])[1])));
        assertEquals(1, byteArrayToInt(((byte[]) ((Object[]) decodeResult[1])[0])));
        assertEquals(2, byteArrayToInt(((byte[]) ((Object[]) decodeResult[1])[1])));
        assertTrue((((Object[]) decodeResult[2]).length == 0));
    }

    @Test
    public void testEncodeEmptyListOfList() {
        // list = [ [ [], [] ], [] ],
        Object[] test = new Object[]{new Object[]{new Object[]{}, new Object[]{}}, new Object[]{}};
        String expected = "c4c2c0c0c0";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        Object[] decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertTrue(decodeResult.length == 2);
        assertTrue(((Object[]) (decodeResult[0])).length == 2);
        assertTrue(((Object[]) (decodeResult[1])).length == 0);
        assertTrue(((Object[]) ((Object[]) (decodeResult[0]))[0]).length == 0);
        assertTrue(((Object[]) ((Object[]) (decodeResult[0]))[1]).length == 0);
    }

    //The set theoretical representation of two
    @Test
    public void testEncodeRepOfTwoListOfList() {
        //list: [ [], [[]], [ [], [[]] ] ]
        Object[] test = new Object[]{new Object[]{}, new Object[]{new Object[]{}}, new Object[]{new Object[]{}, new Object[]{new Object[]{}}}};
        String expected = "c7c0c1c0c3c0c1c0";
        byte[] encoderesult = encode(test);
        assertEquals(expected, Hex.toHexString(encoderesult));

        Object[] decodeResult = (Object[]) decode(encoderesult, 0).getDecoded();
        assertTrue(decodeResult.length == 3);
        assertTrue(((Object[]) (decodeResult[0])).length == 0);
        assertTrue(((Object[]) (decodeResult[1])).length == 1);
        assertTrue(((Object[]) (decodeResult[2])).length == 2);
        assertTrue(((Object[]) ((Object[]) (decodeResult[1]))[0]).length == 0);
        assertTrue(((Object[]) ((Object[]) (decodeResult[2]))[0]).length == 0);
        assertTrue(((Object[]) ((Object[]) (decodeResult[2]))[1]).length == 1);
        assertTrue(((Object[]) ((Object[]) ((Object[]) (decodeResult[2]))[1])[0]).length == 0);
    }

    @Test
    public void testRlpEncode() {

        assertEquals(result01, Hex.toHexString(encode(test01)));
        assertEquals(result02, Hex.toHexString(encode(test02)));
        assertEquals(result03, Hex.toHexString(encode(test03)));
        assertEquals(result04, Hex.toHexString(encode(test04)));
        assertEquals(result05, Hex.toHexString(encode(test05)));
        assertEquals(result06, Hex.toHexString(encode(test06)));
        assertEquals(result07, Hex.toHexString(encode(test07)));
        assertEquals(result08, Hex.toHexString(encode(test08)));
        assertEquals(result09, Hex.toHexString(encode(test09)));
        assertEquals(result10, Hex.toHexString(encode(test10)));
        assertEquals(result11, Hex.toHexString(encode(test11)));
        assertEquals(result12, Hex.toHexString(encode(test12)));
        assertEquals(result13, Hex.toHexString(encode(test13)));
        assertEquals(result14, Hex.toHexString(encode(test14)));
        assertEquals(result15, Hex.toHexString(encode(test15)));
        assertEquals(result16, Hex.toHexString(encode(test16)));
    }

    @Test
    public void testRlpDecode() {
        int pos = 0;
        String emptyString;
        byte[] decodedData;
        Object[] decodedList;

        emptyString = (String) decode(Hex.decode(result01), pos).getDecoded();
        assertEquals("", emptyString);

        emptyString = (String) decode(Hex.decode(result02), pos).getDecoded();
        assertEquals(test02, emptyString);

        decodedData = (byte[]) decode(Hex.decode(result03), pos).getDecoded();
        assertEquals(test03, bytesToAscii(decodedData));

        decodedData = (byte[]) decode(Hex.decode(result04), pos).getDecoded();
        assertEquals(test04, bytesToAscii(decodedData));

        decodedData = (byte[]) decode(Hex.decode(result05), pos).getDecoded();
        assertEquals(test05, bytesToAscii(decodedData));

        decodedList = (Object[]) decode(Hex.decode(result06), pos).getDecoded();
        assertEquals(test06[0], bytesToAscii((byte[]) decodedList[0]));
        assertEquals(test06[1], bytesToAscii((byte[]) decodedList[1]));

        decodedList = (Object[]) decode(Hex.decode(result07), pos).getDecoded();
        assertEquals(test07[0], bytesToAscii((byte[]) decodedList[0]));
        assertEquals(test07[1], bytesToAscii((byte[]) decodedList[1]));
        assertEquals(test07[2], bytesToAscii((byte[]) decodedList[2]));

        // 1
        decodedData = (byte[]) decode(Hex.decode(result08), pos).getDecoded();
        assertEquals(test08, byteArrayToInt(decodedData));

        // 10
        decodedData = (byte[]) decode(Hex.decode(result09), pos).getDecoded();
        assertEquals(test09, byteArrayToInt(decodedData));

        // 100
        decodedData = (byte[]) decode(Hex.decode(result10), pos).getDecoded();
        assertEquals(test10, byteArrayToInt(decodedData));

        // 1000
        decodedData = (byte[]) decode(Hex.decode(result11), pos).getDecoded();
        assertEquals(test11, byteArrayToInt(decodedData));

        decodedData = (byte[]) decode(Hex.decode(result12), pos).getDecoded();
        assertTrue(test12.compareTo(new BigInteger(1, decodedData)) == 0);

        decodedData = (byte[]) decode(Hex.decode(result13), pos).getDecoded();
        assertTrue(test13.compareTo(new BigInteger(1, decodedData)) == 0);

        // Need to test with different expected value, because decoding doesn't recognize types
        Object testObject1 = decode(Hex.decode(result14), pos).getDecoded();
        assertTrue(DeepEquals.deepEquals(expected14, testObject1));

        Object testObject2 = decode(Hex.decode(result15), pos).getDecoded();
        assertTrue(DeepEquals.deepEquals(test15, testObject2));

        // Need to test with different expected value, because decoding doesn't recognize types
        Object testObject3 = decode(Hex.decode(result16), pos).getDecoded();
        assertTrue(DeepEquals.deepEquals(expected16, testObject3));
    }

    @Test
    public void testEncodeLength() {

        // length < 56
        int length = 1;
        int offset = 128;
        byte[] encodedLength = encodeLength(length, offset);
        String expected = "81";
        assertEquals(expected, Hex.toHexString(encodedLength));

        // 56 > length < 2^64
        length = 56;
        offset = 192;
        encodedLength = encodeLength(length, offset);
        expected = "f838";
        assertEquals(expected, Hex.toHexString(encodedLength));
    }

    @Test
    @Ignore
    public void unsupportedLength() {

        int length = 56;
        int offset = 192;
        byte[] encodedLength;

        // length > 2^64
        // TODO: Fix this test - when casting double to int, information gets lost since 'int' is max (2^31)-1
        double maxLength = Math.pow(256, 8);

        try {
            encodedLength = encodeLength((int) maxLength, offset);
            System.out.println("length: " + length + ", offset: " + offset + ", encoded: " + Arrays.toString(encodedLength));

            fail("Expecting RuntimeException: 'Input too long'");
        } catch (RuntimeException e) {
            // Success!
        }

    }

    // Code from: http://stackoverflow.com/a/4785776/459349
    private String bytesToAscii(byte[] b) {
        String hex = Hex.toHexString(b);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    @Test
    public void performanceDecode() throws IOException {
        boolean performanceEnabled = false;

        if (performanceEnabled) {
            String blockRaw = "f8cbf8c7a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a02f4399b08efe68945c1cf90ffe85bbe3ce978959da753f9e649f034015b8817da00000000000000000000000000000000000000000000000000000000000000000834000008080830f4240808080a004994f67dc55b09e814ab7ffc8df3686b4afb2bb53e60eae97ef043fe03fb829c0c0";
            byte[] payload = Hex.decode(blockRaw);

            final int ITERATIONS = 10000000;
            RLPList list = null;
            DecodeResult result = null;
            System.out.println("Starting " + ITERATIONS + " decoding iterations...");

            long start1 = System.currentTimeMillis();
            for (int i = 0; i < ITERATIONS; i++) {
                result = decode(payload, 0);
            }
            long end1 = System.currentTimeMillis();

            long start2 = System.currentTimeMillis();
            for (int i = 0; i < ITERATIONS; i++) {
                list = decode2(payload);
            }
            long end2 = System.currentTimeMillis();

            System.out.println("Result RLP.decode()\t: " + (end1 - start1) + "ms and\t " + determineSize(result) + " bytes for each resulting object list");
            System.out.println("Result RLP.decode2()\t: " + (end2 - start2) + "ms and\t " + determineSize(list) + " bytes for each resulting object list");
        } else {
            System.out.println("Performance test for RLP.decode() disabled");
        }
    }

    private int determineSize(Serializable ser) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ser);
        oos.close();
        return baos.size();
    }


    @Test // found this with a bug - nice to keep
    public void encodeEdgeShortList() {

        String expectedOutput = "f7c0c0b4600160003556601359506301000000600035040f6018590060005660805460016080530160005760003560805760203560003557";

        byte[] rlpKeysList = Hex.decode("c0");
        byte[] rlpValuesList = Hex.decode("c0");
        byte[] rlpCode = Hex.decode("b4600160003556601359506301000000600035040f6018590060005660805460016080530160005760003560805760203560003557");
        byte[] output = encodeList(rlpKeysList, rlpValuesList, rlpCode);

        assertEquals(expectedOutput, Hex.toHexString(output));
    }


    @Test
    public void encodeBigIntegerEdge_1() {

        BigInteger integer = new BigInteger("80", 10);
        byte[] encodedData = encodeBigInteger(integer);
        System.out.println(Hex.toHexString(encodedData));
    }

    @Test
    public void testEncodeListHeader(){

        byte[] header = encodeListHeader(10);
        String expected_1 = "ca";
        assertEquals(expected_1, Hex.toHexString(header));

        header = encodeListHeader(1000);
        String expected_2 = "f903e8";
        assertEquals(expected_2, Hex.toHexString(header));

        header = encodeListHeader(1000000000);
        String expected_3 = "fb3b9aca00";
        assertEquals(expected_3, Hex.toHexString(header));
    }


    @Test
    public void testEncodeSet_1(){

        Set<ByteArrayWrapper> data = new HashSet<>();

        ByteArrayWrapper element1 =
                new ByteArrayWrapper(Hex.decode("1111111111111111111111111111111111111111111111111111111111111111"));

        ByteArrayWrapper element2 =
                new ByteArrayWrapper(Hex.decode("2222222222222222222222222222222222222222222222222222222222222222"));

        data.add(element1);
        data.add(element2);

        byte[] setEncoded = encodeSet(data);

        RLPList list = (RLPList) decode2(setEncoded).get(0);

        byte[] element1_ = list.get(0).getRLPData();
        byte[] element2_ = list.get(1).getRLPData();

        assertTrue(data.contains(wrap(element1_)));
        assertTrue(data.contains(wrap(element2_)));
    }

    @Test
    public void testEncodeSet_2(){

        Set<ByteArrayWrapper> data = new HashSet<>();
        byte[] setEncoded = encodeSet(data);
        assertEquals("c0", Hex.toHexString(setEncoded));
    }

    @Test
    public void testEncodeInt_7f(){
        String result =  Hex.toHexString(encodeInt(0x7f));
        String expected = "7f";
        assertEquals(expected, result);
    }

    @Test
    public void testEncodeInt_80(){
        String result =  Hex.toHexString(encodeInt(0x80));
        String expected = "8180";
        assertEquals(expected, result);
    }


    @Test
    public void testEncode_ED(){
        String result =  Hex.toHexString(encode(0xED));
        String expected = "81ed";
        assertEquals(expected, result);
    }


    @Test // capabilities: (eth:60, bzz:0, shh:2)
    public void testEncodeHelloMessageCap0(){

        List<Capability> capabilities = new ArrayList<>();
        capabilities.add(new Capability("eth", (byte) 0x60));
        capabilities.add(new Capability("shh", (byte) 0x02));
        capabilities.add(new Capability("bzz", (byte) 0x00));

        HelloMessage helloMessage = new HelloMessage((byte)4,
                "Geth/v0.9.29-4182e20e/windows/go1.4.2",
                capabilities , 30303,
                "a52205ce10b39be86507e28f6c3dc08ab4c3e8250e062ec47c6b7fa13cf4a4312d68d6c340315ef953ada7e19d69123a1b902ea84ec00aa5386e5d550e6c550e");

        byte[] rlp = helloMessage.getEncoded();

        HelloMessage helloMessage_ = new HelloMessage(rlp);

        String eth    = helloMessage_.getCapabilities().get(0).getName();
        byte   eth_60 = helloMessage_.getCapabilities().get(0).getVersion();

        assertEquals("eth", eth);
        assertEquals(0x60, eth_60);

        String shh    = helloMessage_.getCapabilities().get(1).getName();
        byte   shh_02 = helloMessage_.getCapabilities().get(1).getVersion();

        assertEquals("shh", shh);
        assertEquals(0x02, shh_02);

        String bzz    = helloMessage_.getCapabilities().get(2).getName();
        byte   bzz_00 = helloMessage_.getCapabilities().get(2).getVersion();

        assertEquals("bzz", bzz);
        assertEquals(0x00, bzz_00);
    }

    @Test
    public void partialDataParseTest() {
        String hex = "000080c180000000000000000000000042699b1104e93abf0008be55f912c2ff";
        RLPList el = (RLPList) decode2OneItem(Hex.decode(hex), 3);
        assertEquals(1, el.size());
        assertEquals(0, Util.rlpDecodeInt(el.get(0)));
    }

    @Test
    public void shortStringRightBoundTest(){
        String testString = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"; //String of length 55
        byte[] rlpEncoded = encode(testString);
        String res = new String((byte[])decode(rlpEncoded, 0).getDecoded());
        assertEquals(testString, res); //Fails
    }

    @Test
    public void encodeDecodeBigInteger() {
        BigInteger expected = new BigInteger("9650128800487972697726795438087510101805200020100629942070155319087371611597658887860952245483247188023303607186148645071838189546969115967896446355306572");
        byte[] encoded = encodeBigInteger(expected);
        BigInteger decoded = decodeBigInteger(encoded, 0);
        assertNotNull(decoded);
        assertEquals(expected, decoded);
    }
}
