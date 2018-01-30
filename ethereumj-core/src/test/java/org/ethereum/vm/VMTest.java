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

import org.ethereum.core.Repository;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.Program.BadJumpDestinationException;
import org.ethereum.vm.program.Program.StackTooSmallException;

import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.junit.*;
import org.junit.runners.MethodSorters;

import org.spongycastle.util.encoders.Hex;

import java.util.List;

import static org.ethereum.util.ByteUtil.oneByteToHexString;
import static org.junit.Assert.*;

/**
 * @author Roman Mandeleil
 * @since 01.06.2014
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VMTest {

    private ProgramInvokeMockImpl invoke;
    private Program program;

    @Before
    public void setup() {
        invoke = new ProgramInvokeMockImpl();
    }

    @After
    public void tearDown() {
        invoke.getRepository().close();
    }

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

    @Test  // AND OP
    public void testAND_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x0A PUSH1 0x0A AND"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000000A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // AND OP
    public void testAND_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xC0 PUSH1 0x0A AND"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = RuntimeException.class)  // AND OP mal data
    public void testAND_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xC0 AND"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // OR OP
    public void testOR_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xF0 PUSH1 0x0F OR"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // OR OP
    public void testOR_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xC3 PUSH1 0x3C OR"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = RuntimeException.class)  // OR OP mal data
    public void testOR_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xC0 OR"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // XOR OP
    public void testXOR_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xFF PUSH1 0xFF XOR"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // XOR OP
    public void testXOR_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x0F PUSH1 0xF0 XOR"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test(expected = RuntimeException.class)  // XOR OP mal data
    public void testXOR_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xC0 XOR"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // BYTE OP
    public void testBYTE_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH6 0xAABBCCDDEEFF PUSH1 0x1E BYTE"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000EE";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // BYTE OP
    public void testBYTE_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH6 0xAABBCCDDEEFF PUSH1 0x20 BYTE"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // BYTE OP
    public void testBYTE_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH6 0xAABBCCDDEE3A PUSH1 0x1F BYTE"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000003A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test(expected = StackTooSmallException.class)  // BYTE OP mal data
    public void testBYTE_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH6 0xAABBCCDDEE3A BYTE"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // ISZERO OP
    public void testISZERO_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x00 ISZERO"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // ISZERO OP
    public void testISZERO_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x2A ISZERO"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class)  // ISZERO OP mal data
    public void testISZERO_3() {

        VM vm = new VM();
        program = new Program(compile("ISZERO"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // EQ OP
    public void testEQ_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x2A PUSH1 0x2A EQ"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // EQ OP
    public void testEQ_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x2A3B4C PUSH3 0x2A3B4C EQ"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // EQ OP
    public void testEQ_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x2A3B5C PUSH3 0x2A3B4C EQ"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class)  // EQ OP mal data
    public void testEQ_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x2A3B4C EQ"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // GT OP
    public void testGT_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH1 0x02 GT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH2 0x0F00 GT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH4 0x01020304 PUSH2 0x0F00 GT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class)  // GT OP mal data
    public void testGT_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x2A3B4C GT"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // SGT OP
    public void testSGT_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH1 0x02 SGT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SGT OP
    public void testSGT_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x000000000000000000000000000000000000000000000000000000000000001E " + //   30
                "PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "SGT"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SGT OP
    public void testSGT_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF57 " + // -169
                "SGT"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class)  // SGT OP mal
    public void testSGT_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "SGT"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // LT OP
    public void testLT_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH1 0x02 LT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH2 0x0F00 LT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH4 0x01020304 PUSH2 0x0F00 LT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class)  // LT OP mal data
    public void testLT_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x2A3B4C LT"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // SLT OP
    public void testSLT_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 PUSH1 0x02 SLT"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SLT OP
    public void testSLT_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x000000000000000000000000000000000000000000000000000000000000001E " + //   30
                "PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "SLT"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SLT OP
    public void testSLT_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF57 " + // -169
                "SLT"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class)  // SLT OP mal
    public void testSLT_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "SLT"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // NOT OP
    public void testNOT_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 NOT"), invoke);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // NOT OP
    public void testNOT_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0xA003 NOT"), invoke);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF5FFC";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }


    @Test(expected = StackTooSmallException.class)  // BNOT OP
    public void testBNOT_4() {

        VM vm = new VM();
        program = new Program(compile("NOT"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // NOT OP test from real failure
    public void testNOT_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x00 NOT"), invoke);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);

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

    @Test(expected = StackTooSmallException.class)  // POP OP mal data
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

    @Test(expected = StackTooSmallException.class)  // DUPN OP mal data
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
        String top = new DataWord(0x10 + n).toString();

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

    @Test(expected = StackTooSmallException.class)  // SWAPN OP mal data
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


    @Test // LOG0 OP
    public void tesLog0() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 LOG0"), invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        List<LogInfo> logInfoList = program.getResult().getLogInfoList();
        LogInfo logInfo = logInfoList.get(0);

        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));
        assertEquals(0, logInfo.getTopics().size());
        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo
                .getData()));
    }

    @Test // LOG1 OP
    public void tesLog1() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH1 0x20 PUSH1 0x00 LOG1"), invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        List<LogInfo> logInfoList = program.getResult().getLogInfoList();
        LogInfo logInfo = logInfoList.get(0);

        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));
        assertEquals(1, logInfo.getTopics().size());
        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo
                .getData()));
    }

    @Test // LOG2 OP
    public void tesLog2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH2 0x6666 PUSH1 0x20 PUSH1 0x00 LOG2"), invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        List<LogInfo> logInfoList = program.getResult().getLogInfoList();
        LogInfo logInfo = logInfoList.get(0);

        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));
        assertEquals(2, logInfo.getTopics().size());
        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo
                .getData()));
    }

    @Test // LOG3 OP
    public void tesLog3() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH2 0x6666 PUSH2 0x3333 PUSH1 0x20 PUSH1 0x00 LOG3"), invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        List<LogInfo> logInfoList = program.getResult().getLogInfoList();
        LogInfo logInfo = logInfoList.get(0);

        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));
        assertEquals(3, logInfo.getTopics().size());
        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo
                .getData()));
    }


    @Test // LOG4 OP
    public void tesLog4() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH2 0x6666 PUSH2 0x3333 PUSH2 0x5555 PUSH1 0x20 PUSH1 0x00 LOG4"), invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        List<LogInfo> logInfoList = program.getResult().getLogInfoList();
        LogInfo logInfo = logInfoList.get(0);

        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));
        assertEquals(4, logInfo.getTopics().size());
        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo
                .getData()));
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

    @Test(expected = StackTooSmallException.class) // MSTORE OP
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

    @Test(expected = StackTooSmallException.class) // MLOAD OP mal data
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

    @Test(expected = StackTooSmallException.class) // MSTORE8 OP mal
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

        DataWord key = new DataWord(Hex.decode(s_expected_key));
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
        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = repository.getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(), key);

        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // SSTORE OP
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

    @Test(expected = StackTooSmallException.class) // SLOAD OP
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

    @Test(expected = BadJumpDestinationException.class) // JUMP OP mal data
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

    @Test(expected = BadJumpDestinationException.class) // JUMP OP mal data
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

    @Test(expected = StackTooSmallException.class) // JUMPI OP mal
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

    @Test(expected = BadJumpDestinationException.class) // JUMPI OP mal
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

    @Test(expected = BadJumpDestinationException.class) // JUMP OP mal data
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

        DataWord key = new DataWord(Hex.decode(s_expected_key));
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

        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = program.getStorage().getStorageValue(invoke.getOwnerAddress()
                .getNoLeadZeroesData(), key);

        assertTrue(program.isStopped());
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // ADD OP mal
    public void testADD_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x02 PUSH1 0x02 ADD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000004";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ADD OP
    public void testADD_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1002 PUSH1 0x02 ADD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000001004";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ADD OP
    public void testADD_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1002 PUSH6 0x123456789009 ADD"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000012345678A00B";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // ADD OP mal
    public void testADD_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 ADD"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // ADDMOD OP mal
    public void testADDMOD_1() {
        VM vm = new VM();
        program = new Program(compile("PUSH1 0x02 PUSH1 0x02 PUSH1 0x03 ADDMOD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertTrue(program.isStopped());
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ADDMOD OP
    public void testADDMOD_2() {
        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1000 PUSH1 0x02 PUSH2 0x1002 ADDMOD PUSH1 0x00"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000004";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertFalse(program.isStopped());
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ADDMOD OP
    public void testADDMOD_3() {
        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1002 PUSH6 0x123456789009 PUSH1 0x02 ADDMOD"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000000000000093B";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertTrue(program.isStopped());
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // ADDMOD OP mal
    public void testADDMOD_4() {
        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 ADDMOD"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MUL OP
    public void testMUL_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x03 PUSH1 0x02 MUL"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000006";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MUL OP
    public void testMUL_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x222222 PUSH1 0x03 MUL"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000666666";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MUL OP
    public void testMUL_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x222222 PUSH3 0x333333 MUL"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000006D3A05F92C6";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // MUL OP mal
    public void testMUL_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 MUL"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MULMOD OP
    public void testMULMOD_1() {
        VM vm = new VM();
        program = new Program(compile("PUSH1 0x03 PUSH1 0x02 PUSH1 0x04 MULMOD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MULMOD OP
    public void testMULMOD_2() {
        VM vm = new VM();
        program = new Program(compile("PUSH3 0x222222 PUSH1 0x03 PUSH1 0x04 MULMOD"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000000000000000C";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MULMOD OP
    public void testMULMOD_3() {
        VM vm = new VM();
        program = new Program(compile("PUSH3 0x222222 PUSH3 0x333333 PUSH3 0x444444 MULMOD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // MULMOD OP mal
    public void testMULMOD_4() {
        VM vm = new VM();
        program = new Program(compile("PUSH1 0x01 MULMOD"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // DIV OP
    public void testDIV_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x02 PUSH1 0x04 DIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // DIV OP
    public void testDIV_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x33 PUSH1 0x99 DIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000003";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // DIV OP
    public void testDIV_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x22 PUSH1 0x99 DIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000004";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // DIV OP
    public void testDIV_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x15 PUSH1 0x99 DIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000007";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // DIV OP
    public void testDIV_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x04 PUSH1 0x07 DIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // DIV OP
    public void testDIV_6() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x07 DIV"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // SDIV OP
    public void testSDIV_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x03E8 PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC18 SDIV"), invoke);
        String s_expected_1 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SDIV OP
    public void testSDIV_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xFF PUSH1 0xFF SDIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SDIV OP
    public void testSDIV_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x00 PUSH1 0xFF SDIV"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // SDIV OP mal
    public void testSDIV_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0xFF SDIV"), invoke);

        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // SUB OP
    public void testSUB_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x04 PUSH1 0x06 SUB"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SUB OP
    public void testSUB_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x4444 PUSH2 0x6666 SUB"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000002222";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SUB OP
    public void testSUB_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x4444 PUSH4 0x99996666 SUB"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000099992222";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // SUB OP mal
    public void testSUB_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH4 0x99996666 SUB"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
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


    @Test // STOP OP
    public void testSTOP_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x20 PUSH1 0x30 PUSH1 0x10 PUSH1 0x30 PUSH1 0x11 PUSH1 0x23 STOP"), invoke);
        int expectedSteps = 7;

        int i = 0;
        while (!program.isStopped()) {

            vm.step(program);
            ++i;
        }
        assertEquals(expectedSteps, i);
    }

    @Ignore //TODO #POC9
    @Test // EXP OP
    public void testEXP_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x03 PUSH1 0x02 EXP"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000008";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        long gas = program.getResult().getGasUsed();

        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
        assertEquals(4, gas);
    }

    @Ignore //TODO #POC9
    @Test // EXP OP
    public void testEXP_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x00 PUSH3 0x123456 EXP"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        long gas = program.getResult().getGasUsed();

        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
        assertEquals(3, gas);
    }

    @Ignore //TODO #POC9
    @Test // EXP OP
    public void testEXP_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1122 PUSH1 0x01 EXP"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        long gas = program.getResult().getGasUsed();

        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
        assertEquals(5, gas);
    }


    @Test(expected = StackTooSmallException.class) // EXP OP mal
    public void testEXP_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH3 0x123456 EXP"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // RETURN OP
    public void testRETURN_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 RETURN"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // RETURN OP
    public void testRETURN_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x1F RETURN"), invoke);
        String s_expected_1 = "3400000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());
        assertTrue(program.isStopped());
    }

    @Test // RETURN OP
    public void testRETURN_3() {

        VM vm = new VM();
        program =
                new Program(compile
                        ("PUSH32 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 RETURN"),
                        invoke);
        String s_expected_1 = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // RETURN OP
    public void testRETURN_4() {

        VM vm = new VM();
        program =
                new Program(compile
                        ("PUSH32 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x10 RETURN"),
                        invoke);
        String s_expected_1 = "E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B100000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());
        assertTrue(program.isStopped());
    }

    @Ignore //TODO #POC9
    @Test // CODECOPY OP
    public void testCODECOPY_1() {

        VM vm = new VM();
        program =
                new Program(compile("PUSH1 0x03 PUSH1 0x07 PUSH1 0x00 CODECOPY SLT CALLVALUE JUMP"), invoke);
        String m_expected_1 = "1234560000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        long gas = program.getResult().getGasUsed();
        assertEquals(m_expected_1, Hex.toHexString(program.getMemory()).toUpperCase());
        assertEquals(6, gas);
    }

    @Ignore //TODO #POC9
    @Test // CODECOPY OP
    public void testCODECOPY_2() {

        VM vm = new VM();
        program =
                new Program(compile
                        ("PUSH1 0x5E PUSH1 0x07 PUSH1 0x00 CODECOPY PUSH1 0x00 PUSH1 0x5f SSTORE PUSH1 0x14 PUSH1 0x00 SLOAD PUSH1 0x1e PUSH1 0x20 SLOAD PUSH4 0xabcddcba PUSH1 0x40 SLOAD JUMPDEST MLOAD PUSH1 0x20 ADD PUSH1 0x0a MSTORE SLOAD MLOAD PUSH1 0x40 ADD PUSH1 0x14 MSTORE SLOAD MLOAD PUSH1 0x60 ADD PUSH1 0x1e MSTORE  SLOAD MLOAD PUSH1 0x80 ADD PUSH1 0x28 MSTORE SLOAD PUSH1 0xa0 MSTORE SLOAD PUSH1 0x16 PUSH1 0x48 PUSH1 0x00 CODECOPY PUSH1 0x16 PUSH1 0x00 CALLCODE PUSH1 0x00 PUSH1 0x3f SSTORE PUSH2 0x03e7 JUMP PUSH1 0x00 SLOAD PUSH1 0x00 MSTORE8 PUSH1 0x20 MUL CALLDATALOAD PUSH1 0x20 SLOAD"),
                        invoke);
        String m_expected_1 =
                "6000605F556014600054601E60205463ABCDDCBA6040545B51602001600A5254516040016014525451606001601E5254516080016028525460A052546016604860003960166000F26000603F556103E756600054600053602002356020540000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        long gas = program.getResult().getGasUsed();
        assertEquals(m_expected_1, Hex.toHexString(program.getMemory()).toUpperCase());
        assertEquals(10, gas);
    }

    @Ignore //TODO #POC9
    @Test // CODECOPY OP
    public void testCODECOPY_3() {

        // cost for that:
        // 94 - data copied
        // 95 - new bytes allocated

        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(10, program.getResult().getGasUsed());
    }

    @Ignore //TODO #POC9
    @Test // CODECOPY OP
    public void testCODECOPY_4() {

        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(10, program.getResult().getGasUsed());
    }


    @Test // CODECOPY OP
    public void testCODECOPY_5() {

        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("611234600054615566602054607060006020396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertFalse(program.isStopped());
    }


    @Test(expected = StackTooSmallException.class) // CODECOPY OP mal
    public void testCODECOPY_6() {

        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("605E6007396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_1() {

        VM vm = new VM();
        program =
                new Program(Hex.decode("60036007600073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C123456"), invoke);
        String m_expected_1 = "6000600000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected_1, Hex.toHexString(program.getMemory()).toUpperCase());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_2() {

        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("603E6007600073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235602054"),
                        invoke);
        String m_expected_1 =
                "6000605F556014600054601E60205463ABCDDCBA6040545B51602001600A5254516040016014525451606001601E5254516080016028525460A0525460160000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected_1, Hex.toHexString(program.getMemory()).toUpperCase());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_3() {
        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("605E6007600073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke);

        String m_expected_1 =
                "6000605F556014600054601E60205463ABCDDCBA6040545B51602001600A5254516040016014525451606001601E5254516080016028525460A052546016604860003960166000F26000603F556103E756600054600053602002350000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected_1, Hex.toHexString(program.getMemory()).toUpperCase());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_4() {
        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("611234600054615566602054603E6000602073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertFalse(program.isStopped());
    }


    @Test(expected = StackTooSmallException.class) // EXTCODECOPY OP mal
    public void testEXTCODECOPY_5() {
        VM vm = new VM();
        program =
                new Program(Hex.decode("605E600773471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C"),
                        invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }


    @Test // CODESIZE OP
    public void testCODESIZE_1() {

        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("385E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000062";

        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Ignore // todo: test is not testing EXTCODESIZE
    @Test // EXTCODESIZE OP
    public void testEXTCODESIZE_1() {
        VM vm = new VM();
        program =
                new Program(Hex.decode
                        ("73471FD3AD3E9EEADEEC4608B92D16CE6B500704CC395E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke); // Push address on the stack and perform EXTCODECOPY
        String s_expected_1 = "000000000000000000000000471FD3AD3E9EEADEEC4608B92D16CE6B500704CC";

        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MOD OP
    public void testMOD_1() {
        VM vm = new VM();
        program = new Program(compile("PUSH1 0x03 PUSH1 0x04 MOD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MOD OP
    public void testMOD_2() {
        VM vm = new VM();
        program = new Program(compile("PUSH2 0x012C PUSH2 0x01F4 MOD"), invoke);
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000000C8";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MOD OP
    public void testMOD_3() {
        VM vm = new VM();
        program = new Program(compile("PUSH1 0x04 PUSH1 0x02 MOD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // MOD OP mal
    public void testMOD_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH1 0x04 MOD"), invoke);

        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // SMOD OP
    public void testSMOD_1() {
        VM vm = new VM();
        program = new Program(compile("PUSH1 0x03 PUSH1 0x04 SMOD"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SMOD OP
    public void testSMOD_2() {
        VM vm = new VM();
        program = new Program(compile("PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE2 " + //  -30
                "PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "SMOD"), invoke);
        String s_expected_1 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEC";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SMOD OP
    public void testSMOD_3() {
        VM vm = new VM();
        program = new Program(compile("PUSH32 0x000000000000000000000000000000000000000000000000000000000000001E " + //   30
                "PUSH32 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56 " + // -170
                "SMOD"), invoke);
        String s_expected_1 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEC";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected = StackTooSmallException.class) // SMOD OP mal
    public void testSMOD_4() {
        VM vm = new VM();
        program = new Program(compile("PUSH32 0x000000000000000000000000000000000000000000000000000000000000001E " + //   30
                "SMOD"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test
    public void regression1Test() {
        // testing that we are working fine with unknown 0xFE bytecode produced by Serpent compiler
        String code2 = "60006116bf537c01000000000000000000000000000000000000000000000000000000006000350463b041b2858114156101c257600435604052780100000000000000000000000000000000000000000000000060606060599059016000905260028152604051816020015260008160400152809050205404606052606051151561008f57600060a052602060a0f35b66040000000000015460c052600760e0525b60605178010000000000000000000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540413156101b0575b60e05160050a60605178010000000000000000000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540403121561014457600060e05113610147565b60005b1561015a57600160e0510360e0526100ea565b7c010000000000000000000000000000000000000000000000000000000060e05160200260020a6060606059905901600090526002815260c051816020015260018160400152809050205402045460c0526100a1565b60405160c05114610160526020610160f35b63720f60f58114156102435760043561018052601c60445990590160009052016305215b2f601c820352790100000000000000000000000000000000000000000000000000600482015260206101c0602483600061018051602d5a03f1506101c05190506604000000000003556604000000000003546101e05260206101e0f35b63b8c48f8c8114156104325760043560c05260243561020052604435610220526000660400000000000254141515610286576000610240526020610240f3610292565b60016604000000000002555b60c0516604000000000001556060606059905901600090526002815260c051816020015260008160400152809050205461026052610260610200518060181a82538060191a600183015380601a1a600283015380601b1a600383015380601c1a600483015380601d1a600583015380601e1a600683015380601f1a60078301535050610260516060606059905901600090526002815260c05181602001526000816040015280905020556060606059905901600090526002815260c051816020015260008160400152809050205461030052601061030001610220518060101a82538060111a60018301538060121a60028301538060131a60038301538060141a60048301538060151a60058301538060161a60068301538060171a60078301538060181a60088301538060191a600983015380601a1a600a83015380601b1a600b83015380601c1a600c83015380601d1a600d83015380601e1a600e83015380601f1a600f8301535050610300516060606059905901600090526002815260c051816020015260008160400152809050205560016103a05260206103a0f35b632b861629811415610eed57365990590160009052366004823760043560208201016103e0525060483580601f1a6104405380601e1a6001610440015380601d1a6002610440015380601c1a6003610440015380601b1a6004610440015380601a1a600561044001538060191a600661044001538060181a600761044001538060171a600861044001538060161a600961044001538060151a600a61044001538060141a600b61044001538060131a600c61044001538060121a600d61044001538060111a600e61044001538060101a600f610440015380600f1a6010610440015380600e1a6011610440015380600d1a6012610440015380600c1a6013610440015380600b1a6014610440015380600a1a601561044001538060091a601661044001538060081a601761044001538060071a601861044001538060061a601961044001538060051a601a61044001538060041a601b61044001538060031a601c61044001538060021a601d61044001538060011a601e61044001538060001a601f6104400153506104405161040052700100000000000000000000000000000000700100000000000000000000000000000000606060605990590160009052600281526104005181602001526000816040015280905020540204610460526104605161061b57005b6103e05160208103516020599059016000905260208183856000600287604801f15080519050905090506104a0526020599059016000905260208160206104a0600060026068f1508051905080601f1a6105605380601e1a6001610560015380601d1a6002610560015380601c1a6003610560015380601b1a6004610560015380601a1a600561056001538060191a600661056001538060181a600761056001538060171a600861056001538060161a600961056001538060151a600a61056001538060141a600b61056001538060131a600c61056001538060121a600d61056001538060111a600e61056001538060101a600f610560015380600f1a6010610560015380600e1a6011610560015380600d1a6012610560015380600c1a6013610560015380600b1a6014610560015380600a1a601561056001538060091a601661056001538060081a601761056001538060071a601861056001538060061a601961056001538060051a601a61056001538060041a601b61056001538060031a601c61056001538060021a601d61056001538060011a601e61056001538060001a601f6105600153506105605160c0527001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540204610580526000610580511415156108345760006105c05260206105c0f35b608c3563010000008160031a02620100008260021a026101008360011a028360001a01010190506105e05263010000006105e051046106405262ffffff6105e0511661066052600361064051036101000a610660510261062052600060c05113156108a6576106205160c051126108a9565b60005b15610ee05760c05160c05160c051660400000000000054556060606059905901600090526002815260c0518160200152600081604001528090502054610680526008610680016604000000000000548060181a82538060191a600183015380601a1a600283015380601b1a600383015380601c1a600483015380601d1a600583015380601e1a600683015380601f1a60078301535050610680516060606059905901600090526002815260c05181602001526000816040015280905020556001660400000000000054016604000000000000556060606059905901600090526002815260c051816020015260008160400152809050205461072052610720600178010000000000000000000000000000000000000000000000006060606059905901600090526002815261040051816020015260008160400152809050205404018060181a82538060191a600183015380601a1a600283015380601b1a600383015380601c1a600483015380601d1a600583015380601e1a600683015380601f1a60078301535050610720516060606059905901600090526002815260c051816020015260008160400152809050205560006107e052780100000000000000000000000000000000000000000000000068010000000000000000606060605990590160009052600281526104005181602001526000816040015280905020540204610800526107e06108005180601c1a825380601d1a600183015380601e1a600283015380601f1a600383015350506001610880525b6008610880511215610c07576108805160050a6108a05260016108a05178010000000000000000000000000000000000000000000000006060606059905901600090526002815260c051816020015260008160400152809050205404071415610b7957610880516004026107e0016108005180601c1a825380601d1a600183015380601e1a600283015380601f1a60038301535050610bf7565b610880516004026107e0017c01000000000000000000000000000000000000000000000000000000006108805160200260020a60606060599059016000905260028152610400518160200152600181604001528090502054020480601c1a825380601d1a600183015380601e1a600283015380601f1a600383015350505b6001610880510161088052610adf565b6107e0516060606059905901600090526002815260c051816020015260018160400152809050205550506080608059905901600090526002815260c051816020015260028160400152600081606001528090502060005b6002811215610c8057806020026103e051015182820155600181019050610c5e565b700100000000000000000000000000000000600003816020026103e051015116828201555050610620517bffff000000000000000000000000000000000000000000000000000005610a00526060606059905901600090526002815260c0518160200152600081604001528090502054610a20526010610a2001610a005161046051018060101a82538060111a60018301538060121a60028301538060131a60038301538060141a60048301538060151a60058301538060161a60068301538060171a60078301538060181a60088301538060191a600983015380601a1a600a83015380601b1a600b83015380601c1a600c83015380601d1a600d83015380601e1a600e83015380601f1a600f8301535050610a20516060606059905901600090526002815260c05181602001526000816040015280905020557001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c051816020015260008160400152809050205402046105805266040000000000025461058051121515610e965760c05166040000000000015561058051660400000000000255601c606459905901600090520163c86a90fe601c8203526103e860048201523260248201526020610ae06044836000660400000000000354602d5a03f150610ae051905015610e95576103e8660400000000000454016604000000000004555b5b78010000000000000000000000000000000000000000000000006060606059905901600090526002815260c051816020015260008160400152809050205404610b00526020610b00f35b6000610b40526020610b40f35b63c6605beb811415611294573659905901600090523660048237600435610b6052602435610b80526044356020820101610ba0526064356040525067016345785d8a00003412151515610f47576000610bc0526020610bc0f35b601c6044599059016000905201633d73b705601c82035260405160048201526020610be0602483600030602d5a03f150610be05190508015610f895780610fc1565b601c604459905901600090520163b041b285601c82035260405160048201526020610c20602483600030602d5a03f150610c20519050155b905015610fd5576000610c40526020610c40f35b6060601c61014c59905901600090520163b7129afb601c820352610b60516004820152610b80516024820152610ba05160208103516020026020018360448401526020820360a4840152806101088401528084019350505081600401599059016000905260648160648460006004601cf161104c57fe5b6064810192506101088201518080858260a487015160006004600a8705601201f161107357fe5b508084019350508083036020610d008284600030602d5a03f150610d00519050905090509050610c60526080608059905901600090526002815260405181602001526002816040015260008160600152809050207c010000000000000000000000000000000000000000000000000000000060028201540464010000000060018301540201610d805250610d805180601f1a610de05380601e1a6001610de0015380601d1a6002610de0015380601c1a6003610de0015380601b1a6004610de0015380601a1a6005610de001538060191a6006610de001538060181a6007610de001538060171a6008610de001538060161a6009610de001538060151a600a610de001538060141a600b610de001538060131a600c610de001538060121a600d610de001538060111a600e610de001538060101a600f610de0015380600f1a6010610de0015380600e1a6011610de0015380600d1a6012610de0015380600c1a6013610de0015380600b1a6014610de0015380600a1a6015610de001538060091a6016610de001538060081a6017610de001538060071a6018610de001538060061a6019610de001538060051a601a610de001538060041a601b610de001538060031a601c610de001538060021a601d610de001538060011a601e610de001538060001a601f610de0015350610de051610d4052610d4051610c60511415611286576001610e00526020610e00f3611293565b6000610e20526020610e20f35b5b638f6b104c8114156115195736599059016000905236600482376004356020820101610e4052602435610b6052604435610b80526064356020820101610ba05260843560405260a435610e60525060016080601c6101ac59905901600090520163c6605beb601c820352610b60516004820152610b80516024820152610ba05160208103516020026020018360448401526020820360c48401528061014884015280840193505050604051606482015281600401599059016000905260848160848460006004601ff161136357fe5b6084810192506101488201518080858260c487015160006004600a8705601201f161138a57fe5b508084019350508083036020610e80828434306123555a03f150610e8051905090509050905014156114b3576040601c60ec59905901600090520163f0cf1ff4601c820352610e40516020601f6020830351010460200260200183600484015260208203604484015280608884015280840193505050610b60516024820152816004015990590160009052604481604484600060046018f161142857fe5b604481019250608882015180808582604487015160006004600a8705601201f161144e57fe5b508084019350508083036020610ec082846000610e6051602d5a03f150610ec0519050905090509050610ea0526040599059016000905260018152610ea051602082015260208101905033602082035160200282a150610ea051610f20526020610f20f35b604059905901600090526001815261270f600003602082015260208101905033602082035160200282a150604059905901600090526001815261270f6000036020820152602081019050610e6051602082035160200282a1506000610f80526020610f80f35b6309dd0e8181141561153957660400000000000154610fa0526020610fa0f35b630239487281141561159557780100000000000000000000000000000000000000000000000060606060599059016000905260028152660400000000000154816020015260008160400152809050205404610fc0526020610fc0f35b6361b919a68114156116045770010000000000000000000000000000000070010000000000000000000000000000000060606060599059016000905260028152660400000000000154816020015260008160400152809050205402046110005261100051611040526020611040f35b63a7cc63c28114156118b55766040000000000015460c0527001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540204611060526000610880525b600a610880511215611853576080608059905901600090526002815260c05181602001526002816040015260008160600152809050207c0100000000000000000000000000000000000000000000000000000000600182015404640100000000825402016110c052506110c05180601f1a6111205380601e1a6001611120015380601d1a6002611120015380601c1a6003611120015380601b1a6004611120015380601a1a600561112001538060191a600661112001538060181a600761112001538060171a600861112001538060161a600961112001538060151a600a61112001538060141a600b61112001538060131a600c61112001538060121a600d61112001538060111a600e61112001538060101a600f611120015380600f1a6010611120015380600e1a6011611120015380600d1a6012611120015380600c1a6013611120015380600b1a6014611120015380600a1a601561112001538060091a601661112001538060081a601761112001538060071a601861112001538060061a601961112001538060051a601a61112001538060041a601b61112001538060031a601c61112001538060021a601d61112001538060011a601e61112001538060001a601f6111200153506111205160c0526001610880510161088052611671565b7001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c0518160200152600081604001528090502054020461114052611140516110605103611180526020611180f35b63b7129afb811415611e35573659905901600090523660048237600435610b6052602435610b80526044356020820101610ba05250610b60516111a0526020610ba05103516111c0526000610880525b6111c051610880511215611e0c5761088051602002610ba05101516111e0526002610b805107611200526001611200511415611950576111e051611220526111a0516112405261196e565b600061120051141561196d576111a051611220526111e051611240525b5b604059905901600090526112205180601f1a6112805380601e1a6001611280015380601d1a6002611280015380601c1a6003611280015380601b1a6004611280015380601a1a600561128001538060191a600661128001538060181a600761128001538060171a600861128001538060161a600961128001538060151a600a61128001538060141a600b61128001538060131a600c61128001538060121a600d61128001538060111a600e61128001538060101a600f611280015380600f1a6010611280015380600e1a6011611280015380600d1a6012611280015380600c1a6013611280015380600b1a6014611280015380600a1a601561128001538060091a601661128001538060081a601761128001538060071a601861128001538060061a601961128001538060051a601a61128001538060041a601b61128001538060031a601c61128001538060021a601d61128001538060011a601e61128001538060001a601f6112800153506112805181526112405180601f1a6112e05380601e1a60016112e0015380601d1a60026112e0015380601c1a60036112e0015380601b1a60046112e0015380601a1a60056112e001538060191a60066112e001538060181a60076112e001538060171a60086112e001538060161a60096112e001538060151a600a6112e001538060141a600b6112e001538060131a600c6112e001538060121a600d6112e001538060111a600e6112e001538060101a600f6112e0015380600f1a60106112e0015380600e1a60116112e0015380600d1a60126112e0015380600c1a60136112e0015380600b1a60146112e0015380600a1a60156112e001538060091a60166112e001538060081a60176112e001538060071a60186112e001538060061a60196112e001538060051a601a6112e001538060041a601b6112e001538060031a601c6112e001538060021a601d6112e001538060011a601e6112e001538060001a601f6112e00153506112e051602082015260205990590160009052602081604084600060026088f1508051905061130052602059905901600090526020816020611300600060026068f1508051905080601f1a6113805380601e1a6001611380015380601d1a6002611380015380601c1a6003611380015380601b1a6004611380015380601a1a600561138001538060191a600661138001538060181a600761138001538060171a600861138001538060161a600961138001538060151a600a61138001538060141a600b61138001538060131a600c61138001538060121a600d61138001538060111a600e61138001538060101a600f611380015380600f1a6010611380015380600e1a6011611380015380600d1a6012611380015380600c1a6013611380015380600b1a6014611380015380600a1a601561138001538060091a601661138001538060081a601761138001538060071a601861138001538060061a601961138001538060051a601a61138001538060041a601b61138001538060031a601c61138001538060021a601d61138001538060011a601e61138001538060001a601f6113800153506113805190506111a0526002610b805105610b80526001610880510161088052611905565b6111a0511515611e265760016000036113a05260206113a0f35b6111a0516113c05260206113c0f35b633d73b7058114156120625760043560405266040000000000015460c0526000610880525b60066108805112156120555760c0516040511415611e7f5760016113e05260206113e0f35b6080608059905901600090526002815260c05181602001526002816040015260008160600152809050207c01000000000000000000000000000000000000000000000000000000006001820154046401000000008254020161142052506114205180601f1a6114805380601e1a6001611480015380601d1a6002611480015380601c1a6003611480015380601b1a6004611480015380601a1a600561148001538060191a600661148001538060181a600761148001538060171a600861148001538060161a600961148001538060151a600a61148001538060141a600b61148001538060131a600c61148001538060121a600d61148001538060111a600e61148001538060101a600f611480015380600f1a6010611480015380600e1a6011611480015380600d1a6012611480015380600c1a6013611480015380600b1a6014611480015380600a1a601561148001538060091a601661148001538060081a601761148001538060071a601861148001538060061a601961148001538060051a601a61148001538060041a601b61148001538060031a601c61148001538060021a601d61148001538060011a601e61148001538060001a601f6114800153506114805160c0526001610880510161088052611e5a565b60006114a05260206114a0f35b6391cf0e96811415612105576004356114c052601c60845990590160009052016367eae672601c8203523360048201526114c051602482015230604482015260206114e06064836000660400000000000354602d5a03f1506114e051905015612104576604000000000004546114c05130310205611500526114c0516604000000000004540366040000000000045560006000600060006115005133611388f1505b5b6313f955e18114156122985736599059016000905236600482376004356020820101611520526024356115405250605061156052600061158052611560516115a0526000610880525b611540516108805112156122895761158051806115a051038080602001599059016000905281815260208101905090508180828286611520510160006004600a8705601201f161219a57fe5b50809050905090506115c0526020601c608c599059016000905201632b861629601c8203526115c0516020601f6020830351010460200260200183600484015260208203602484015280604884015280840193505050816004015990590160009052602481602484600060046015f161220f57fe5b602481019250604882015180808582602487015160006004600a8705601201f161223557fe5b5080840193505080830360206116808284600030602d5a03f150611680519050905090509050610ea05261156051611580510161158052611560516115a051016115a052600161088051016108805261214e565b610ea0516116a05260206116a0f35b50";
        String result = Program.stringifyMultiline(Hex.decode(code2));
    }

    @Test
    public void regression2Test() {
        // testing that we are working fine with unknown 0xFE bytecode produced by Serpent compiler
        String code2 = "6060604052604051602080603f8339016040526060805190602001505b806000600050819055505b50600a8060356000396000f30060606040526008565b000000000000000000000000000000000000000000000000000000000000000021";
        String result = Program.stringifyMultiline(Hex.decode(code2));
        assertTrue(result.contains("00000000000000000000000000000000")); // detecting bynary data in bytecode
    }

    private byte[] compile(String code) {
        return new BytecodeCompiler().compile(code);
    }
}

// TODO: add gas expeted and calculated to all test cases
// TODO: considering: G_TXDATA + G_TRANSACTION

/**
 *   TODO:
 *
 *   22) CREATE:
 *   23) CALL:
 *
 *
 **/

/**

 contract creation (gas usage)
 -----------------------------
 G_TRANSACTION =                                (500)
 60016000546006601160003960066000f261778e600054 (115)
 PUSH1    6001 (1)
 PUSH1    6000 (1)
 MSTORE   54   (1 + 1)
 PUSH1    6006 (1)
 PUSH1    6011 (1)
 PUSH1    6000 (1)
 CODECOPY 39   (1)
 PUSH1    6006 (1)
 PUSH1    6000 (1)
 RETURN   f2   (1)
 61778e600054

 */
