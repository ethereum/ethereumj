package org.ethereum.vm;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.fail;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 01/06/2014 11:05
 */

public class VMTest {


    @Test  // PUSH1 OP
    public void testPUSH1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("60A0"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000A0";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH2 OP
    public void testPUSH2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("61A0B0"));
        String expected = "000000000000000000000000000000000000000000000000000000000000A0B0";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH3 OP
    public void testPUSH3(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("62A0B0C0"));
        String expected = "0000000000000000000000000000000000000000000000000000000000A0B0C0";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH4 OP
    public void testPUSH4(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("63A0B0C0D0"));
        String expected = "00000000000000000000000000000000000000000000000000000000A0B0C0D0";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH5 OP
    public void testPUSH5(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("64A0B0C0D0E0"));
        String expected = "000000000000000000000000000000000000000000000000000000A0B0C0D0E0";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH6 OP
    public void testPUSH6(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("65A0B0C0D0E0F0"));
        String expected = "0000000000000000000000000000000000000000000000000000A0B0C0D0E0F0";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH7 OP
    public void testPUSH7(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("66A0B0C0D0E0F0A1"));
        String expected = "00000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH8 OP
    public void testPUSH8(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("67A0B0C0D0E0F0A1B1"));
        String expected = "000000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH9 OP
    public void testPUSH9(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("68A0B0C0D0E0F0A1B1C1"));
        String expected = "0000000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }


    @Test  // PUSH10 OP
    public void testPUSH10(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("69A0B0C0D0E0F0A1B1C1D1"));
        String expected = "00000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH11 OP
    public void testPUSH11(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6AA0B0C0D0E0F0A1B1C1D1E1"));
        String expected = "000000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH12 OP
    public void testPUSH12(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6BA0B0C0D0E0F0A1B1C1D1E1F1"));
        String expected = "0000000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH13 OP
    public void testPUSH13(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6CA0B0C0D0E0F0A1B1C1D1E1F1A2"));
        String expected = "00000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH14 OP
    public void testPUSH14(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6DA0B0C0D0E0F0A1B1C1D1E1F1A2B2"));
        String expected = "000000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH15 OP
    public void testPUSH15(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6EA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2"));
        String expected = "0000000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH16 OP
    public void testPUSH16(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2"));
        String expected = "00000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH17 OP
    public void testPUSH17(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("70A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2"));
        String expected = "000000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH18 OP
    public void testPUSH18(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("71A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2"));
        String expected = "0000000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH19 OP
    public void testPUSH19(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("72A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3"));
        String expected = "00000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH20 OP
    public void testPUSH20(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("73A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3"));
        String expected = "000000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH21 OP
    public void testPUSH21(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("74A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3"));
        String expected = "0000000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH22 OP
    public void testPUSH22(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("75A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3"));
        String expected = "00000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH23 OP
    public void testPUSH23(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("76A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3"));
        String expected = "000000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH24 OP
    public void testPUSH24(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("77A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3"));
        String expected = "0000000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH25 OP
    public void testPUSH25(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("78A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4"));
        String expected = "00000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH26 OP
    public void testPUSH26(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("79A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4"));
        String expected = "000000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH27 OP
    public void testPUSH27(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("7AA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4"));
        String expected = "0000000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH28 OP
    public void testPUSH28(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("7BA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4"));
        String expected = "00000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH29 OP
    public void testPUSH29(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("7CA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4"));
        String expected = "000000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH30 OP
    public void testPUSH30(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("7DA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4"));
        String expected = "0000A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH31 OP
    public void testPUSH31(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("7EA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1"));
        String expected = "00A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSH32 OP
    public void testPUSH32(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("7FA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1"));
        String expected = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

        program.fullTrace();
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // PUSHN OP mal data
    public void testPUSHN_1(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("61AA"));
            String expected = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

            program.fullTrace();
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // PUSHN OP mal data
    public void testPUSHN_2(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("7fAABB"));
            String expected = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";

            program.fullTrace();
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // AND OP
    public void testAND_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("600A600A10"));
        String expected = "000000000000000000000000000000000000000000000000000000000000000A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // AND OP
    public void testAND_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C0600A10"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // AND OP mal data
    public void testAND_3(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("60C010"));

            vm.step(program);
            vm.step(program);
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // OR OP
    public void testOR_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("60F0600F11"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // OR OP
    public void testOR_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("60C3603C11"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // OR OP mal data
    public void testOR_3(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("60C011"));

            vm.step(program);
            vm.step(program);
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // XOR OP
    public void testXOR_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("60FF60FF12"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // XOR OP
    public void testXOR_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("600F60F012"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000FF";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }


    @Test  // XOR OP mal data
    public void testXOR_3(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("60C012"));

            vm.step(program);
            vm.step(program);
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }


    @Test  // BYTE OP
    public void testBYTE_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEEFF601E13"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000EE";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // BYTE OP
    public void testBYTE_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEEFF602013"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // BYTE OP
    public void testBYTE_3(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("65AABBCCDDEE3A601F13"));
        String expected = "000000000000000000000000000000000000000000000000000000000000003A";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }


    @Test  // BYTE OP mal data
    public void testBYTE_4(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("65AABBCCDDEE3A13"));

            vm.step(program);
            vm.step(program);
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // NOT OP
    public void testNOT_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("60000F"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // NOT OP
    public void testNOT_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("602A0F"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // NOT OP mal data
    public void testNOT_3(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("0F"));

            vm.step(program);
            vm.step(program);
            vm.step(program);
        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // EQ OP
    public void testEQ_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("602A602A0E"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // EQ OP
    public void testEQ_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B4C622A3B4C0E"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase()  );
    }

    @Test  // EQ OP
    public void testEQ_3(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("622A3B5C622A3B4C0E"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // EQ OP mal data
    public void testEQ_4(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("622A3B4C0E"));

            vm.step(program);
            vm.step(program);
            vm.step(program);

        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test  // GT OP
    public void testGT_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160020B"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6001610F000B"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // GT OP
    public void testGT_3(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6301020304610F000B"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // GT OP mal data
    public void testGT_4(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("622A3B4C0B"));

            vm.step(program);
            vm.step(program);
            vm.step(program);

        } catch (RuntimeException e) {
            return;
        }
        fail();
    }


    @Test  // LT OP
    public void testLT_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("600160020A"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6001610F000A"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // LT OP
    public void testLT_3(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("6301020304610F000A"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000001";

        vm.step(program);
        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // LT OP mal data
    public void testLT_4(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("622A3B4C0A"));

            vm.step(program);
            vm.step(program);
            vm.step(program);

        } catch (RuntimeException e) {
            return;
        }
        fail();
    }


    @Test  // NEG OP
    public void testNEG_1(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("600109"));
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_2(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("61A00309"));
        String expected = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF5FFD";

        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_3(){

        VM vm = new VM();
        Program program = new Program(Hex.decode("61000009"));
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        vm.step(program);
        vm.step(program);

        Assert.assertEquals(expected, Hex.toHexString(program.stack.get(0).data).toUpperCase());
    }

    @Test  // NEG OP
    public void testNEG_4(){

        try {
            VM vm = new VM();
            Program program = new Program(Hex.decode("09"));

            vm.step(program);
            vm.step(program);

        } catch (RuntimeException e) {
            return;
        }
        fail();
    }

}
