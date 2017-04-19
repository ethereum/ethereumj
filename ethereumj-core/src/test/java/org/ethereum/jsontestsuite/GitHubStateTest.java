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
package org.ethereum.jsontestsuite;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.*;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ethereum.jsontestsuite.suite.JSONReader.getFileNamesForTreeSha;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "289b3e4524786618c7ec253b516bc8e76350f947";


    private long oldForkValue;

    @Before
    public void setup() {
        // TODO remove this after Homestead launch and shacommit update with actual block number
        // for this JSON test commit the Homestead block was defined as 900000
        SystemProperties.getDefault().setBlockchainConfig(new BaseNetConfig() {{
            add(0, new FrontierConfig());
            add(1_150_000, new HomesteadConfig());
            add(2_457_000, new Eip150HFConfig(new DaoHFConfig()));
            add(2_700_000, new Eip160HFConfig(new DaoHFConfig()));

        }});
    }

    @After
    public void clean() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, "CreateHashCollision");
    }

    @Test
    public void stExample() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallCodes() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stCallCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stCallCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallDelegateCodes() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallDelegateCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stCallDelegateCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stCallDelegateCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallDelegateCodesCallCode() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallDelegateCodesCallCode.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stCallDelegateCodesCallCode.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stCallDelegateCodesCallCode.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stHomeSteadSpecific() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stHomeSteadSpecific.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stHomeSteadSpecific.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stHomeSteadSpecific.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallCreateCallCodeTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBombPreCall"); // Max Gas value is pending to be < 2^63

        // the test creates a contract with the same address as existing contract (which is not possible in
        // live). In this case we need to clear the storage in TransactionExecutor.create
        // return back to this case when the contract deleting will be implemented
        excluded.add("createJS_ExampleContract");

        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stDelegatecallTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
//        String json = JSONReader.loadJSONFromCommit("StateTests/stDelegatecallTest.json", shacommit);
//        GitHubJSONTestSuite.runStateTest(json, excluded);

        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stDelegatecallTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stDelegatecallTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stDelegatecallTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stInitCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stLogTests() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stPreCompiledContracts() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    @Ignore
    public void stMemoryStressTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("mload32bitBound_return2");// The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_return"); // The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_Msize"); // The test extends memory to 4Gb which can't be handled with Java arrays
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stMemoryTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stQuadraticComplexityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        // leaving only Homestead version since the test runs too long
//        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
//        GitHubJSONTestSuite.runStateTest(json, excluded);

        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stSolidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stRecursiveCreate() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stRefundTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stSpecialTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stBlockHashTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json);
    }

    @Test
    public void stSystemOperationsTest() throws IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stTransactionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stTransitionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransitionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stWalletTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stWalletTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stWalletTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stWalletTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stWalletTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stBoundsTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stBoundsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP150/Homestead/stBoundsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/EIP158/Homestead/stBoundsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stEIPSpecificTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP150/stEIPSpecificTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stChangedTests() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP150/stChangedTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stEIPsingleCodeGasPrices() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP150/stEIPsingleCodeGasPrices.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stMemExpandingEIPCalls() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP150/stMemExpandingEIPCalls.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCreateTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP158/stCreateTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stEIP158SpecificTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP158/stEIP158SpecificTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stNonZeroCallsTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP158/stNonZeroCallsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stZeroCallsTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP158/stZeroCallsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCodeSizeLimit() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/EIP158/stCodeSizeLimit.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test // testing full suite
    public void testRandomStateGitHub() throws ParseException, IOException {

        String sha = "99db6f4f5fea3aa5cfbe8436feba8e213d06d1e8";
        List<String> fileNames = getFileNamesForTreeSha(sha);
        List<String> includedFiles =
                Arrays.asList(
                        "st201504081841JAVA.json",
                        "st201504081842JAVA.json",
                        "st201504081843JAVA.json"
                );

        for (String fileName : fileNames) {
            if (includedFiles.contains(fileName)) {
              System.out.println("Running: " + fileName);
              String json = JSONReader.loadJSON("StateTests//RandomTests/" + fileName);
              GitHubJSONTestSuite.runStateTest(json);
            }
        }

    }
}

