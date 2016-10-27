package org.ethereum.solidity;

import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Anton Nashatyrev on 03.03.2016.
 */
public class CompilerTest {

    @Test
    public void simpleTest() throws IOException {
        String contract =
            "pragma solidity ^0.4.3;\n" +
                    "\n" +
                    "contract a {\n" +
                    "\n" +
                    "        mapping(address => string) private mailbox;\n" +
                    "\n" +
                    "        event Mailed(address from, string message);\n" +
                    "        event Read(address from, string message);\n" +
                    "\n" +
                    "}";

        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.INTERFACE);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);
        if (result.contracts.get("a") != null)
            System.out.println(result.contracts.get("a").bin);
        else
            Assert.fail();
    }

    public static void main(String[] args) throws Exception {
        new CompilerTest().simpleTest();
    }
}
