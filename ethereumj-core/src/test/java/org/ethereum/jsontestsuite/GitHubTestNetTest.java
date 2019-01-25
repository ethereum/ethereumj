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

import org.ethereum.jsontestsuite.GitHubJSONTestSuite.Network;
import org.ethereum.jsontestsuite.suite.BlockchainTestSuite;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTestNetTest {

    static String commitSHA = "725dbc73a54649e22a00330bd0f4d6699a5060e5";
    static String treeSHA = "36528084e10bf993fdb20cc304f3421c7324c2a4"; // https://github.com/ethereum/tests/tree/develop/BlockchainTests/TransitionTests

    static BlockchainTestSuite suite;

    @BeforeClass
    public static void setup() throws IOException {
        suite = new BlockchainTestSuite(treeSHA, commitSHA);
        suite.setSubDir("TransitionTests/");
    }

    @Test
    @Ignore
    // this method is mostly for hands-on convenient testing
    // using this method turn off initializing of BlockchainTestSuite to avoid unnecessary GitHub API hits
    public void bcTransitionSingle() throws IOException {
        BlockchainTestSuite.runSingle(
                "TransitionTests/bcHomesteadToDao/DaoTransactions.json", commitSHA);
    }

    @Test
    public void bcFrontierToHomestead() throws IOException {
        suite.runAll("bcFrontierToHomestead", GitHubJSONTestSuite.Network.FrontierToHomesteadAt5);
    }

    @Test
    public void bcHomesteadToDao() throws IOException {
        suite.runAll("bcHomesteadToDao", GitHubJSONTestSuite.Network.HomesteadToDaoAt5);
    }

    @Test
    public void bcHomesteadToEIP150() throws IOException {
        suite.runAll("bcHomesteadToEIP150", GitHubJSONTestSuite.Network.HomesteadToEIP150At5);
    }

    @Test
    public void bcEIP158ToByzantium() throws IOException {
        suite.runAll("bcEIP158ToByzantium", GitHubJSONTestSuite.Network.EIP158ToByzantiumAt5);
    }

    @Test
    public void byzantiumToConstantinople() throws IOException {
        suite.runAll("bcByzantiumToConstantinople", GitHubJSONTestSuite.Network.ByzantiumToConstantinopleFixAt5);
    }
}
