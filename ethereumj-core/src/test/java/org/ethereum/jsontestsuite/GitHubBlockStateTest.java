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
import org.ethereum.jsontestsuite.suite.BlockchainTestSuite;
import org.ethereum.jsontestsuite.suite.GeneralStateTestSuite;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockStateTest {

    static String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";
    static String treeSHA = "9b96943196bfbb8b49651eab5e479956d7dabcc7"; // https://github.com/ethereum/tests/tree/develop/BlockchainTests/GeneralStateTests/

    static GitHubJSONTestSuite.Network[] targetNets = {
            GitHubJSONTestSuite.Network.Frontier,
            GitHubJSONTestSuite.Network.Homestead,
            GitHubJSONTestSuite.Network.EIP150,
            GitHubJSONTestSuite.Network.EIP158,
            GitHubJSONTestSuite.Network.Byzantium
    };

    static BlockchainTestSuite suite;

    @BeforeClass
    public static void setup() throws IOException {
        suite = new BlockchainTestSuite(treeSHA, commitSHA, targetNets);
        suite.setSubDir("GeneralStateTests/");
        SystemProperties.getDefault().setRecordInternalTransactionsData(false);
    }

    @AfterClass
    public static void clean() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
        SystemProperties.getDefault().setRecordInternalTransactionsData(true);
    }

    @Test
    @Ignore
    // this method is mostly for hands-on convenient testing
    // using this method turn off initializing of BlockchainTestSuite to avoid unnecessary GitHub API hits
    public void bcStSingle() throws IOException {
        BlockchainTestSuite.runSingle(
                "GeneralStateTests/stRandom/randomStatetest642_d0g0v0.json", commitSHA, GitHubJSONTestSuite.Network.Byzantium);
    }

    @Test
    public void bcStAttackTest() throws IOException {
        suite.runAll("stAttackTest");
    }

    @Test
    public void bcStCallCodes() throws IOException {
        suite.runAll("stCallCodes");
    }

    @Test
    public void bcStExample() throws IOException {
        suite.runAll("stExample");
    }

    @Test
    public void bcStCallDelegateCodesCallCodeHomestead() throws IOException {
        suite.runAll("stCallDelegateCodesCallCodeHomestead");
    }

    @Test
    public void bcStCallDelegateCodesHomestead() throws IOException {
        suite.runAll("stCallDelegateCodesHomestead");
    }

    @Test
    public void bcStChangedEIP150() throws IOException {
        suite.runAll("stChangedEIP150");
    }

    @Test
    public void bcStCallCreateCallCodeTest() throws IOException {
        suite.runAll("stCallCreateCallCodeTest");
    }

    @Test
    public void bcStDelegatecallTestHomestead() throws IOException {
        suite.runAll("stDelegatecallTestHomestead");
    }

    @Test
    public void bcStEIP150Specific() throws IOException {
        suite.runAll("stEIP150Specific");
    }

    @Test
    public void bcStEIP150singleCodeGasPrices() throws IOException {
        suite.runAll("stEIP150singleCodeGasPrices");
    }

    @Test
    public void bcStEIP158Specific() throws IOException {
        suite.runAll("stEIP158Specific");
    }

    @Test
    public void bcStHomesteadSpecific() throws IOException {
        suite.runAll("stHomesteadSpecific");
    }

    @Test
    public void bcStInitCodeTest() throws IOException {
        suite.runAll("stInitCodeTest");
    }

    @Test
    public void bcStLogTests() throws IOException {
        suite.runAll("stLogTests");
    }

    @Test
    public void bcStMemExpandingEIP150Calls() throws IOException {
        suite.runAll("stMemExpandingEIP150Calls");
    }

    @Test
    public void bcStPreCompiledContracts() throws IOException {
        suite.runAll("stPreCompiledContracts");
    }

    @Test
    public void bcStPreCompiledContracts2() throws IOException {
        suite.runAll("stPreCompiledContracts2");
    }

    @Test
    @Ignore
    public void bcStMemoryStressTest() throws IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("mload32bitBound_return2");// The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_return"); // The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_Msize"); // The test extends memory to 4Gb which can't be handled with Java arrays
        suite.runAll("stMemoryStressTest", excluded);
    }

    @Test
    public void bcStMemoryTest() throws IOException {
        suite.runAll("stMemoryTest");
    }

    @Test
    public void bcStQuadraticComplexityTest() throws IOException {
        // leaving only Homestead version since the test runs too long
        suite.runAll("stQuadraticComplexityTest", GitHubJSONTestSuite.Network.Homestead);
    }

    @Test
    public void bcStSolidityTest() throws IOException {
        suite.runAll("stSolidityTest");
    }

    @Test
    public void bcStRecursiveCreate() throws IOException {
        suite.runAll("stRecursiveCreate");
    }

    @Test
    public void bcStRefundTest() throws IOException {
        suite.runAll("stRefundTest");
    }

    @Test
    public void bcStReturnDataTest() throws IOException {
        suite.runAll("stReturnDataTest");
    }

    @Test
    public void bcStRevertTest() throws IOException {
        suite.runAll("stRevertTest");
    }

    @Test
    public void bcStSpecialTest() throws IOException {
        suite.runAll("stSpecialTest");
    }

    @Test
    public void bcStStackTests() throws IOException {
        suite.runAll("stStackTests");
    }

    @Test
    public void bcStStaticCall() throws IOException {
        suite.runAll("stStaticCall");
    }

    @Test
    public void bcStSystemOperationsTest() throws IOException {
        suite.runAll("stSystemOperationsTest");
    }

    @Test
    public void bcStTransactionTest() throws IOException {
        // TODO enable when zero sig Txes comes in
        suite.runAll("stTransactionTest", new HashSet<>(Arrays.asList(
                "zeroSigTransacrionCreate",
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
    public void bcStTransitionTest() throws IOException {
        suite.runAll("stTransitionTest");
    }

    @Test
    public void bcStWalletTest() throws IOException {
        suite.runAll("stWalletTest");
    }

    @Test
    public void bcStZeroCallsRevert() throws IOException {
        suite.runAll("stZeroCallsRevert");
    }

    @Test
    public void bcStCreateTest() throws IOException {
        suite.runAll("stCreateTest");
    }

    @Test
    public void bcStZeroCallsTest() throws IOException {
        suite.runAll("stZeroCallsTest");
    }

    @Test
    public void bcStZeroKnowledge() throws IOException {
        suite.runAll("stZeroKnowledge");
    }

    @Test
    public void bcStZeroKnowledge2() throws IOException {
        suite.runAll("stZeroKnowledge2");
    }

    @Test
    public void bcStCodeSizeLimit() throws IOException {
        suite.runAll("stCodeSizeLimit");
    }

    @Test
    public void bcStRandom() throws IOException {
        suite.runAll("stRandom");
    }

    @Test
    public void bcStRandom2() throws IOException {
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
