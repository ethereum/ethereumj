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

import org.ethereum.net.server.Channel;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

/**
 * EIP-706 provides snappy samples. FrameCodec should be
 * able to decode these samples.
 */
public class SnappyCodecTest {
    @Test
    public void test_decode_go_generated_snappy() throws Exception {
        Resource golangGeneratedSnappy = new ClassPathResource("/rlp/block.go.snappy");
        List<Object> result = snappyDecode(golangGeneratedSnappy);
        assertEquals(1, result.size());
    }

    @Test
    public void test_decode_python_generated_snappy() throws Exception {
        Resource pythonGeneratedSnappy = new ClassPathResource("/rlp/block.py.snappy");
        List<Object> result = snappyDecode(pythonGeneratedSnappy);
        assertEquals(1, result.size());
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