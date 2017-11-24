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

import java.io.IOException;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.VMTestSuite;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubVMTest {

    static String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";
    static String treeSHA = "7429078cc0ae66034bbede7c9666c234dc76bc67";  // https://github.com/ethereum/tests/tree/develop/VMTests/

    static VMTestSuite suite;

    @BeforeClass
    public static void setup() throws IOException {
        suite = new VMTestSuite(treeSHA, commitSHA);
    }

    @After
    public void recover() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @Ignore
    @Test
    public void runSingle() throws ParseException, IOException {
        VMTestSuite.runSingle(commitSHA, "vmArithmeticTest/add0.json");
    }

    @Test
    public void vmArithmeticTest() throws ParseException {
        suite.runAll("vmArithmeticTest");
    }

    @Test
    public void vmBitwiseLogicOperation() throws ParseException {
        suite.runAll("vmBitwiseLogicOperation");
    }

    @Test
    public void vmBlockInfoTest() throws ParseException {
        suite.runAll("vmBlockInfoTest");
    }

    @Test
    public void vmEnvironmentalInfo() throws ParseException {
        suite.runAll("vmEnvironmentalInfo");
    }

    @Test
    public void vmIOandFlowOperations() throws ParseException {
        suite.runAll("vmIOandFlowOperations");
    }

    @Test
    public void vmLogTest() throws ParseException {
        suite.runAll("vmLogTest");
    }

    @Ignore // we pass it, but it's too long to run it each build
    @Test
    public void vmPerformance() throws ParseException {
        suite.runAll("vmPerformance");
    }

    @Test
    public void vmPushDupSwapTest() throws ParseException {
        suite.runAll("vmPushDupSwapTest");
    }

    @Test
    public void vmRandomTest() throws ParseException {
        suite.runAll("vmRandomTest");
    }

    @Test
    public void vmSha3Test() throws ParseException {
        suite.runAll("vmSha3Test");
    }

    @Test
    public void vmSystemOperations() throws ParseException {
        suite.runAll("vmSystemOperations");
    }

    @Test
    public void vmTests() throws ParseException {
        suite.runAll("vmTests");
    }
}
