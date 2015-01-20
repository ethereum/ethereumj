package test.ethereum.serpent;

import org.ethereum.serpent.SerpentCompiler;

import org.junit.Test;

import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

/**
 * @author Roman Mandeleil
 * @since 28.05.2014
 */
public class MachineCompileTest {

    @Test // very simple contract
    public void test1() {

        String code = "a=2";
        String expected = "6005600c60003960056000f36002600052";
        String asm = SerpentCompiler.compile(code);
        byte[] machineCode = SerpentCompiler.compileAssemblyToMachine(asm);
        byte[] vmReadyCode = SerpentCompiler.encodeMachineCodeForVMRun(machineCode, null);

        String result = Hex.toHexString(vmReadyCode);
        assertEquals(expected, result);
    }

    @Test // contract for 256 bytes (len 2 bytes)
    public void test2() {

        String code = "a=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\na=2\n[asm PUSH10 asm]";
        String expected = "610100600e6000396101006000f360026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005260026000526002600052600260005269";
        String asm = SerpentCompiler.compile(code);
        byte[] machineCode = SerpentCompiler.compileAssemblyToMachine(asm);
        byte[] vmReadyCode = SerpentCompiler.encodeMachineCodeForVMRun(machineCode, null);

        String result = Hex.toHexString(vmReadyCode);

        assertEquals(expected, result);
    }

    @Test // contract for if jump
    public void test3() {

        String code = "a=2\n" +
                "if a>0:\n" +
                "  b = 3\n" +
                "else: \n" +
                "  c = 4";
//        String expected = "610100600e6000396101006000f260026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005460026000546002600054600260005469";
        String asm = SerpentCompiler.compile(code);
        byte[] machineCode = SerpentCompiler.compileAssemblyToMachine(asm);
        byte[] vmReadyCode = SerpentCompiler.encodeMachineCodeForVMRun(machineCode, null);

        System.out.println(asm);
    }
}
