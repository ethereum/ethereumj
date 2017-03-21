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

import org.ethereum.core.CallTransaction;
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
}
