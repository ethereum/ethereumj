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

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.ConstantinopleConfig;
import org.ethereum.config.blockchain.DaoHFConfig;
import org.ethereum.core.Repository;
import org.ethereum.vm.program.Program;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.util.ByteUtil.oneByteToHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple tests for VM Memory, Storage and Flow Operations
 */
public class VMMemoryOpTest extends VMBaseOpTest {
    private static final SystemProperties constantinopleConfig = new SystemProperties(){{
        setBlockchainConfig(new ConstantinopleConfig(new DaoHFConfig()));
    }};

    @Test  // PUSH1 OP
    public void testPUSH1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xa0"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000A0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH2 OP
    public void testPUSH2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0xa0b0"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000A0B0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH3 OP
    public void testPUSH3() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0xA0B0C0"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000A0B0C0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH4 OP
    public void testPUSH4() {

        VM vm = new VM();
        program = new Program(compile("PUSH4 0xA0B0C0D0"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000A0B0C0D0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH5 OP
    public void testPUSH5() {

        VM vm = new VM();
        program = new Program(compile("PUSH5 0xA0B0C0D0E0"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000A0B0C0D0E0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH6 OP
    public void testPUSH6() {

        VM vm = new VM();
        program = new Program(compile("PUSH6 0xA0B0C0D0E0F0"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000A0B0C0D0E0F0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH7 OP
    public void testPUSH7() {

        VM vm = new VM();
        program = new Program(compile("PUSH7 0xA0B0C0D0E0F0A1"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH8 OP
    public void testPUSH8() {

        VM vm = new VM();
        program = new Program(compile("PUSH8 0xA0B0C0D0E0F0A1B1"), invoke);
        String expected = "000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH9 OP
    public void testPUSH9() {

        VM vm = new VM();
        program = new Program(compile("PUSH9 0xA0B0C0D0E0F0A1B1C1"), invoke);
        String expected = "0000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test  // PUSH10 OP
    public void testPUSH10() {

        VM vm = new VM();
        program = new Program(compile("PUSH10 0xA0B0C0D0E0F0A1B1C1D1"), invoke);
        String expected = "00000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH11 OP
    public void testPUSH11() {

        VM vm = new VM();
        program = new Program(compile("PUSH11 0xA0B0C0D0E0F0A1B1C1D1E1"), invoke);
        String expected = "000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH12 OP
    public void testPUSH12() {

        VM vm = new VM();
        program = new Program(compile("PUSH12 0xA0B0C0D0E0F0A1B1C1D1E1F1"), invoke);
        String expected = "0000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH13 OP
    public void testPUSH13() {

        VM vm = new VM();
        program = new Program(compile("PUSH13 0xA0B0C0D0E0F0A1B1C1D1E1F1A2"), invoke);
        String expected = "00000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH14 OP
    public void testPUSH14() {

        VM vm = new VM();
        program = new Program(compile("PUSH14 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2"), invoke);
        String expected = "000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH15 OP
    public void testPUSH15() {

        VM vm = new VM();
        program = new Program(compile("PUSH15 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2"), invoke);
        String expected = "0000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH16 OP
    public void testPUSH16() {

        VM vm = new VM();
        program = new Program(compile("PUSH16 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2"), invoke);
        String expected = "00000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH17 OP
    public void testPUSH17() {

        VM vm = new VM();
        program = new Program(compile("PUSH17 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2"), invoke);
        String expected = "000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH18 OP
    public void testPUSH18() {

        VM vm = new VM();
        program = new Program(compile("PUSH18 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2"), invoke);
        String expected = "0000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH19 OP
    public void testPUSH19() {

        VM vm = new VM();
        program = new Program(compile("PUSH19 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3"), invoke);
        String expected = "00000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH20 OP
    public void testPUSH20() {

        VM vm = new VM();
        program = new Program(compile("PUSH20 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3"), invoke);
        String expected = "000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH21 OP
    public void testPUSH21() {

        VM vm = new VM();
        program = new Program(compile("PUSH21 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3"), invoke);
        String expected = "0000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH22 OP
    public void testPUSH22() {

        VM vm = new VM();
        program = new Program(compile("PUSH22 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3"), invoke);
        String expected = "00000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH23 OP
    public void testPUSH23() {

        VM vm = new VM();
        program = new Program(compile("PUSH23 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3"), invoke);
        String expected = "000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH24 OP
    public void testPUSH24() {

        VM vm = new VM();
        program = new Program(compile("PUSH24 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3"), invoke);
        String expected = "0000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH25 OP
    public void testPUSH25() {

        VM vm = new VM();
        program = new Program(compile("PUSH25 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4"), invoke);
        String expected = "00000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH26 OP
    public void testPUSH26() {

        VM vm = new VM();
        program = new Program(compile("PUSH26 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4"), invoke);
        String expected = "000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH27 OP
    public void testPUSH27() {

        VM vm = new VM();
        program = new Program(compile("PUSH27 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4"), invoke);
        String expected = "0000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH28 OP
    public void testPUSH28() {

        VM vm = new VM();
        program = new Program(compile("PUSH28 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4"), invoke);
        String expected = "00000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH29 OP
    public void testPUSH29() {

        VM vm = new VM();
        program = new Program(compile("PUSH29 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4"), invoke);
        String expected = "000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH30 OP
    public void testPUSH30() {

        VM vm = new VM();
        program = new Program(compile("PUSH30 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4"), invoke);
        String expected = "0000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH31 OP
    public void testPUSH31() {

        VM vm = new VM();
        program = new Program(compile("PUSH31 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1"), invoke);
        String expected = "00A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // PUSH32 OP
    public void testPUSH32() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1"), invoke);
        String expected = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // PUSHN OP not enough data
    public void testPUSHN_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0xAA"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000AA00";

        program.fullTrace();
        vm.step(program);

        assertTrue(program.isStopped());
        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // PUSHN OP not enough data
    public void testPUSHN_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xAABB"), invoke);
        String expected = "AABB000000000000000000000000000000000000000000000000000000000000";

        program.fullTrace();
        vm.step(program);

        assertTrue(program.isStopped());
        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // POP OP
    public void testPOP_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x0000 PUSH1 0x01 PUSH3 0x000002 POP"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // POP OP
    public void testPOP_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x0000 PUSH1 0x01 PUSH3 0x000002 POP POP"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = Program.StackTooSmallException.class)  // POP OP mal data
    public void testPOP_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x0000 PUSH1 0x01 PUSH3 0x000002 POP POP POP POP"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // DUP1...DUP16 OP
    public void testDUPS() {
        for (int i = 1; i < 17; i++) {
            testDUPN_1(i);
        }
    }

    /**
     * Generic test function for DUP1-16
     *
     * @param n in DUPn
     */
    private void testDUPN_1(int n) {

        VM vm = new VM();
        String programCode = "";

        for (int i = 0; i < n; i++) {
            programCode += "PUSH1 0x" + (12 + i) + " ";
        }

        programCode += "DUP" + n;

        program = new Program(compile(programCode), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000012";
        int expectedLen = n + 1;

        for (int i = 0; i < expectedLen; i++) {
            vm.step(program);
        }

        assertEquals(expectedLen, program.getStack().toArray().length);
        assertEquals(expected, Hex.toHexString(program.stackPop().getData()).toUpperCase());
        for (int i = 0; i < expectedLen - 2; i++) {
            assertNotEquals(expected, Hex.toHexString(program.stackPop().getData()).toUpperCase());
        }
        assertEquals(expected, Hex.toHexString(program.stackPop().getData()).toUpperCase());
    }

    @Test(expected = Program.StackTooSmallException.class)  // DUPN OP mal data
    public void testDUPN_2() {

        VM vm = new VM();
        program = new Program(compile("DUP1"), invoke);
        try {
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // SWAP1...SWAP16 OP
    public void testSWAPS() {
        for (int i = 1; i < 17; ++i) {
            testSWAPN_1(i);
        }
    }

    /**
     * Generic test function for SWAP1-16
     *
     * @param n in SWAPn
     */
    private void testSWAPN_1(int n) {

        VM vm = new VM();

        String programCode = "";
        String top = DataWord.of(0x10 + n).toString();

        for (int i = n; i > -1; --i) {
            programCode += "PUSH1 0x" + oneByteToHexString((byte) (0x10 + i)) + " ";
        }

        programCode += "SWAP" + n;

        program = new Program(compile(programCode), invoke);

        for (int i = 0; i < n + 2; ++i) {
            vm.step(program);
        }

        assertEquals(n + 1, program.getStack().toArray().length);
        assertEquals(top, Hex.toHexString(program.stackPop().getData()));
    }

    @Test(expected = Program.StackTooSmallException.class)  // SWAPN OP mal data
    public void testSWAPN_2() {

        VM vm = new VM();
        program = new Program(compile("SWAP1"), invoke);

        try {
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MSTORE OP
    public void testMSTORE_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getMemory()));
    }

    @Test // MSTORE OP
    public void testMSTORE_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x5566 PUSH1 0x20 MSTORE"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000001234" +
                "0000000000000000000000000000000000000000000000000000000000005566";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getMemory()));
    }

    @Test // MSTORE OP
    public void testMSTORE_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x5566 PUSH1 0x20 MSTORE PUSH2 0x8888 PUSH1 0x00 MSTORE"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000008888" +
                "0000000000000000000000000000000000000000000000000000000000005566";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getMemory()));
    }

    @Test // MSTORE OP
    public void testMSTORE_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0xA0 MSTORE"), invoke);
        String expected = "" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getMemory()));
    }

    @Test(expected = Program.StackTooSmallException.class) // MSTORE OP
    public void testMSTORE_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 MSTORE"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MLOAD OP
    public void testMLOAD_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x00 MLOAD"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 MLOAD"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()).toUpperCase());
        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test // MLOAD OP
    public void testMLOAD_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x20 MLOAD"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x20 MSTORE PUSH1 0x20 MLOAD"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000001234";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x20 MSTORE PUSH1 0x1F MLOAD"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000001234";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000012";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = Program.StackTooSmallException.class) // MLOAD OP mal data
    public void testMLOAD_6() {

        VM vm = new VM();
        program = new Program(compile("MLOAD"), invoke);
        try {
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MSTORE8 OP
    public void testMSTORE8_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x11 PUSH1 0x00 MSTORE8"), invoke);
        String m_expected = "1100000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
    }


    @Test // MSTORE8 OP
    public void testMSTORE8_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 PUSH1 0x01 MSTORE8"), invoke);
        String m_expected = "0022000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
    }

    @Test // MSTORE8 OP
    public void testMSTORE8_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 PUSH1 0x21 MSTORE8"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                "0022000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.getMemory()));
    }

    @Test(expected = Program.StackTooSmallException.class) // MSTORE8 OP mal
    public void testMSTORE8_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 MSTORE8"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // SSTORE OP
    public void testSSTORE_1() {

        VM vm = new VM();

        program = new Program(compile("PUSH1 0x22 PUSH1 0xAA SSTORE"), invoke);
        String s_expected_key = "00000000000000000000000000000000000000000000000000000000000000AA";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord key = DataWord.of(Hex.decode(s_expected_key));
        DataWord val = program.getStorage().getStorageValue(invoke.getOwnerAddress()
                .getNoLeadZeroesData(), key);

        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // SSTORE OP
    public void testSSTORE_2() {

        VM vm = new VM();

        program = new Program(compile("PUSH1 0x22 PUSH1 0xAA SSTORE PUSH1 0x22 PUSH1 0xBB SSTORE"), invoke);
        String s_expected_key = "00000000000000000000000000000000000000000000000000000000000000BB";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        Repository repository = program.getStorage();
        DataWord key = DataWord.of(Hex.decode(s_expected_key));
        DataWord val = repository.getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(), key);

        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test(expected = Program.StackTooSmallException.class) // SSTORE OP
    public void testSSTORE_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 SSTORE"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_1() {

        VM vm = new VM();

        program = new Program(Hex.decode("60006000556000600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(412, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_2() {

        VM vm = new VM();

        program = new Program(Hex.decode("60006000556001600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(20212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_3() {

        VM vm = new VM();

        program = new Program(Hex.decode("60016000556000600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(20212, program.getResult().getGasUsed());
        assertEquals(19800, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_4() {

        VM vm = new VM();

        program = new Program(Hex.decode("60016000556002600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(20212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_5() {

        VM vm = new VM();

        program = new Program(Hex.decode("60016000556001600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(20212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_6() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60006000556000600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(15000, program.getResult().getFutureRefund());
    }

    /**
     * Sets Storage row on "cow" address:
     * 0: 1
     */
    private void setStorageToOne(VM vm) {
        // Sets storage value to 1 and commits
        program = new Program(Hex.decode("60006000556001600055"), invoke, constantinopleConfig);
        while (!program.isStopped())
            vm.step(program);
        invoke.getRepository().commit();
        invoke.setOrigRepository(invoke.getRepository());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_7() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60006000556001600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(4800, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_8() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60006000556002600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_9() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60026000556000600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(15000, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_10() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60026000556003600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_11() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60026000556001600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(4800, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_12() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60026000556002600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_13() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60016000556000600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(15000, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_14() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60016000556002600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(5212, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_15() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("60016000556001600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(412, program.getResult().getGasUsed());
        assertEquals(0, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_16() {

        VM vm = new VM();

        program = new Program(Hex.decode("600160005560006000556001600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(40218, program.getResult().getGasUsed());
        assertEquals(19800, program.getResult().getFutureRefund());
    }

    @Test // SSTORE EIP1283
    public void testSSTORE_NET_17() {

        VM vm = new VM();
        setStorageToOne(vm);
        program = new Program(Hex.decode("600060005560016000556000600055"), invoke, constantinopleConfig);

        while (!program.isStopped())
            vm.step(program);

        assertEquals(10218, program.getResult().getGasUsed());
        assertEquals(19800, program.getResult().getFutureRefund());
    }

    @Test // SLOAD OP
    public void testSLOAD_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xAA SLOAD"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // SLOAD OP
    public void testSLOAD_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 PUSH1 0xAA SSTORE PUSH1 0xAA SLOAD"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test // SLOAD OP
    public void testSLOAD_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 PUSH1 0xAA SSTORE PUSH1 0x33 PUSH1 0xCC SSTORE PUSH1 0xCC SLOAD"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000033";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = Program.StackTooSmallException.class) // SLOAD OP
    public void testSLOAD_4() {

        VM vm = new VM();
        program = new Program(compile("SLOAD"), invoke);
        try {
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // PC OP
    public void testPC_1() {

        VM vm = new VM();
        program = new Program(compile("PC"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test // PC OP
    public void testPC_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 PUSH1 0xAA MSTORE PUSH1 0xAA SLOAD PC"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000008";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = Program.BadJumpDestinationException.class) // JUMP OP mal data
    public void testJUMP_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xAA PUSH1 0xBB PUSH1 0x0E JUMP PUSH1 0xCC PUSH1 0xDD PUSH1 0xEE JUMPDEST PUSH1 0xFF"), invoke);
        String s_expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = Program.BadJumpDestinationException.class) // JUMP OP mal data
    public void testJUMP_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x0C PUSH1 0x0C SWAP1 JUMP PUSH1 0xCC PUSH1 0xDD PUSH1 0xEE PUSH1 0xFF"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // JUMPI OP
    public void testJUMPI_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH1 0x05 JUMPI JUMPDEST PUSH1 0xCC"), invoke);
        String s_expected = "00000000000000000000000000000000000000000000000000000000000000CC";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test // JUMPI OP
    public void testJUMPI_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH4 0x00000000 PUSH1 0x44 JUMPI PUSH1 0xCC PUSH1 0xDD"), invoke);
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000000DD";
        String s_expected_2 = "00000000000000000000000000000000000000000000000000000000000000CC";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        DataWord item2 = program.stackPop();

        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
        assertEquals(s_expected_2, Hex.toHexString(item2.getData()).toUpperCase());
    }

    @Test(expected = Program.StackTooSmallException.class) // JUMPI OP mal
    public void testJUMPI_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 JUMPI"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test(expected = Program.BadJumpDestinationException.class) // JUMPI OP mal
    public void testJUMPI_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH1 0x22 SWAP1 SWAP1 JUMPI"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test(expected = Program.BadJumpDestinationException.class) // JUMP OP mal data
    public void testJUMPDEST_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x23 PUSH1 0x08 JUMP PUSH1 0x01 JUMPDEST PUSH1 0x02 SSTORE"), invoke);

        String s_expected_key = "0000000000000000000000000000000000000000000000000000000000000002";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000023";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord key = DataWord.of(Hex.decode(s_expected_key));
        DataWord val = program.getStorage().getStorageValue(invoke.getOwnerAddress()
                .getNoLeadZeroesData(), key);

        assertTrue(program.isStopped());
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // JUMPDEST OP for JUMPI
    public void testJUMPDEST_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x23 PUSH1 0x01 PUSH1 0x09 JUMPI PUSH1 0x01 JUMPDEST PUSH1 0x02 SSTORE"), invoke);

        String s_expected_key = "0000000000000000000000000000000000000000000000000000000000000002";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000023";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord key = DataWord.of(Hex.decode(s_expected_key));
        DataWord val = program.getStorage().getStorageValue(invoke.getOwnerAddress()
                .getNoLeadZeroesData(), key);

        assertTrue(program.isStopped());
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // MSIZE OP
    public void testMSIZE_1() {

        VM vm = new VM();
        program = new Program(compile("MSIZE"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MSIZE OP
    public void testMSIZE_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x20 PUSH1 0x30 MSTORE MSIZE"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000060";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }
}
