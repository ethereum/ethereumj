package org.ethereum.vm;

import org.ethereum.facade.Repository;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.Program.StackTooSmallException;
import org.ethereum.vm.VM.BadJumpDestinationException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 01/06/2014 11:05
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VMTest {
	
	private ProgramInvoke invoke;
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
        program = new Program(Hex.decode("60A0"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000A0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH2 OP
    public void testPUSH2() {

        VM vm = new VM();
        program = new Program(Hex.decode("61A0B0"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000A0B0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH3 OP
    public void testPUSH3() {

        VM vm = new VM();
        program = new Program(Hex.decode("62A0B0C0"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000A0B0C0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH4 OP
    public void testPUSH4() {

        VM vm = new VM();
        program = new Program(Hex.decode("63A0B0C0D0"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000A0B0C0D0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH5 OP
    public void testPUSH5() {

        VM vm = new VM();
        program = new Program(Hex.decode("64A0B0C0D0E0"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000A0B0C0D0E0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH6 OP
    public void testPUSH6() {

        VM vm = new VM();
        program = new Program(Hex.decode("65A0B0C0D0E0F0"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000A0B0C0D0E0F0";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH7 OP
    public void testPUSH7() {

        VM vm = new VM();
        program = new Program(Hex.decode("66A0B0C0D0E0F0A1"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH8 OP
    public void testPUSH8() {

        VM vm = new VM();
        program = new Program(Hex.decode("67A0B0C0D0E0F0A1B1"), invoke);
        String expected = "000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH9 OP
    public void testPUSH9() {

        VM vm = new VM();
        program = new Program(Hex.decode("68A0B0C0D0E0F0A1B1C1"), invoke);
        String expected = "0000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }


    @Test  // PUSH10 OP
    public void testPUSH10() {

        VM vm = new VM();
        program = new Program(Hex.decode("69A0B0C0D0E0F0A1B1C1D1"), invoke);
        String expected = "00000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH11 OP
    public void testPUSH11() {

        VM vm = new VM();
        program = new Program(Hex.decode("6AA0B0C0D0E0F0A1B1C1D1E1"), invoke);
        String expected = "000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH12 OP
    public void testPUSH12() {

        VM vm = new VM();
        program = new Program(Hex.decode("6BA0B0C0D0E0F0A1B1C1D1E1F1"), invoke);
        String expected = "0000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH13 OP
    public void testPUSH13() {

        VM vm = new VM();
        program = new Program(Hex.decode("6CA0B0C0D0E0F0A1B1C1D1E1F1A2"), invoke);
        String expected = "00000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH14 OP
    public void testPUSH14() {

        VM vm = new VM();
        program = new Program(Hex.decode("6DA0B0C0D0E0F0A1B1C1D1E1F1A2B2"), invoke);
        String expected = "000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH15 OP
    public void testPUSH15() {

        VM vm = new VM();
        program = new Program(Hex.decode("6EA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2"), invoke);
        String expected = "0000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH16 OP
    public void testPUSH16() {

        VM vm = new VM();
        program = new Program(Hex.decode("6FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2"), invoke);
        String expected = "00000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH17 OP
    public void testPUSH17() {

        VM vm = new VM();
        program = new Program(Hex.decode("70A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2"), invoke);
        String expected = "000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH18 OP
    public void testPUSH18() {

        VM vm = new VM();
        program = new Program(Hex.decode("71A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2"), invoke);
        String expected = "0000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH19 OP
    public void testPUSH19() {

        VM vm = new VM();
        program = new Program(Hex.decode("72A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3"), invoke);
        String expected = "00000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH20 OP
    public void testPUSH20() {

        VM vm = new VM();
        program = new Program(Hex.decode("73A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3"), invoke);
        String expected = "000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH21 OP
    public void testPUSH21() {

        VM vm = new VM();
        program = new Program(Hex.decode("74A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3"), invoke);
        String expected = "0000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH22 OP
    public void testPUSH22() {

        VM vm = new VM();
        program = new Program(Hex.decode("75A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3"), invoke);
        String expected = "00000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH23 OP
    public void testPUSH23() {

        VM vm = new VM();
        program = new Program(Hex.decode("76A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3"), invoke);
        String expected = "000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH24 OP
    public void testPUSH24() {

        VM vm = new VM();
        program = new Program(Hex.decode("77A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3"), invoke);
        String expected = "0000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH25 OP
    public void testPUSH25() {

        VM vm = new VM();
        program = new Program(Hex.decode("78A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4"), invoke);
        String expected = "00000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH26 OP
    public void testPUSH26() {

        VM vm = new VM();
        program = new Program(Hex.decode("79A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4"), invoke);
        String expected = "000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH27 OP
    public void testPUSH27() {

        VM vm = new VM();
        program = new Program(Hex.decode("7AA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4"), invoke);
        String expected = "0000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH28 OP
    public void testPUSH28() {

        VM vm = new VM();
        program = new Program(Hex.decode("7BA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4"), invoke);
        String expected = "00000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH29 OP
    public void testPUSH29() {

        VM vm = new VM();
        program = new Program(Hex.decode("7CA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4"), invoke);
        String expected = "000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH30 OP
    public void testPUSH30() {

        VM vm = new VM();
        program = new Program(Hex.decode("7DA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4"), invoke);
        String expected = "0000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH31 OP
    public void testPUSH31() {

        VM vm = new VM();
        program = new Program(Hex.decode("7EA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1"), invoke);
        String expected = "00A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // PUSH32 OP
    public void testPUSH32() {

        VM vm = new VM();
        program = new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1"), invoke);
        String expected = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        program.fullTrace();
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // PUSHN OP mal data
    public void testPUSHN_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61AA"), invoke);

        try {
            program.fullTrace();
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test(expected=RuntimeException.class)  // PUSHN OP mal data
    public void testPUSHN_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7fAABB"), invoke);

        try {
            program.fullTrace();
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // AND OP
    public void testAND_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600A600A10"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000000A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // AND OP
    public void testAND_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60C0600A10"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // AND OP mal data
    public void testAND_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60C010"), invoke);
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
        program =  new Program(Hex.decode("60F0600F11"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // OR OP
    public void testOR_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60C3603C11"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=RuntimeException.class)  // OR OP mal data
    public void testOR_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60C011"), invoke);
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
        program =  new Program(Hex.decode("60FF60FF12"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // XOR OP
    public void testXOR_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600F60F012"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }


    @Test(expected=RuntimeException.class)  // XOR OP mal data
    public void testXOR_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60C012"), invoke);
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
        program =  new Program(Hex.decode("65AABBCCDDEEFF601E13"), invoke);
        String expected = "00000000000000000000000000000000000000000000000000000000000000EE";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // BYTE OP
    public void testBYTE_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("65AABBCCDDEEFF602013"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // BYTE OP
    public void testBYTE_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("65AABBCCDDEE3A601F13"), invoke);
        String expected = "000000000000000000000000000000000000000000000000000000000000003A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }


    @Test(expected=StackTooSmallException.class)  // BYTE OP mal data
    public void testBYTE_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("65AABBCCDDEE3A13"), invoke);
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
        program =  new Program(Hex.decode("60000F"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // NOT OP
    public void testNOT_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602A0F"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test(expected=StackTooSmallException.class)  // NOT OP mal data
    public void testNOT_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("0F"), invoke);
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
        program =  new Program(Hex.decode("602A602A0E"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // EQ OP
    public void testEQ_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("622A3B4C622A3B4C0E"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase()  );
    }

    @Test  // EQ OP
    public void testEQ_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("622A3B5C622A3B4C0E"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // EQ OP mal data
    public void testEQ_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("622A3B4C0E"), invoke);
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
        program =  new Program(Hex.decode("600160020B"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6001610F000B"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6301020304610F000B"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // GT OP mal data
    public void testGT_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("622A3B4C0B"), invoke);
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
        program =  new Program(Hex.decode("600160020D"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SGT OP
    public void testSGT_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "0D"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SGT OP
    public void testSGT_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF57" + // -169
                "0D"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // SGT OP mal
    public void testSGT_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "0D"), invoke);
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
        program =  new Program(Hex.decode("600160020A"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6001610F000A"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6301020304610F000A"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // LT OP mal data
    public void testLT_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("622A3B4C0A"), invoke);
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
        program =  new Program(Hex.decode("600160020C"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SLT OP
    public void testSLT_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "0C"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // SLT OP
    public void testSLT_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF57" + // -169
                                                 "0C"), invoke);

        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // SLT OP mal
    public void testSLT_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                "0D"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test  // NEG OP
    public void testNEG_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600109"), invoke);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61A00309"), invoke);
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF5FFD";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61000009"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // NEG OP
    public void testNEG_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("09"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // POP OP
    public void testPOP_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61000060016200000250"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // POP OP
    public void testPOP_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6100006001620000025050"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // POP OP mal data
    public void testPOP_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61000060016200000250505050"), invoke);
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
        byte operation = (byte) (OpCode.DUP1.val() + n - 1);
        String programCode = "";
        for (int i = 0; i < n; i++) {
			programCode += "60" + (12 + i);
		}
        program =  new Program(ByteUtil.appendByte(Hex.decode(programCode.getBytes()), operation), invoke);
        String expected     = "0000000000000000000000000000000000000000000000000000000000000012";
        int    expectedLen  = n + 1;

        for (int i = 0; i < expectedLen; i++) {
        	vm.step(program);
		}

        assertEquals(expectedLen, program.stack.toArray().length);
        assertEquals(expected, Hex.toHexString(program.stackPop().getData()).toUpperCase());
        for (int i = 0; i < expectedLen-2; i++) {
        	assertNotEquals(expected, Hex.toHexString(program.stackPop().getData()).toUpperCase());
		}
        assertEquals(expected, Hex.toHexString(program.stackPop().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // DUPN OP mal data
    public void testDUPN_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("80"), invoke);
        try {
        	vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }
    
    @Test // SWAP1...SWAP16 OP
    public void testSWAPS() {
    	for (int i = 1; i < 17; i++) {
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
        byte operation = (byte) (OpCode.SWAP1.val() + n - 1);
        String[] expected = new String[n + 1];
        String programCode = "";
        for (int i = 0; i < expected.length; i++) {
        	programCode += "60" + (11+i);
        	expected[i] = "00000000000000000000000000000000000000000000000000000000000000" + (11+i);
		}
        program =  new Program(ByteUtil.appendByte(Hex.decode(programCode), operation), invoke);

		for (int i = 0; i <= expected.length; i++) {
			vm.step(program);
		}
		
		assertEquals(expected.length, program.stack.toArray().length);
		assertEquals(expected[0], Hex.toHexString(program.stackPop().getData()).toUpperCase());
		for (int i = expected.length-2; i > 0; i--) {
			assertEquals(expected[i], Hex.toHexString(program.stackPop().getData()).toUpperCase());
		}
		assertEquals(expected[expected.length-1], Hex.toHexString(program.stackPop().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class)  // SWAPN OP mal data
    public void testSWAPN_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("90"), invoke);

        try {
        	vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MSTORE OP
    public void testMSTORE_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("611234600054"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE OP
    public void testMSTORE_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("611234600054615566602054"), invoke);
        String expected = "0000000000000000000000000000000000000000000000000000000000001234" +
                          "0000000000000000000000000000000000000000000000000000000000005566";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE OP
    public void testMSTORE_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("611234600054615566602054618888600054"), invoke);
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

        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE OP
    public void testMSTORE_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61123460A054"), invoke);
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

        assertEquals(expected, Hex.toHexString(program.memory.array()));
    }

    @Test(expected=StackTooSmallException.class) // MSTORE OP
    public void testMSTORE_5() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61123454"), invoke);
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
        program =  new Program(Hex.decode("600053"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602253"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                          "0000000000000000000000000000000000000000000000000000000000000000" +
                          "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()).toUpperCase());
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // MLOAD OP
    public void testMLOAD_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602053"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0000000000000000000000000000000000000000000000000000000000000000";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("611234602054602053"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0000000000000000000000000000000000000000000000000000000000001234";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // MLOAD OP
    public void testMLOAD_5() {

        VM vm = new VM();
        program =  new Program(Hex.decode("611234602054601F53"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0000000000000000000000000000000000000000000000000000000000001234";
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000012";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // MLOAD OP mal data
    public void testMLOAD_6() {

        VM vm = new VM();
        program =  new Program(Hex.decode("53"), invoke);
        try {
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // MSTORE8 OP
    public void testMSTORE8_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6011600055"), invoke);
        String m_expected = "1100000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
    }


    @Test // MSTORE8 OP
    public void testMSTORE8_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6022600155"), invoke);
        String m_expected = "0022000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
    }

    @Test // MSTORE8 OP
    public void testMSTORE8_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6022602155"), invoke);
        String m_expected = "0000000000000000000000000000000000000000000000000000000000000000" +
                            "0022000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected, Hex.toHexString(program.memory.array()));
    }

    @Test(expected=StackTooSmallException.class) // MSTORE8 OP mal
    public void testMSTORE8_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602255"), invoke);
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

        program =  new Program(Hex.decode("602260AA57"), invoke);
        String s_expected_key = "00000000000000000000000000000000000000000000000000000000000000AA";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = program.result.getRepository().getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(), key);

        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // SSTORE OP
    public void testSSTORE_2() {

        VM vm = new VM();

        program =  new Program(Hex.decode("602260AA57602260BB57"), invoke);
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

        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // SSTORE OP
    public void testSSTORE_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602257"), invoke);
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
        program =  new Program(Hex.decode("60AA56"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // SLOAD OP
    public void testSLOAD_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602260AA5760AA56"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000022";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // SLOAD OP
    public void testSLOAD_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602260AA57603360CC5760CC56"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000033";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // SLOAD OP
    public void testSLOAD_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("56"), invoke);
        try {
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // PC OP
    public void testPC_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("5A"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // PC OP
    public void testPC_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602260AA5760AA565A"), invoke);
        String s_expected = "0000000000000000000000000000000000000000000000000000000000000008";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test // JUMP OP
    public void testJUMP_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60AA60BB600E5860CC60DD60EE5D60FF"), invoke);
        String s_expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }

    @Test(expected=BadJumpDestinationException.class) // JUMP OP mal data
    public void testJUMP_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600C5860CC60DD60EE60FF"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
    }

    @Test // JUMPI OP
    public void testJUMPI_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60016006595D60CC"), invoke);
        String s_expected = "00000000000000000000000000000000000000000000000000000000000000CC";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected, Hex.toHexString(program.stack.peek().getData()).toUpperCase());
    }


    @Test // JUMPI OP
    public void testJUMPI_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("630000000060445960CC60DD"), invoke);
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

    @Test(expected=StackTooSmallException.class) // JUMPI OP mal
    public void testJUMPI_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600159"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
        	assertTrue(program.isStopped());
        }
    }

    @Test(expected=BadJumpDestinationException.class) // JUMPI OP mal
    public void testJUMPI_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6001602259"), invoke);
        try {
            vm.step(program);
            vm.step(program);
            vm.step(program);
        } finally {
        	assertTrue(program.isStopped());
        }
    }
    
    @Test // JUMPDEST OP for JUMP
    public void testJUMPDEST_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("602360085860015d600257"), invoke);
        
        String s_expected_key = "0000000000000000000000000000000000000000000000000000000000000002";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000023";
        
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
            	
        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = program.result.getRepository().getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(), key);
    	
    	assertTrue(program.isStopped());
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }
    
    @Test // JUMPDEST OP for JUMPI
    public void testJUMPDEST_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60236001600a5960015d600257"), invoke);
        
        String s_expected_key = "0000000000000000000000000000000000000000000000000000000000000002";
        String s_expected_val = "0000000000000000000000000000000000000000000000000000000000000023";
        
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
            	
        DataWord key = new DataWord(Hex.decode(s_expected_key));
        DataWord val = program.result.getRepository().getStorageValue(invoke.getOwnerAddress().getNoLeadZeroesData(), key);
    	
    	assertTrue(program.isStopped());
        assertEquals(s_expected_val, Hex.toHexString(val.getData()).toUpperCase());
    }

    @Test // ADD OP mal
    public void testADD_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6002600201"), invoke);
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
        program =  new Program(Hex.decode("611002600201"), invoke);
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
        program =  new Program(Hex.decode("6110026512345678900901"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000012345678A00B";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // ADD OP mal
    public void testADD_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("61123401"), invoke);
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
        program =  new Program(Hex.decode("60026002600314"), invoke);
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
        program =  new Program(Hex.decode("6110006002611002146000"), invoke);
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
        program =  new Program(Hex.decode("61100265123456789009600214"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000000000000093B";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertTrue(program.isStopped());
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // ADDMOD OP mal
    public void testADDMOD_4() {
        VM vm = new VM();
        program =  new Program(Hex.decode("61123414"), invoke);
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
        program =  new Program(Hex.decode("6003600202"), invoke);
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
        program =  new Program(Hex.decode("62222222600302"), invoke);
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
        program =  new Program(Hex.decode("622222226233333302"), invoke);
        String s_expected_1 = "000000000000000000000000000000000000000000000000000006D3A05F92C6";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // MUL OP mal
    public void testMUL_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600102"), invoke);
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
        program =  new Program(Hex.decode("60036002600415"), invoke);
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
        program =  new Program(Hex.decode("622222226003600415"), invoke);
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
        program =  new Program(Hex.decode("62222222623333336244444415"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // MULMOD OP mal
    public void testMULMOD_4() {
        VM vm = new VM();
        program =  new Program(Hex.decode("600115"), invoke);
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
        program =  new Program(Hex.decode("6002600404"), invoke);
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
        program =  new Program(Hex.decode("6033609904"), invoke);
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
        program =  new Program(Hex.decode("6022609904"), invoke);
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
        program =  new Program(Hex.decode("6015609904"), invoke);
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
        program =  new Program(Hex.decode("6004600704"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // DIV OP
    public void testDIV_6() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600704"), invoke);
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
        program =  new Program(Hex.decode("6103E87FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC1805"), invoke);
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
        program =  new Program(Hex.decode("60FF60FF05"), invoke);
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
        program =  new Program(Hex.decode("600060FF05"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // SDIV OP mal
    public void testSDIV_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60FF05"), invoke);

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
        program =  new Program(Hex.decode("6004600603"), invoke);
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
        program =  new Program(Hex.decode("61444461666603"), invoke);
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
        program =  new Program(Hex.decode("614444639999666603"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000099992222";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // SUB OP mal
    public void testSUB_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("639999666603"), invoke);
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
        program =  new Program(Hex.decode("5B"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // MSIZE OP
    public void testMSIZE_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60206030545B"), invoke);
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
        program =  new Program(Hex.decode("60206030601060306011602300"), invoke);
        int expectedSteps = 7;

        int i = 0;
        while (!program.isStopped()) {

            vm.step(program);
            ++i;
        }
        assertEquals(expectedSteps, i);
    }


    @Test // EXP OP
    public void testEXP_1() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6003600208"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000008";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test // EXP OP
    public void testEXP_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("60006212345608"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // EXP OP mal
    public void testEXP_3() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6212345608"), invoke);
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
        program =  new Program(Hex.decode("61123460005460206000F2"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000001234";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // RETURN OP
    public void testRETURN_2() {

        VM vm = new VM();
        program =  new Program(Hex.decode("6112346000546020601FF2"), invoke);
        String s_expected_1 = "3400000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }

    @Test // RETURN OP
    public void testRETURN_3() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B160005460206000F2"),
                        invoke);
        String s_expected_1 = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // RETURN OP
    public void testRETURN_4() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B160005460206010F2"),
                        invoke);
        String s_expected_1 = "E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B100000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn().array()).toUpperCase());
        assertTrue(program.isStopped());
    }


    @Test // CODECOPY OP
    public void testCODECOPY_1() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("60036007600039123456"), invoke);
        String m_expected_1 = "1234560000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected_1, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // CODECOPY OP
    public void testCODECOPY_2() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235602054"),
                        invoke);
        String m_expected_1 = "6000605F556014600054601E60205463ABCDDCBA6040545B51602001600A5254516040016014525451606001601E5254516080016028525460A052546016604860003960166000F26000603F556103E756600054600053602002356020540000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected_1, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // CODECOPY OP
    public void testCODECOPY_3() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertTrue(program.isStopped());
    }

    @Test // CODECOPY OP
    public void testCODECOPY_4() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("605E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertTrue(program.isStopped());
    }

//    DataWord memOffsetData
//    DataWord codeOffsetData
//    DataWord lengthData


    @Test // CODECOPY OP
    public void testCODECOPY_5() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("611234600054615566602054607060006020396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
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


    @Test(expected=StackTooSmallException.class) // CODECOPY OP mal
    public void testCODECOPY_6() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("605E6007396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
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
        
        assertEquals(m_expected_1, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_2() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("603E6007600073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235602054"),
                        invoke);
        String m_expected_1 = "6000605F556014600054601E60205463ABCDDCBA6040545B51602001600A5254516040016014525451606001601E5254516080016028525460A0525460160000";

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertEquals(m_expected_1, Hex.toHexString(program.memory.array()).toUpperCase());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_3() {
        VM vm = new VM();
        program = 
                new Program(Hex.decode("605E6007600073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        
        assertTrue(program.isStopped());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_4() {

        VM vm = new VM();
        program = 
                new Program(Hex.decode("605E6007600073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
                        invoke);

        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);
        vm.step(program);

        assertTrue(program.isStopped());
    }

    @Test // EXTCODECOPY OP
    public void testEXTCODECOPY_5() {
        VM vm = new VM();
        program = 
                new Program(Hex.decode("611234600054615566602054603E6000602073471FD3AD3E9EEADEEC4608B92D16CE6B500704CC3C6000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e756600054600053602002351234"),
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


    @Test(expected=StackTooSmallException.class) // EXTCODECOPY OP mal
    public void testEXTCODECOPY_6() {
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
                new Program(Hex.decode("385E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000062";

        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }
    
    @Test // EXTCODESIZE OP
    public void testEXTCODESIZE_1() {
        VM vm = new VM();
        program = 
                new Program(Hex.decode("73471FD3AD3E9EEADEEC4608B92D16CE6B500704CC395E60076000396000605f556014600054601e60205463abcddcba6040545b51602001600a5254516040016014525451606001601e5254516080016028525460a052546016604860003960166000f26000603f556103e75660005460005360200235"),
                        invoke); // Push address on the stack and perform EXTCODECOPY
        String s_expected_1 = "000000000000000000000000471FD3AD3E9EEADEEC4608B92D16CE6B500704CC";

        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }
    
    @Test // MOD OP
    public void testMOD_1() {
        VM vm = new VM();
        program =  new Program(Hex.decode("6003600406"), invoke);
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
        program =  new Program(Hex.decode("61012C6101F406"), invoke);
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
        program =  new Program(Hex.decode("6004600206"), invoke);
        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000000002";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }

    @Test(expected=StackTooSmallException.class) // MOD OP mal
    public void testMOD_4() {

        VM vm = new VM();
        program =  new Program(Hex.decode("600406"), invoke);

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
        program =  new Program(Hex.decode("6003600407"), invoke);
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
        program =  new Program(Hex.decode("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE2" + //  -30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "07"), invoke);
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
        program =  new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                 "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF56" + // -170
                                                 "07"), invoke);
        String s_expected_1 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEC";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        DataWord item1 = program.stackPop();
        assertEquals(s_expected_1, Hex.toHexString(item1.getData()).toUpperCase());
    }
    
    @Test(expected=StackTooSmallException.class) // SMOD OP mal
    public void testSMOD_4() {
        VM vm = new VM();
        program =  new Program(Hex.decode("7F000000000000000000000000000000000000000000000000000000000000001E" + //   30
                                                "07"), invoke);
        try {
            vm.step(program);
            vm.step(program);
        } finally {
            assertTrue(program.isStopped());
        }
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
