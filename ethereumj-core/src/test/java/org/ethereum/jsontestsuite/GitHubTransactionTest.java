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
import org.ethereum.config.blockchain.Eip160HFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.ethereum.jsontestsuite.suite.TransactionTestSuite;
import org.ethereum.jsontestsuite.suite.runners.TransactionTestRunner;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTransactionTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "289b3e4524786618c7ec253b516bc8e76350f947";

    @Before
    public void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @After
    public void recover() {
        SystemProperties.resetToDefault();
    }

    @Test
    public void testEIP155TransactionTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        SystemProperties.getDefault().setBlockchainConfig(new BaseNetConfig() {{
            add(0, new FrontierConfig());
            add(1_150_000, new HomesteadConfig());
            add(2_457_000, new Eip150HFConfig(new DaoHFConfig()));
            add(2_675_000, new Eip160HFConfig(new DaoHFConfig()){
                @Override
                public Integer getChainId() {
                    return null;
                }
            });
        }});
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Ignore
    @Test
    public void runsingleTest() throws Exception {
        String json = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttTransactionTest.json", shacommit);
        TransactionTestSuite testSuite = new TransactionTestSuite(json);
        List<String> res = TransactionTestRunner.run(testSuite.getTestCases().get("V_overflow64bitPlus28"));
        System.out.println(res);
    }

    @Test
    public void testHomesteadTestsFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json1 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/tt10mbDataField.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json1, excluded);

        String json2 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json2, excluded);

        String json3 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttTransactionTestEip155VitaliksTests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json3, excluded);

        String json4 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttWrongRLPTransaction.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json4, excluded);
    }

    @Test
    public void testRandomTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        // pre-EIP155 wrong chain id (negative)
        String json = JSONReader.loadJSONFromCommit("TransactionTests/RandomTests/tr201506052141PYTHON.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Test
    public void testGeneralTestsFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json1 = JSONReader.loadJSONFromCommit("TransactionTests/tt10mbDataField.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json1, excluded);

        String json2 = JSONReader.loadJSONFromCommit("TransactionTests/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json2, excluded);
    }

    @Ignore // Few tests fails, RLPWrongByteEncoding and RLPLength preceding 0s errors left
    @Test
    public void testWrongRLPTestsFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("TransactionTests/ttWrongRLPTransaction.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Test
    public void testEip155VitaliksTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTestEip155VitaliksTests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Test
    public void testEip155VRuleTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTestVRule.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }
}
