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
 * Simple tests for VM Environmental Information
 */
public class VMEnvOpTest extends VMBaseOpTest {

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


    @Test(expected = Program.StackTooSmallException.class) // CODECOPY OP mal
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


    @Test(expected = Program.StackTooSmallException.class) // EXTCODECOPY OP mal
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
}
