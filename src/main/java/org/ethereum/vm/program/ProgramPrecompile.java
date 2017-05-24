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
package org.ethereum.vm.program;

import org.ethereum.datasource.Source;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.ethereum.vm.OpCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 06.02.2017.
 */
public class ProgramPrecompile {
    private static final int version = 1;

    private Set<Integer> jumpdest = new HashSet<>();

    public byte[] serialize() {
        byte[][] jdBytes = new byte[jumpdest.size() + 1][];
        int cnt = 0;
        jdBytes[cnt++] = RLP.encodeInt(version);
        for (Integer dst : jumpdest) {
            jdBytes[cnt++] = RLP.encodeInt(dst);
        }

        return RLP.encodeList(jdBytes);
    }

    public static ProgramPrecompile deserialize(byte[] stream) {
        RLPList l = (RLPList) RLP.decode2(stream).get(0);
        int ver = ByteUtil.byteArrayToInt(l.get(0).getRLPData());
        if (ver != version) return null;
        ProgramPrecompile ret = new ProgramPrecompile();
        for (int i = 1; i < l.size(); i++) {
            ret.jumpdest.add(ByteUtil.byteArrayToInt(l.get(i).getRLPData()));
        }
        return ret;
    }

    public static ProgramPrecompile compile(byte[] ops) {
        ProgramPrecompile ret = new ProgramPrecompile();
        for (int i = 0; i < ops.length; ++i) {

            OpCode op = OpCode.code(ops[i]);
            if (op == null) continue;

            if (op.equals(OpCode.JUMPDEST)) ret.jumpdest.add(i);

            if (op.asInt() >= OpCode.PUSH1.asInt() && op.asInt() <= OpCode.PUSH32.asInt()) {
                i += op.asInt() - OpCode.PUSH1.asInt() + 1;
            }
        }
        return ret;
    }

    public boolean hasJumpDest(int pc) {
        return jumpdest.contains(pc);
    }

    public static void main(String[] args) throws Exception {
        ProgramPrecompile pp = new ProgramPrecompile();
        pp.jumpdest.add(100);
        pp.jumpdest.add(200);
        byte[] bytes = pp.serialize();

        ProgramPrecompile pp1 = ProgramPrecompile.deserialize(bytes);
        System.out.println(pp1.jumpdest);
    }
}
