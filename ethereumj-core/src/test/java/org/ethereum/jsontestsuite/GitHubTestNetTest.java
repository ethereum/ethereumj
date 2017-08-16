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
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTestNetTest {

    static String commitSHA = "85f6d7cc01b6bd04e071f5ba579fc675cfd2043b";
    static String treeSHA = "0af522c09e8a264f651f5a4715301381c14784d7"; // https://github.com/ethereum/tests/tree/develop/BlockchainTests/TransitionTests

    static BlockchainTestSuite suite;

    @BeforeClass
    public static void setup() {
        suite = new BlockchainTestSuite(treeSHA, commitSHA);
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
        suite.runAll("TransitionTests/bcFrontierToHomestead", GitHubJSONTestSuite.Network.FrontierToHomesteadAt5);
    }

    @Test
    @Ignore // TODO fix it
    public void bcHomesteadToDao() throws IOException {
        suite.runAll("TransitionTests/bcHomesteadToDao", GitHubJSONTestSuite.Network.HomesteadToDaoAt5);
    }

    @Test
    public void bcHomesteadToEIP150() throws IOException {
        suite.runAll("TransitionTests/bcHomesteadToEIP150", GitHubJSONTestSuite.Network.HomesteadToEIP150At5);
    }
}
