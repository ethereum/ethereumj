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
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.GeneralStateTestSuite;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    static String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";
    static String treeSHA = "d1ece13ebfb2adb27061ae5a6155bd9ed9773d8f"; // https://github.com/ethereum/tests/tree/develop/GeneralStateTests/
    static GitHubJSONTestSuite.Network[] targetNets = {
            GitHubJSONTestSuite.Network.Frontier,
            GitHubJSONTestSuite.Network.Homestead,
            GitHubJSONTestSuite.Network.EIP150,
            GitHubJSONTestSuite.Network.EIP158,
            GitHubJSONTestSuite.Network.Byzantium
    };

    static GeneralStateTestSuite suite;

    @BeforeClass
    public static void setup() throws IOException {
        suite = new GeneralStateTestSuite(treeSHA, commitSHA, targetNets);
        SystemProperties.getDefault().setRecordInternalTransactionsData(false);
    }

    @AfterClass
    public static void clean() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
        SystemProperties.getDefault().setRecordInternalTransactionsData(true);
    }

    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    // omit GeneralStateTestSuite initialization when use this method
    // it reduces impact on GitHub API
    public void stSingleTest() throws IOException {
        GeneralStateTestSuite.runSingle(
                "stPreCompiledContracts2/modexpRandomInput.json", commitSHA, GitHubJSONTestSuite.Network.Byzantium);
    }

    @Test
    public void stAttackTest() throws IOException {
        suite.runAll("stAttackTest");
    }

    @Test
    public void stCallCodes() throws IOException {
        suite.runAll("stCallCodes");
    }

    @Test
    public void stExample() throws IOException {
        suite.runAll("stExample");
    }

    @Test
    public void stCallDelegateCodesCallCodeHomestead() throws IOException {
        suite.runAll("stCallDelegateCodesCallCodeHomestead");
    }

    @Test
    public void stCallDelegateCodesHomestead() throws IOException {
        suite.runAll("stCallDelegateCodesHomestead");
    }

    @Test
    public void stChangedEIP150() throws IOException {
        suite.runAll("stChangedEIP150");
    }

    @Test
    public void stCallCreateCallCodeTest() throws IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBombPreCall"); // Max Gas value is pending to be < 2^63

        suite.runAll("stCallCreateCallCodeTest", excluded);
    }

    @Test
    public void stDelegatecallTestHomestead() throws IOException {
        suite.runAll("stDelegatecallTestHomestead");
    }

    @Test
    public void stEIP150Specific() throws IOException {
        suite.runAll("stEIP150Specific");
    }

    @Test
    public void stEIP150singleCodeGasPrices() throws IOException {
        suite.runAll("stEIP150singleCodeGasPrices");
    }

    @Test
    public void stEIP158Specific() throws IOException {
        suite.runAll("stEIP158Specific");
    }

    @Test
    public void stHomesteadSpecific() throws IOException {
        suite.runAll("stHomesteadSpecific");
    }

    @Test
    public void stInitCodeTest() throws IOException {
        suite.runAll("stInitCodeTest");
    }

    @Test
    public void stLogTests() throws IOException {
        suite.runAll("stLogTests");
    }

    @Test
    public void stMemExpandingEIP150Calls() throws IOException {
        suite.runAll("stMemExpandingEIP150Calls");
    }

    @Test
    public void stPreCompiledContracts() throws IOException {
        suite.runAll("stPreCompiledContracts");
    }

    @Test
    public void stPreCompiledContracts2() throws IOException {
        suite.runAll("stPreCompiledContracts2");
    }

    @Test
    @Ignore
    public void stMemoryStressTest() throws IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("mload32bitBound_return2");// The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_return"); // The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_Msize"); // The test extends memory to 4Gb which can't be handled with Java arrays
        suite.runAll("stMemoryStressTest", excluded);
    }

    @Test
    @Ignore
    public void stMemoryTest() throws IOException {
        suite.runAll("stMemoryTest");
    }

    @Test
    public void stQuadraticComplexityTest() throws IOException {
        // leaving only Homestead version since the test runs too long
        suite.runAll("stQuadraticComplexityTest", GitHubJSONTestSuite.Network.Homestead);
    }

    @Test
    public void stSolidityTest() throws IOException {
        suite.runAll("stSolidityTest");
    }

    @Test
    public void stRecursiveCreate() throws IOException {
        suite.runAll("stRecursiveCreate");
    }

    @Test
    public void stRefundTest() throws IOException {
        suite.runAll("stRefundTest");
    }

    @Test
    public void stReturnDataTest() throws IOException {
        suite.runAll("stReturnDataTest");
    }

    @Test
    public void stRevertTest() throws IOException {
        suite.runAll("stRevertTest");
    }

    @Test
    public void stSpecialTest() throws IOException {
        suite.runAll("stSpecialTest");
    }

    @Test
    public void stStackTests() throws IOException {
        suite.runAll("stStackTests");
    }

    @Test
    public void stStaticCall() throws IOException {
        suite.runAll("stStaticCall");
    }

    @Test
    public void stSystemOperationsTest() throws IOException {
        suite.runAll("stSystemOperationsTest");
    }

    @Test
    public void stTransactionTest() throws IOException {
        // TODO enable when zero sig Txes comes in
        suite.runAll("stTransactionTest", new HashSet<>(Arrays.asList(
                "zeroSigTransactionCreate",
                "zeroSigTransactionCreatePrice0",
                "zeroSigTransacrionCreatePrice0",
                "zeroSigTransaction",
                "zeroSigTransaction0Price",
                "zeroSigTransactionInvChainID",
                "zeroSigTransactionInvNonce",
                "zeroSigTransactionInvNonce2",
                "zeroSigTransactionOOG",
                "zeroSigTransactionOrigin",
                "zeroSigTransactionToZero",
                "zeroSigTransactionToZero2"
        )));
    }

    @Test
    public void stTransitionTest() throws IOException {
        suite.runAll("stTransitionTest");
    }

    @Test
    public void stWalletTest() throws IOException {
        suite.runAll("stWalletTest");
    }

    @Test
    public void stZeroCallsRevert() throws IOException {
        suite.runAll("stZeroCallsRevert");
    }

    @Test
    public void stCreateTest() throws IOException {
        suite.runAll("stCreateTest");
    }

    @Test
    public void stZeroCallsTest() throws IOException {
        suite.runAll("stZeroCallsTest");
    }

    @Test
    public void stZeroKnowledge() throws IOException {
        suite.runAll("stZeroKnowledge");
    }

    @Test
    public void stZeroKnowledge2() throws IOException {
        suite.runAll("stZeroKnowledge2");
    }

    @Test
    public void stCodeSizeLimit() throws IOException {
        suite.runAll("stCodeSizeLimit");
    }

    @Test
    public void stRandom() throws IOException {
        suite.runAll("stRandom");
    }

    @Test
    public void stRandom2() throws IOException {
        suite.runAll("stRandom2");
    }

    @Test
    public void stBadOpcode() throws IOException {
        suite.runAll("stBadOpcode");
    }

    @Test
    public void stNonZeroCallsTest() throws IOException {
        suite.runAll("stNonZeroCallsTest");
    }

    @Test
    public void stCodeCopyTest() throws IOException {
        suite.runAll("stCodeCopyTest");
    }
}

