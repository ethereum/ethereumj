package org.ethereum.serpent;

import org.ethereum.gui.GUIUtils;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 28/05/2014 20:05
 */


public class MachineCompileTest {


    @Test //
    public void test1(){

        String code = "a=2";
        String expected = "6005600c60003960056000f26002600054";
        String asm = SerpentCompiler.compile(code);
        byte[] machineCode = SerpentCompiler.compileAssemblyToMachine(asm);
        byte[] vmReadyCode = SerpentCompiler.encodeMachineCodeForVMRun(machineCode);

        System.out.println(GUIUtils.getHexStyledText(vmReadyCode));
        String result = Hex.toHexString(vmReadyCode);

        Assert.assertEquals(expected, result);
    }
}
