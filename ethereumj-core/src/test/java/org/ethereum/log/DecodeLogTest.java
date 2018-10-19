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
package org.ethereum.log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ethereum.core.CallTransaction;
import org.ethereum.solidity.Abi;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class DecodeLogTest {

	@Test
	public void decodeEventWithIndexedParamsTest() {
		byte[] encodedLog = Hex.decode("f89b9494d9cf9ed550a358d4576e2efd168a80075c648bf863a027772adc63db07aae765b71eb2b533064fa781bd57457e1b138592d8198d0959a0000000000000000000000000bb8492b71d933c1da7ac154b4d01bf54d6f09e99a0000000000000000000000000f84e5656d026ba9c321394a59cca7cd8e705f448a00000000000000000000000000000000000000000000000000000000000000064");
		String abi = "[{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint128\"}],\"name\":\"Transfer\",\"type\":\"event\"}]\n";
		LogInfo logInfo = new LogInfo(encodedLog);
		CallTransaction.Contract contract = new CallTransaction.Contract(abi);
		CallTransaction.Invocation invocation = contract.parseEvent(logInfo);
		Assert.assertEquals(3, invocation.args.length);
		Assert.assertArrayEquals(Hex.decode("bb8492b71d933c1da7ac154b4d01bf54d6f09e99"), (byte[]) invocation.args[0]);
		Assert.assertArrayEquals(Hex.decode("f84e5656d026ba9c321394a59cca7cd8e705f448"), (byte[]) invocation.args[1]);
		Assert.assertEquals(new BigInteger("100"), invocation.args[2]);
	}

    @Test
    public void testBytesIndexedParam() {
        String abiJson = "[{\n" +
                "    'anonymous': false,\n" +
                "    'inputs': [\n" +
                "      {\n" +
                "        'indexed': true,\n" +
                "        'name': 'from',\n" +
                "        'type': 'address'\n" +
                "      },\n" +
                "      {\n" +
                "        'indexed': true,\n" +
                "        'name': 'to',\n" +
                "        'type': 'address'\n" +
                "      },\n" +
                "      {\n" +
                "        'indexed': false,\n" +
                "        'name': 'value',\n" +
                "        'type': 'uint256'\n" +
                "      },\n" +
                "      {\n" +
                "        'indexed': true,\n" +
                "        'name': 'data',\n" +
                "        'type': 'bytes'\n" +
                "      }\n" +
                "    ],\n" +
                "    'name': 'Transfer',\n" +
                "    'type': 'event'\n" +
                "  }]";
        CallTransaction.Contract contract = new CallTransaction.Contract(abiJson.replaceAll("'", "\""));
        List<DataWord> topics = new ArrayList<>();
        topics.add(DataWord.of("e19260aff97b920c7df27010903aeb9c8d2be5d310a2c67824cf3f15396e4c16"));
        topics.add(DataWord.of("000000000000000000000000c9ca2e8db68ffeb21978ea30a0b762f0ad2d445b"));
        topics.add(DataWord.of("000000000000000000000000d71ebe710322f0a95504cdd12294f613536204ce"));
        topics.add(DataWord.of("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"));
        byte[] data = Hex.decode("00000000000000000000000000000000000000000000000000002d7982473f00");
        LogInfo logInfo = new LogInfo(Hex.decode("2Cc114bbE7b551d62B15C465c7bdCccd9125b182"), topics, data);
        CallTransaction.Invocation e = contract.parseEvent(logInfo);

        Assert.assertEquals(4, e.args.length);
        Assert.assertArrayEquals(Hex.decode("c9ca2e8db68ffeb21978ea30a0b762f0ad2d445b"), (byte[]) e.args[0]);
        Assert.assertArrayEquals(Hex.decode("d71ebe710322f0a95504cdd12294f613536204ce"), (byte[]) e.args[1]);
        Assert.assertEquals(ByteUtil.bytesToBigInteger(Hex.decode("002d7982473f00")), e.args[2]);
        Assert.assertArrayEquals(Hex.decode("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"), (byte[]) e.args[3]);

        // No exception - OK

        Abi abi = Abi.fromJson(abiJson.replaceAll("'", "\""));
        Abi.Event event = abi.findEvent(p -> true);
        List<?> args = event.decode(data, topics.stream().map(DataWord::getData).collect(Collectors.toList()).toArray(new byte[0][]));

        Assert.assertEquals(4, args.size());
        Assert.assertArrayEquals(Hex.decode("c9ca2e8db68ffeb21978ea30a0b762f0ad2d445b"), (byte[]) args.get(0));
        Assert.assertArrayEquals(Hex.decode("d71ebe710322f0a95504cdd12294f613536204ce"), (byte[]) args.get(1));
        Assert.assertEquals(ByteUtil.bytesToBigInteger(Hex.decode("002d7982473f00")), args.get(2));
        Assert.assertArrayEquals(Hex.decode("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"), (byte[]) args.get(3));

    }

    @Test
    public void testBytesIndexedParamAnonymous() {
        String abiJson = "[{\n" +
                "    'anonymous': true,\n" +
                "    'inputs': [\n" +
                "      {\n" +
                "        'indexed': true,\n" +
                "        'name': 'from',\n" +
                "        'type': 'address'\n" +
                "      },\n" +
                "      {\n" +
                "        'indexed': true,\n" +
                "        'name': 'to',\n" +
                "        'type': 'address'\n" +
                "      },\n" +
                "      {\n" +
                "        'indexed': false,\n" +
                "        'name': 'value',\n" +
                "        'type': 'uint256'\n" +
                "      },\n" +
                "      {\n" +
                "        'indexed': true,\n" +
                "        'name': 'data',\n" +
                "        'type': 'bytes'\n" +
                "      }\n" +
                "    ],\n" +
                "    'name': 'Transfer',\n" +
                "    'type': 'event'\n" +
                "  }]";
        CallTransaction.Contract contract = new CallTransaction.Contract(abiJson.replaceAll("'", "\""));
        List<DataWord> topics = new ArrayList<>();
        topics.add(DataWord.of("000000000000000000000000c9ca2e8db68ffeb21978ea30a0b762f0ad2d445b"));
        topics.add(DataWord.of("000000000000000000000000d71ebe710322f0a95504cdd12294f613536204ce"));
        topics.add(DataWord.of("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"));
        byte[] data = Hex.decode("00000000000000000000000000000000000000000000000000002d7982473f00");

        Abi abi = Abi.fromJson(abiJson.replaceAll("'", "\""));
        Abi.Event event = abi.findEvent(p -> true);
        List<?> args = event.decode(data, topics.stream().map(DataWord::getData).collect(Collectors.toList()).toArray(new byte[0][]));

        Assert.assertEquals(4, args.size());
        Assert.assertArrayEquals(Hex.decode("c9ca2e8db68ffeb21978ea30a0b762f0ad2d445b"), (byte[]) args.get(0));
        Assert.assertArrayEquals(Hex.decode("d71ebe710322f0a95504cdd12294f613536204ce"), (byte[]) args.get(1));
        Assert.assertEquals(ByteUtil.bytesToBigInteger(Hex.decode("002d7982473f00")), args.get(2));
        Assert.assertArrayEquals(Hex.decode("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"), (byte[]) args.get(3));

    }
}
