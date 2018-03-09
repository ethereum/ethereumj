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
package org.ethereum.solidity;

import org.ethereum.core.CallTransaction;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.ethereum.solidity.compiler.SolidityCompiler.Options.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Created by Anton Nashatyrev on 03.03.2016.
 */
public class CompilerTest {

    @Test
    public void solc_getVersion_shouldWork() throws IOException {
        final String version = SolidityCompiler.runGetVersionOutput();

        // ##### May produce 2 lines:
        //solc, the solidity compiler commandline interface
        //Version: 0.4.7+commit.822622cf.mod.Darwin.appleclang
        System.out.println(version);

        assertThat(version, containsString("Version:"));
    }

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
        if (result.getContract("a") != null)
            System.out.println(result.getContract("a").bin);
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

        CompilationResult.ContractMetadata a = result.getContract("a");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.print(contract.functions[0].toString());
    }

    @Test
    public void compileFilesTest() throws IOException {

        Path source = Paths.get("src","test","resources","solidity","file1.sol");

        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);

        Assert.assertEquals("test1", result.getContractName());
        Assert.assertEquals(source.toAbsolutePath(), result.getContractPath());

        CompilationResult.ContractMetadata a = result.getContract(source, "test1");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.print(contract.functions[0].toString());
    }

    @Test public void compileFilesWithImportTest() throws IOException {

        Path source = Paths.get("src","test","resources","solidity","file2.sol");

        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);

        CompilationResult.ContractMetadata a = result.getContract(source, "test2");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.print(contract.functions[0].toString());
    }

    @Test public void compileFilesWithImportFromParentFileTest() throws IOException {

        Path source = Paths.get("src","test","resources","solidity","foo","file3.sol");

        SolidityCompiler.Option allowPathsOption = new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent().toFile()));
        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);

        Assert.assertEquals(2, result.getContractKeys().size());
        Assert.assertEquals(result.getContract("test3"), result.getContract(source,"test3"));
        Assert.assertNotNull(result.getContract("test1"));

        CompilationResult.ContractMetadata a = result.getContract(source, "test3");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.print(contract.functions[0].toString());
    }

    @Test public void compileFilesWithImportFromParentStringTest() throws IOException {

        Path source = Paths.get("src","test","resources","solidity","foo","file3.sol");

        SolidityCompiler.Option allowPathsOption = new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent().toAbsolutePath().toString()));
        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);

        CompilationResult.ContractMetadata a = result.getContract(source, "test3");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.print(contract.functions[0].toString());
    }

    @Test public void compileFilesWithImportFromParentPathTest() throws IOException {

        Path source = Paths.get("src","test","resources","solidity","foo","file3.sol");

        SolidityCompiler.Option allowPathsOption = new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent()));
        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);

        CompilationResult.ContractMetadata a = result.getContract("test3");
        CallTransaction.Contract contract = new CallTransaction.Contract(a.abi);
        System.out.print(contract.functions[0].toString());
    }

    public static void main(String[] args) throws Exception {
        new CompilerTest().simpleTest();
    }
}
