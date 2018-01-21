/*
 * Copyright (c) [2018] [ <ether.camp> ]
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
package org.ethereum.test;

import org.ethereum.vm.OpCode;

import java.util.ArrayList;
import java.util.List;

public class BytecodeCompiler {
    public byte[] compile(String code) {
        return compile(code.split("\\s+"));
    }

    private byte[] compile(String[] tokens) {
        List<Byte> bytecodes = new ArrayList<>();
        int ntokens = tokens.length;

        for (int i = 0; i < ntokens; i++) {
            String token = tokens[i].trim().toUpperCase();

            if (token.isEmpty())
                continue;

            bytecodes.add(OpCode.byteVal(token));
        }

        int nbytes = bytecodes.size();
        byte[] bytes = new byte[nbytes];

        for (int k = 0; k < nbytes; k++)
            bytes[k] = bytecodes.get(k).byteValue();

        return bytes;
    }
}
