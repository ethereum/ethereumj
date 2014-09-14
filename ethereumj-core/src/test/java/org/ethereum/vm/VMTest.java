package org.ethereum.vm;

import org.ethereum.facade.Repository;
import org.ethereum.vm.Program.OutOfGasException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/06/2014 11:05
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VMTest {

    @Test  // PUSH1 OP
    public void testPUSH1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60A0"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000000000000000A0";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH2 OP
    public void testPUSH2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61A0B0"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000000000000000000000000000A0B0";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH3 OP
    public void testPUSH3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("62A0B0C0"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000A0B0C0";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH4 OP
    public void testPUSH4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("63A0B0C0D0"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000000000A0B0C0D0";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH5 OP
    public void testPUSH5() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("64A0B0C0D0E0"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000000000000000000000A0B0C0D0E0";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH6 OP
    public void testPUSH6() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("65A0B0C0D0E0F0"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000A0B0C0D0E0F0";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH7 OP
    public void testPUSH7() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("66A0B0C0D0E0F0A1"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH8 OP
    public void testPUSH8() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("67A0B0C0D0E0F0A1B1"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH9 OP
    public void testPUSH9() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("68A0B0C0D0E0F0A1B1C1"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }


    @Test  // PUSH10 OP
    public void testPUSH10() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("69A0B0C0D0E0F0A1B1C1D1"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH11 OP
    public void testPUSH11() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6AA0B0C0D0E0F0A1B1C1D1E1"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH12 OP
    public void testPUSH12() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6BA0B0C0D0E0F0A1B1C1D1E1F1"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH13 OP
    public void testPUSH13() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6CA0B0C0D0E0F0A1B1C1D1E1F1A2"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH14 OP
    public void testPUSH14() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6DA0B0C0D0E0F0A1B1C1D1E1F1A2B2"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH15 OP
    public void testPUSH15() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6EA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH16 OP
    public void testPUSH16() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH17 OP
    public void testPUSH17() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("70A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH18 OP
    public void testPUSH18() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("71A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH19 OP
    public void testPUSH19() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("72A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH20 OP
    public void testPUSH20() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("73A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH21 OP
    public void testPUSH21() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("74A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH22 OP
    public void testPUSH22() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("75A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH23 OP
    public void testPUSH23() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("76A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH24 OP
    public void testPUSH24() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("77A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH25 OP
    public void testPUSH25() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("78A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4"), new ProgramInvokeMockImpl());
        String expected = "00000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH26 OP
    public void testPUSH26() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("79A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4"), new ProgramInvokeMockImpl());
        String expected = "000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH27 OP
    public void testPUSH27() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7AA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4"), new ProgramInvokeMockImpl());
        String expected = "0000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH28 OP
    public void testPUSH28() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7BA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4"), new ProgramInvokeMockImpl());
        String expected = "00000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH29 OP
    public void testPUSH29() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7CA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4"), new ProgramInvokeMockImpl());
        String expected = "000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH30 OP
    public void testPUSH30() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7DA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4"), new ProgramInvokeMockImpl());
        String expected = "0000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH31 OP
    public void testPUSH31() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7EA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1"), new ProgramInvokeMockImpl());
        String expected = "00A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH32 OP
    public void testPUSH32() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1"), new ProgramInvokeMockImpl());
        String expected = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        program.fullTrace();
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // PUSHN OP mal data
    public void testPUSHN_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61AA"), new ProgramInvokeMockImpl());

        try {
            program.fullTrace();
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test(expected=RuntimeException.class)  // PUSHN OP mal data
    public void testPUSHN_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7fAABB"), new ProgramInvokeMockImpl());

        try {
            program.fullTrace();
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // AND OP
    public void testAND_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600A600A10"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000000000000000000000000000000A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // AND OP
    public void testAND_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C0600A10"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // AND OP mal data
    public void testAND_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C010"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // OR OP
    public void testOR_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60F0600F11"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // OR OP
    public void testOR_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C3603C11"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // OR OP mal data
    public void testOR_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C011"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // XOR OP
    public void testXOR_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60FF60FF12"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // XOR OP
    public void testXOR_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600F60F012"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }


    @Test(expected=RuntimeException.class)  // XOR OP mal data
    public void testXOR_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C012"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // BYTE OP
    public void testBYTE_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEEFF601E13"), new ProgramInvokeMockImpl());
        String expected = "00000000000000000000000000000000000000000000000000000000000000EE";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // BYTE OP
    public void testBYTE_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEEFF602013"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // BYTE OP
    public void testBYTE_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEE3A601F13"), new ProgramInvokeMockImpl());
        String expected = "000000000000000000000000000000000000000000000000000000000000003A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }


    @Test(expected=RuntimeException.class)  // BYTE OP mal data
    public void testBYTE_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEE3A13"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // NOT OP
    public void testNOT_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60000F"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // NOT OP
    public void testNOT_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602A0F"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // NOT OP mal data
    public void testNOT_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("0F"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // EQ OP
    public void testEQ_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602A602A0E"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // EQ OP
    public void testEQ_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B4C622A3B4C0E"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // EQ OP
    public void testEQ_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B5C622A3B4C0E"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // EQ OP mal data
    public void testEQ_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B4C0E"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // GT OP
    public void testGT_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160020B"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6001610F000B"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6301020304610F000B"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // GT OP mal data
    public void testGT_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B4C0B"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // SGT OP
    public void testSGT_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160020D"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SGT OP
    public void testSGT_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "0D"), new ProgramInvokeMockImpl());

        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SGT OP
    public void testSGT_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF57" + // -169
                "0D"), new ProgramInvokeMockImpl());

        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // SGT OP mal
    public void testSGT_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "0D"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // LT OP
    public void testLT_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160020A"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6001610F000A"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6301020304610F000A"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // LT OP mal data
    public void testLT_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B4C0A"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // SLT OP
    public void testSLT_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160020C"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SLT OP
    public void testSLT_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "0C"), new ProgramInvokeMockImpl());

        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SLT OP
    public void testSLT_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF57" + // -169
                                                 "0C"), new ProgramInvokeMockImpl());

        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // SLT OP mal
    public void testSLT_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                "0D"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test  // NEG OP
    public void testNEG_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600109"), new ProgramInvokeMockImpl());
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61A00309"), new ProgramInvokeMockImpl());
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF5FFD";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61000009"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // NEG OP
    public void testNEG_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("09"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // POP OP
    public void testPOP_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61000060016200000250"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // POP OP
    public void testPOP_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6100006001620000025050"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class)  // POP OP mal data
    public void testPOP_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61000060016200000250505050"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // DUP OP
    public void testDUP_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("601251"), new ProgramInvokeMockImpl());
        String expected     = "0000000000000000000000000000000000000000000000000000000000000012";
        int    expectedLen  = 2;

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
        assertEquals(expectedLen, program.stack.toArray().length);
    }


    @Test(expected=RuntimeException.class)  // DUP OP mal data
    public void testDUP_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("51"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // SWAP OP
    public void testSWAP_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6011602252"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000000011";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // SWAP OP
    public void testSWAP_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60116022623333335252"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000333333";
        int    expectedLen  = 3;

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
        assertEquals(expectedLen, program.stack.toArray().length);
    }

    @Test // MSTORE OP
    public void testMSTORE_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("611234600054"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE OP
    public void testMSTORE_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("611234600054615566602054"), new ProgramInvokeMockImpl());
        String expected = "0000000000000000000000000000000000000000000000000000000000001234" +
                          "0000000000000000000000000000000000000000000000000000000000005566";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE OP
    public void testMSTORE_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("611234600054615566602054618888600054"), new ProgramInvokeMockImpl());
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

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE OP
    public void testMSTORE_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61123460A054"), new ProgramInvokeMockImpl());
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

        program.getResult().getRepository().close();
        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test(expected=RuntimeException.class) // MSTORE OP
    public void testMSTORE_5() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61123454"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // MLOAD OP
    public void testMLOAD_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600053"), new ProgramInvokeMockImpl());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602253"), new ProgramInvokeMockImpl());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                          "0000000000000000000000000000000000000000000000000000000000000000" +
                          "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // MLOAD OP
    public void testMLOAD_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602053"), new ProgramInvokeMockImpl());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("611234602054602053"), new ProgramInvokeMockImpl());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0000000000000000000000000000000000000000000000000000000000001234";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_5() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("611234602054601F53"), new ProgramInvokeMockImpl());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0000000000000000000000000000000000000000000000000000000000001234";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000012";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // MLOAD OP mal data
    public void testMLOAD_6() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("53"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // MSTORE8 OP
    public void testMSTORE8_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6011600055"), new ProgramInvokeMockImpl());
        String m_expected = "1100000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
    }


    @Test // MSTORE8 OP
    public void testMSTORE8_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6022600155"), new ProgramInvokeMockImpl());
        String m_expected = "0022000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE8 OP
    public void testMSTORE8_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6022602155"), new ProgramInvokeMockImpl());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0022000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
    }

    @Test(expected=RuntimeException.class) // MSTORE8 OP mal
    public void testMSTORE8_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602255"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // SSTORE OP
    public void testSSTORE_1() {

        VM vm = new VM();
        ProgramInvokeMockImpl invoke = new ProgramInvokeMockImpl();

        Program program = new Program(Hex.decode("602260AA57"), invoke);
        String s_expected_key = "00000000000000000000000000000000000000000000000000000000000000AA";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = program.result.getRepository().getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(), key);

        program.getResult().getRepository().close();
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // SSTORE OP
    public void testSSTORE_2() {

        VM vm = new VM();
        ProgramInvokeMockImpl invoke = new ProgramInvokeMockImpl();

        Program program = new Program(Hex.decode("602260AA57602260BB57"), invoke);
        String s_expected_key = "00000000000000000000000000000000000000000000000000000000000000BB";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        Repository repository = program.result.getRepository();
        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = repository.getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(),  key);

        program.getResult().getRepository().close();
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // SSTORE OP
    public void testSSTORE_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602257"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // SLOAD OP
    public void testSLOAD_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60AA56"), new ProgramInvokeMockImpl());
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // SLOAD OP
    public void testSLOAD_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602260AA5760AA56"), new ProgramInvokeMockImpl());
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // SLOAD OP
    public void testSLOAD_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602260AA57603360CC5760CC56"), new ProgramInvokeMockImpl());
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000033";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // SLOAD OP
    public void testSLOAD_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("56"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // PC OP
    public void testPC_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("5A"), new ProgramInvokeMockImpl());
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // PC OP
    public void testPC_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("602260AA5760AA565A"), new ProgramInvokeMockImpl());
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000008";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // JUMP OP
    public void testJUMP_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60AA60BB600D5860CC60DD60EE60FF"), new ProgramInvokeMockImpl());
        String s_expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // JUMP OP mal data
    public void testJUMP_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600C5860CC60DD60EE60FF"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // JUMPI OP
    public void testJUMPI_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160055960CC"), new ProgramInvokeMockImpl());
        String s_expected = "00000000000000000000000000000000000000000000000000000000000000CC";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // JUMPI OP
    public void testJUMPI_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("630000000060445960CC60DD"), new ProgramInvokeMockImpl());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000000DD";
        String s_expected_2 = "00000000000000000000000000000000000000000000000000000000000000CC";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        DataWord item2 = program.stack.pop();

        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
        assertEquals(s_expected_2, Hex.toHexString(item2.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // JUMPI OP mal
    public void testJUMPI_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600159"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test(expected=RuntimeException.class) // JUMPI OP mal
    public void testJUMPI_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6001602259"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // ADD OP mal
    public void testADD_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6002600201"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000004";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ADD OP
    public void testADD_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("611002600201"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000001004";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ADD OP
    public void testADD_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6110026512345678900901"), new ProgramInvokeMockImpl());
        String s_expected_1 = "000000000000000000000000000000000000000000000000000012345678A00B";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // ADD OP mal
    public void testADD_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61123401"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // MULL OP
    public void testMULL_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6003600202"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000006";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MULL OP
    public void testMULL_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("62222222600302"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000666666";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MULL OP
    public void testMULL_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("622222226233333302"), new ProgramInvokeMockImpl());
        String s_expected_1 = "000000000000000000000000000000000000000000000000000006D3A05F92C6";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // MULL OP mal
    public void testMULL_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600102"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // DIV OP
    public void testDIV_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6002600404"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // DIV OP
    public void testDIV_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6033609904"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000003";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // DIV OP
    public void testDIV_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6022609904"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000004";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // DIV OP
    public void testDIV_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6015609904"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000007";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // DIV OP
    public void testDIV_5() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6004600704"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // DIV OP
    public void testDIV_6() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600704"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // SDIV OP
    public void testSDIV_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6103E87FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC1805"), new ProgramInvokeMockImpl());
        String s_expected_1 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SDIV OP
    public void testSDIV_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60FF60FF05"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SDIV OP
    public void testSDIV_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600060FF05"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // SDIV OP mal
    public void testSDIV_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60FF05"), new ProgramInvokeMockImpl());

        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // SUB OP
    public void testSUB_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6004600603"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
        program.getResult().getRepository().close();
    }

    @Test // SUB OP
    public void testSUB_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61444461666603"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000002222";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SUB OP
    public void testSUB_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("614444639999666603"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000099992222";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // SUB OP mal
    public void testSUB_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("639999666603"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // MSIZE OP
    public void testMSIZE_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("5B"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MSIZE OP
    public void testMSIZE_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60206030545B"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000060";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // STOP OP
    public void testSTOP_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60206030601060306011602300"), new ProgramInvokeMockImpl());
        int expectedSteps = 7;

        int i = 0;
        while (!program.isStopped()) {

            vm.step(program);
            ++i;
        }
        program.getResult().getRepository().close();
        assertEquals(expectedSteps, i);
    }


    @Test // EXP OP
    public void testEXP_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6003600208"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000008";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // EXP OP
    public void testEXP_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("60006212345608"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // EXP OP mal
    public void testEXP_3() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6212345608"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // RETURN OP
    public void testRETURN_1() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("61123460005460206000F2"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // RETURN OP
    public void testRETURN_2() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("6112346000546020601FF2"), new ProgramInvokeMockImpl());
        String s_expected_1 = "3400000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }

    @Test // RETURN OP
    public void testRETURN_3() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B160005460206000F2"),
                        new ProgramInvokeMockImpl());
        String s_expected_1 = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // RETURN OP
    public void testRETURN_4() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B160005460206010F2"),
                        new ProgramInvokeMockImpl());
        String s_expected_1 = "E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B100000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // CODECOPY OP
    public void testCODECOPY_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60036007600039123456"), new ProgramInvokeMockImpl());
        String m_expected_1 = "1234560000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected_1, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // CODECOPY OP
    public void testCODECOPY_2() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235602054"),
                        new ProgramInvokeMockImpl());
        String m_expected_1 = "6000605F556014600054601E60205463ABCDDCBA6040545B51602001600A5254516040016014525451606001601E5254516080016028525460A052546016604860003960166000F26000603F556103E756600054600053602002356020540000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected_1, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // CODECOPY OP
    public void testCODECOPY_3() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        new ProgramInvokeMockImpl());

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertTrue(program.isStopped());
    }

    @Test // CODECOPY OP
    public void testCODECOPY_4() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        new ProgramInvokeMockImpl());

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertTrue(program.isStopped());
    }

//    DataWord memOffsetData
//    DataWord codeOffsetData
//    DataWord lengthData


    @Test // CODECOPY OP
    public void testCODECOPY_5() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("611234600054615566602054607060006020396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        new ProgramInvokeMockImpl());

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

        program.getResult().getRepository().close();
        assertFalse(program.isStopped());
    }


    @Test(expected=RuntimeException.class) // CODECOPY OP mal
    public void testCODECOPY_6() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("605E6007396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
        	program.getResult().getRepository().close();
        	assertTrue(program.isStopped());
        }
    }

    @Test // CODESIZE OP
    public void testCODESIZE_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("385E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000062";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // CALLDATASIZE OP
    public void testCALLDATASIZE_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("36"),
                        createProgramInvoke_1());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000040";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // CALLDATALOAD OP
    public void testCALLDATALOAD_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("600035"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000000A1";

        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // CALLDATALOAD OP
    public void testCALLDATALOAD_2() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("600235"),
                        createProgramInvoke_1());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000A10000";

        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // CALLDATALOAD OP
    public void testCALLDATALOAD_3() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("602035"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000000B1";

        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }


    @Test // CALLDATALOAD OP
    public void testCALLDATALOAD_4() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("602335"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000B1000000";

        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // CALLDATALOAD OP
    public void testCALLDATALOAD_5() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("603F35"),
                        createProgramInvoke_1());
        String s_expected_1 = "B100000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // CALLDATALOAD OP mal
    public void testCALLDATALOAD_6() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("35"),
                        createProgramInvoke_1());
        try {
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // CALLDATACOPY OP
    public void testCALLDATACOPY_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60206000600037"),
                        createProgramInvoke_1());
        String m_expected = "00000000000000000000000000000000000000000000000000000000000000A1";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // CALLDATACOPY OP
    public void testCALLDATACOPY_2() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60406000600037"),
                        createProgramInvoke_1());
        String m_expected = "00000000000000000000000000000000000000000000000000000000000000A1" +
                            "00000000000000000000000000000000000000000000000000000000000000B1";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
    }


    @Test // CALLDATACOPY OP
    public void testCALLDATACOPY_3() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60406004600037"),
                        createProgramInvoke_1());
        String m_expected = "000000000000000000000000000000000000000000000000000000A100000000" +
                            "000000000000000000000000000000000000000000000000000000B100000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
    }


    @Test // CALLDATACOPY OP
    public void testCALLDATACOPY_4() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60406000600437"),
                        createProgramInvoke_1());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "000000A100000000000000000000000000000000000000000000000000000000" +
                            "000000B100000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // CALLDATACOPY OP
    public void testCALLDATACOPY_5() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60406000600437"),
                        createProgramInvoke_1());
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "000000A100000000000000000000000000000000000000000000000000000000" +
                            "000000B100000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        program.getResult().getRepository().close();
        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
    }


    @Test(expected=RuntimeException.class) // CALLDATACOPY OP mal
    public void testCALLDATACOPY_6() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("6040600037"),
                        createProgramInvoke_1());

        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test(expected=OutOfGasException.class) // CALLDATACOPY OP mal
    public void testCALLDATACOPY_7() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("6020600073CC0929EB16730E7C14FEFC63006AC2D794C5795637"),
                        createProgramInvoke_1());

        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }
    
    @Test // ADDRESS OP
    public void testADDRESS_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("30"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000077045E71A7A2C50903D88E564CD72FAB11E82051";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // BALANCE OP
    public void testBALANCE_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("3031"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000003E8";

        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // ORIGIN OP
    public void testORIGIN_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("32"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000013978AEE95F38490E9769C39B2773ED763D9CD5F";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // CALLER OP
    public void testCALLER_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("33"),
                        createProgramInvoke_1());
        String s_expected_1 = "000000000000000000000000885F93EED577F2FC341EBB9A5C9B2CE4465D96C4";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // CALLVALUE OP
    public void testCALLVALUE_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("34"),
                        createProgramInvoke_1());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000DE0B6B3A7640000";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SHA3 OP
    public void testSHA3_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("60016000556001600020"),
                        createProgramInvoke_1());
        String s_expected_1 = "5FE7F977E71DBA2EA1A68E21057BEEBB9BE2AC30C6410AA38D4F3FBE41DCFFD2";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SHA3 OP
    public void testSHA3_2() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("6102016000546002601E20"),
                        createProgramInvoke_1());
        String s_expected_1 = "114A3FE82A0219FCC31ABD15617966A125F12B0FD3409105FC83B487A9D82DE4";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // SHA3 OP mal
    public void testSHA3_3() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("610201600054600220"),
                        createProgramInvoke_1());
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // MOD OP
    public void testMOD_1() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("6003600406"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MOD OP
    public void testMOD_2() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("61012C6101F406"), new ProgramInvokeMockImpl());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000000C8";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MOD OP
    public void testMOD_3() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("6004600206"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // MOD OP mal
    public void testMOD_4() {

        VM vm = new VM();
        Program program = new Program(Hex.decode("600406"), new ProgramInvokeMockImpl());

        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // SMOD OP
    public void testSMOD_1() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("6003600407"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SMOD OP
    public void testSMOD_2() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE2" + //  -30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "07"), new ProgramInvokeMockImpl());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // SMOD OP
    public void testSMOD_3() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "07"), new ProgramInvokeMockImpl());
        String s_expected_1 = "000000000000000000000000000000000000000000000000000000000000000A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=RuntimeException.class) // SMOD OP mal
    public void testSMOD_4() {
        VM vm = new VM();
        Program program = new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                "07"), new ProgramInvokeMockImpl());
        try {
            vm.step(program);
            vm.step(program);
            fail();
        } finally {
            program.getResult().getRepository().close();
            assertTrue(program.isStopped());
        }
    }

    @Test // PREVHASH OP
    public void testPREVHASH_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("40"),
                        createProgramInvoke_1());
        String s_expected_1 = "961CB117ABA86D1E596854015A1483323F18883C2D745B0BC03E87F146D2BB1C";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // COINBASE OP
    public void testCOINBASE_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("41"),
                        createProgramInvoke_1());
        String s_expected_1 = "000000000000000000000000E559DE5527492BCB42EC68D07DF0742A98EC3F1E";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // TIMESTAMP OP
    public void testTIMESTAMP_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("42"),
                        createProgramInvoke_1());
        String s_expected_1 = "000000000000000000000000000000000000000000000000000000005387FE24";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // NUMBER OP
    public void testNUMBER_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("43"),
                        createProgramInvoke_1());
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000021";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // DIFFICULTY OP
    public void testDIFFICULTY_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("44"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000003ED290";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // GASPRICE OP
    public void testGASPRICE_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("3A"),
                        createProgramInvoke_1());
        String s_expected_1 = "000000000000000000000000000000000000000000000000000009184E72A000";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // GAS OP
    public void testGAS_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("5C"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000F423F";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // GASLIMIT OP
    public void testGASLIMIT_1() {

        VM vm = new VM();
        Program program =
                new Program(Hex.decode("45"),
                        createProgramInvoke_1());
        String s_expected_1 = "00000000000000000000000000000000000000000000000000000000000F4240";

        vm.step(program);

        DataWord item1 = program.stack.pop();
        program.getResult().getRepository().close();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    /* TEST CASE LIST END */

    public ProgramInvoke createProgramInvoke_1() {

        String ownerAddress = "77045E71A7A2C50903D88E564CD72FAB11E82051";
        byte[] address = Hex.decode(ownerAddress);

        byte[] msgData = Hex.decode("00000000000000000000000000000000000000000000000000000000000000A1" +
                                    "00000000000000000000000000000000000000000000000000000000000000B1");

        ProgramInvokeMockImpl pi = new ProgramInvokeMockImpl(msgData);
        pi.setOwnerAddress(ownerAddress);

        pi.getRepository().createAccount(address);
        pi.getRepository().addBalance(address, BigInteger.valueOf(1000L));

        return pi;
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
