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
package org.ethereum.erp;

import org.ethereum.erp.RawStateChangeObject.RawStateChangeAction;

import java.math.BigInteger;
import java.util.Arrays;

import static org.ethereum.util.ByteUtil.hexStringToBytes;

public class StateChangeObject {
    public String erpId;
    public long targetBlock;
    public StateChangeAction[] actions;

    public static StateChangeObject parse(RawStateChangeObject raw) {
        StateChangeObject result = new StateChangeObject();
        result.erpId = raw.erpId;
        result.targetBlock = raw.targetBlock;
        result.actions = Arrays.stream(raw.actions)
                .map(StateChangeAction::parse)
                .toArray(StateChangeAction[]::new);

        return result;
    }

    public static class StateChangeAction {
        String type;
        byte[] fromAddress;
        byte[] toAddress;

        BigInteger valueInWei;

        byte[] code;
        byte[] expectedCodeHash;

        public static StateChangeAction parse(RawStateChangeAction raw) {
            StateChangeAction result = new StateChangeAction();
            result.type = raw.type;
            result.fromAddress = hexStringToBytes(raw.fromAddress);
            result.toAddress = hexStringToBytes(raw.toAddress);
            result.valueInWei = BigInteger.valueOf(raw.valueInWei);
            result.code = hexStringToBytes(raw.code);
            result.expectedCodeHash = hexStringToBytes(raw.expectedCodeHash);
            return result;
        }
    }
}

