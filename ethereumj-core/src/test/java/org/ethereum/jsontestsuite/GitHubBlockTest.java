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

import org.ethereum.jsontestsuite.suite.BlockchainTestSuite;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    static String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";
    static String treeSHA = "3f6a1117be5c0d6f801875118c7c580dc4200712"; // https://github.com/ethereum/tests/tree/develop/BlockchainTests/
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
    }

    @Ignore
    @Test
    // this method is mostly for hands-on convenient testing
    // do not initialize BlockchainTestSuite to avoid unnecessary GitHub API hits
    public void bcSingleTest() throws IOException {
        BlockchainTestSuite.runSingle(
                "bcWalletTest/wallet2outOf3txs2.json", commitSHA, GitHubJSONTestSuite.Network.Byzantium);
    }


    @Test
    public void bcBlockGasLimitTest() throws IOException {
        suite.runAll("bcBlockGasLimitTest");
    }

    @Test
    public void bcExploitTest() throws IOException {
        suite.runAll("bcExploitTest", new HashSet<>(Arrays.asList(
                "SuicideIssue" // it was checked once, but it's too heavy to hit it each time
        )));
    }

    @Test
    public void bcForgedTest() throws IOException {
        suite.runAll("bcForgedTest");
    }

    @Test
    public void bcForkStressTest() throws IOException {
        suite.runAll("bcForkStressTest");
    }

    @Test
    public void bcGasPricerTest() throws IOException {
        suite.runAll("bcGasPricerTest");
    }

    @Test
    public void bcInvalidHeaderTest() throws IOException {
        suite.runAll("bcInvalidHeaderTest");
    }

    @Test
    public void bcMultiChainTest() throws IOException {
        suite.runAll("bcMultiChainTest");
    }

    @Test
    public void bcRandomBlockhashTest() throws IOException {
        suite.runAll("bcRandomBlockhashTest");
    }

    @Test
    public void bcStateTests() throws IOException {
        suite.runAll("bcStateTests");
    }

    @Test
    public void bcTotalDifficultyTest() throws IOException {
        suite.runAll("bcTotalDifficultyTest");
    }

    @Test
    public void bcUncleHeaderValidity() throws IOException {
        suite.runAll("bcUncleHeaderValidity");
    }

    @Test
    public void bcUncleTest() throws IOException {
        suite.runAll("bcUncleTest");
    }

    @Test
    public void bcValidBlockTest() throws IOException {
        suite.runAll("bcValidBlockTest");
    }

    @Test
    public void bcWalletTest() throws IOException {
        suite.runAll("bcWalletTest");
    }
}
