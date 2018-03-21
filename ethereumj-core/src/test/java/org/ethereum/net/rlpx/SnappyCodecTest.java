/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
 *
 *
 */

package org.ethereum.net.rlpx;

import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.ethereum.net.server.Channel;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.ethereum.net.message.ReasonCode.BAD_PROTOCOL;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EIP-706 provides snappy samples. FrameCodec should be
 * able to decode these samples.
 */
public class SnappyCodecTest {
    @Test
    public void testDecodeGoGeneratedSnappy() throws Exception {
        Resource golangGeneratedSnappy = new ClassPathResource("/rlp/block.go.snappy");
        List<Object> result = snappyDecode(golangGeneratedSnappy);
        assertEquals(1, result.size());
    }

    @Test
    public void testDecodePythonGeneratedSnappy() throws Exception {
        Resource pythonGeneratedSnappy = new ClassPathResource("/rlp/block.py.snappy");
        List<Object> result = snappyDecode(pythonGeneratedSnappy);
        assertEquals(1, result.size());
    }

    @Test
    public void testFramedDecodeDisconnect() throws Exception {
        byte[] frameBytes = new byte[] {(byte) 0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59};
        Channel shouldBeDropped = mock(Channel.class);
        when(shouldBeDropped.getNodeStatistics())
                .thenReturn(new NodeStatistics(new Node(new byte[0], "", 0)));

        snappyDecode(frameBytes, shouldBeDropped);

        assertTrue(shouldBeDropped.getNodeStatistics().wasDisconnected());
        String stats = shouldBeDropped.getNodeStatistics().toString();
        assertTrue(stats.contains(BAD_PROTOCOL.toString()));
    }

    private void snappyDecode(byte[] payload, Channel channel) throws Exception {
        SnappyCodec codec = new SnappyCodec(channel);

        FrameCodec.Frame msg = new FrameCodec.Frame(1, payload);

        List<Object> result = newArrayList();
        codec.decode(null, msg, result);
    }

    private List<Object> snappyDecode(Resource hexEncodedSnappyResource) throws Exception {
        Channel channel = new Channel();
        SnappyCodec codec = new SnappyCodec(channel);

        byte[] golangSnappyBytes = FileCopyUtils.copyToByteArray(hexEncodedSnappyResource.getInputStream());
        FrameCodec.Frame msg = new FrameCodec.Frame(1, Hex.decode(golangSnappyBytes));

        List<Object> result = newArrayList();
        codec.decode(null, msg, result);
        return result;
    }
}