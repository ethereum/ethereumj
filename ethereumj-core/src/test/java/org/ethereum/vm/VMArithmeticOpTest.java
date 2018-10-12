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

import org.ethereum.vm.program.Program;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Simple tests for VM Arithmetic Operations
 */
public class VMArithmeticOpTest extends VMBaseOpTest {

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

    @Test(expected = Program.StackTooSmallException.class) // ADD OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // ADDMOD OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // MUL OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // MULMOD OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // DIV OP
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

    @Test(expected = Program.StackTooSmallException.class) // SDIV OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // SUB OP mal
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


    @Test(expected = Program.StackTooSmallException.class) // EXP OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // MOD OP mal
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

    @Test(expected = Program.StackTooSmallException.class) // SMOD OP mal
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
}
