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
import org.ethereum.config.blockchain.DaoHFConfig;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Collections;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTestNetTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "9ed33d7440f13c09ce7f038f92abd02d23b26f0d";

    @Before
    public void setup() {
        SystemProperties.getDefault().setGenesisInfo("frontier.json");
        SystemProperties.getDefault().setBlockchainConfig(new BaseNetConfig() {{
            add(0, new FrontierConfig());
            add(5, new HomesteadConfig());
            add(8, new DaoHFConfig(new HomesteadConfig(), 8));
            add(10, new Eip150HFConfig(new DaoHFConfig(new HomesteadConfig(), 8)));

        }});
    }

    @After
    public void clean() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Test
    public void bcEIP150Test() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/TestNetwork/bcEIP150Test.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
    }
    @Test
    public void bcSimpleTransitionTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/TestNetwork/bcSimpleTransitionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
    }
    @Test
    public void bcTheDaoTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/TestNetwork/bcTheDaoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
    }
}
