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
package org.ethereum.jsontestsuite.suite;

import org.ethereum.util.ByteUtil;

import org.json.simple.JSONObject;

import org.spongycastle.util.encoders.Hex;

/**
 * @author Roman Mandeleil
 * @since 28.06.2014
 */
public class CallCreate {

    private final byte[] data;
    private final byte[] destination;
    private final byte[] gasLimit;
    private final byte[] value;

/* e.g.
        "data" : [
                ],
        "destination" : "cd1722f3947def4cf144679da39c4c32bdc35681",
        "gasLimit" : 9792,
        "value" : 74
*/

    public CallCreate(JSONObject callCreateJSON) {

        String data = callCreateJSON.get("data").toString();
        String destination = callCreateJSON.get("destination").toString();
        String gasLimit = callCreateJSON.get("gasLimit").toString();
        String value = callCreateJSON.get("value").toString();

        if (data != null && data.length() > 2)
            this.data = Utils.parseData(data);
        else
            this.data = ByteUtil.EMPTY_BYTE_ARRAY;

        this.destination = Utils.parseData(destination);
        this.gasLimit = ByteUtil.bigIntegerToBytes(TestCase.toBigInt(gasLimit));
        this.value = ByteUtil.bigIntegerToBytes(TestCase.toBigInt(value));
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getDestination() {
        return destination;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "CallCreate{" +
                "data=" + Hex.toHexString(data) +
                ", destination=" + Hex.toHexString(destination) +
                ", gasLimit=" + Hex.toHexString(gasLimit) +
                ", value=" + Hex.toHexString(value) +
                '}';
    }
}
