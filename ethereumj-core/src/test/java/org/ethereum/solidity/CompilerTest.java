package org.ethereum.solidity;

import org.ethereum.core.CallTransaction;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.ethereum.solidity.compiler.SolidityCompiler.Options.*;

/**
 * Created by Anton Nashatyrev on 03.03.2016.
 */
public class CompilerTest {

    @Test
    public void simpleTest() throws IOException {
        String contract =
            "pragma solidity ^0.4.7;\n" +
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
                contract.getBytes(), true, ABI, BIN, INTERFACE, METADATA);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);
        if (result.contracts.get("a") != null)
            System.out.println(result.contracts.get("a").bin);
        else
            Assert.fail();
    }

    @Test
    public void defaultFuncTest() throws IOException {
        String contractSrc =
            "pragma solidity ^0.4.7;\n" +
                    "contract a {" +
                    "        function() {throw;}" +
                    "}";

        SolidityCompiler.Result res = SolidityCompiler.compile(
                contractSrc.getBytes(), true, ABI, BIN, INTERFACE, METADATA);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);

        CompilationResult.ContractMetadata a = result.contracts.get("a");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.printf(contract.functions[0].toString());
    }



    public static void main(String[] args) throws Exception {
        new CompilerTest().simpleTest();
    }
}
