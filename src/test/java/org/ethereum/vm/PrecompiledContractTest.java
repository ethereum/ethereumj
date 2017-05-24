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
package org.ethereum.vm;

import org.ethereum.util.ByteUtil;
import org.ethereum.vm.PrecompiledContracts.PrecompiledContract;

import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

/**
 * @author Roman Mandeleil
 */
public class PrecompiledContractTest {


    @Test
    public void identityTest1() {

        DataWord addr = new DataWord("0000000000000000000000000000000000000000000000000000000000000004");
        PrecompiledContract contract = PrecompiledContracts.getContractForAddress(addr);
        byte[] data = Hex.decode("112233445566");
        byte[] expected = Hex.decode("112233445566");

        byte[] result = contract.execute(data);

        assertArrayEquals(expected, result);
    }


    @Test
    public void sha256Test1() {

        DataWord addr = new DataWord("0000000000000000000000000000000000000000000000000000000000000002");
        PrecompiledContract contract = PrecompiledContracts.getContractForAddress(addr);
        byte[] data = null;
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        byte[] result = contract.execute(data);

        assertEquals(expected, Hex.toHexString(result));
    }

    @Test
    public void sha256Test2() {

        DataWord addr = new DataWord("0000000000000000000000000000000000000000000000000000000000000002");
        PrecompiledContract contract = PrecompiledContracts.getContractForAddress(addr);
        byte[] data = ByteUtil.EMPTY_BYTE_ARRAY;
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        byte[] result = contract.execute(data);

        assertEquals(expected, Hex.toHexString(result));
    }

    @Test
    public void sha256Test3() {

        DataWord addr = new DataWord("0000000000000000000000000000000000000000000000000000000000000002");
        PrecompiledContract contract = PrecompiledContracts.getContractForAddress(addr);
        byte[] data = Hex.decode("112233");
        String expected = "49ee2bf93aac3b1fb4117e59095e07abe555c3383b38d608da37680a406096e8";

        byte[] result = contract.execute(data);

        assertEquals(expected, Hex.toHexString(result));
    }


    @Test
    public void Ripempd160Test1() {

        DataWord addr = new DataWord("0000000000000000000000000000000000000000000000000000000000000003");
        PrecompiledContract contract = PrecompiledContracts.getContractForAddress(addr);
        byte[] data = Hex.decode("0000000000000000000000000000000000000000000000000000000000000001");
        String expected = "000000000000000000000000ae387fcfeb723c3f5964509af111cf5a67f30661";

        byte[] result = contract.execute(data);

        assertEquals(expected, Hex.toHexString(result));
    }

    @Test
    public void ecRecoverTest1() {

        byte[] data = Hex.decode("18c547e4f7b0f325ad1e56f57e26c745b09a3e503d86e00e5255ff7f715d3d1c000000000000000000000000000000000000000000000000000000000000001c73b1693892219d736caba55bdb67216e485557ea6b6af75f37096c9aa6a5a75feeb940b1d03b21e36b0e47e79769f095fe2ab855bd91e3a38756b7d75a9c4549");
        DataWord addr = new DataWord("0000000000000000000000000000000000000000000000000000000000000001");
        PrecompiledContract contract = PrecompiledContracts.getContractForAddress(addr);
        String expected = "000000000000000000000000ae387fcfeb723c3f5964509af111cf5a67f30661";

        byte[] result = contract.execute(data);

        System.out.println(Hex.toHexString(result));


    }

}
