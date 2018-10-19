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
import org.ethereum.vm.program.Program;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple tests for VM Bitwise Logic & Comparison Operations
 */
public class VMBitOpTest extends VMBaseOpTest {
    private static final SystemProperties constantinopleConfig = new SystemProperties(){{
        setBlockchainConfig(new ConstantinopleConfig(new DaoHFConfig()));
    }};

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


    @Test(expected = Program.StackTooSmallException.class)  // BYTE OP mal data
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

    @Test  // SHL OP
    public void testSHL_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0x00 SHL"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0x01 SHL"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0xff SHL"), invoke, constantinopleConfig);
        String expected = "8000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH2 0x0100 SHL"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH2 0x0101 SHL"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_6() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x00 SHL"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_7() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x01 SHL"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_8() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0xff SHL"), invoke, constantinopleConfig);
        String expected = "8000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_9() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH2 0x0100 SHL"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_10() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000000 PUSH1 0x01 SHL"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHL OP
    public void testSHL_11() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x01 SHL"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0x00 SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0x01 SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH1 0x01 SHR"), invoke, constantinopleConfig);
        String expected = "4000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH1 0xff SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH2 0x0100 SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_6() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH2 0x0101 SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_7() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x00 SHR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_8() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x01 SHR"), invoke, constantinopleConfig);
        String expected = "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_9() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0xff SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_10() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH2 0x0100 SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SHR OP
    public void testSHR_11() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000000 PUSH1 0x01 SHR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }
    @Test  // SAR OP
    public void testSAR_1() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0x00 SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_2() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000001 PUSH1 0x01 SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_3() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH1 0x01 SAR"), invoke, constantinopleConfig);
        String expected = "C000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_4() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH1 0xff SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_5() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH2 0x0100 SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_6() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x8000000000000000000000000000000000000000000000000000000000000000 PUSH2 0x0101 SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_7() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x00 SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_8() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0x01 SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_9() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0xff SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_10() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH2 0x0100 SAR"), invoke, constantinopleConfig);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_11() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x0000000000000000000000000000000000000000000000000000000000000000 PUSH1 0x01 SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_12() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x4000000000000000000000000000000000000000000000000000000000000000 PUSH1 0xfe SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_13() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0xf8 SAR"), invoke, constantinopleConfig);
        String expected = "000000000000000000000000000000000000000000000000000000000000007F";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_14() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0xfe SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_15() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH1 0xff SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
    }

    @Test  // SAR OP
    public void testSAR_16() {

        VM vm = new VM();
        program = new Program(compile("PUSH32 0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff PUSH2 0x0100 SAR"), invoke, constantinopleConfig);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.getStack().peek().getData()).toUpperCase());
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

    @Test(expected = Program.StackTooSmallException.class)  // ISZERO OP mal data
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

    @Test(expected = Program.StackTooSmallException.class)  // EQ OP mal data
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

    @Test(expected = Program.StackTooSmallException.class)  // GT OP mal data
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

    @Test(expected = Program.StackTooSmallException.class)  // SGT OP mal
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

    @Test(expected = Program.StackTooSmallException.class)  // LT OP mal data
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

    @Test(expected = Program.StackTooSmallException.class)  // SLT OP mal
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


    @Test(expected = Program.StackTooSmallException.class)  // BNOT OP
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
}
