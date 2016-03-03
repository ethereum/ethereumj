package org.ethereum.solidity;

import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Anton Nashatyrev on 03.03.2016.
 */
public class CompilerTest {

    @Test
    public void simpleTest() throws IOException {
        String contract =
                "contract a {" +
                "  int i1;" +
                "  function i() returns (int) {" +
                "    return i1;" +
                "  }" +
                "}";
        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.INTERFACE);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);
        System.out.println(result.contracts.get("a").bin);
    }

    public static void main(String[] args) throws Exception {
        new CompilerTest().simpleTest();
    }
}
