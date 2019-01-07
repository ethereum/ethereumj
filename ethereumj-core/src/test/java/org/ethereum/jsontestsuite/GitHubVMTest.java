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
import org.ethereum.jsontestsuite.suite.VMTestSuite;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubVMTest {

    static String commitSHA = "253e99861fe406c7b1daf3d6a0c40906e8a8fd8f";
    static String treeSHA = "d909a11b8315a81eaf610f457205025fb1284cc8";  // https://github.com/ethereum/tests/tree/develop/VMTests/

    static VMTestSuite suite;

    @BeforeClass
    public static void setup() throws IOException {
        suite = new VMTestSuite(treeSHA, commitSHA);
    }

    @After
    public void recover() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
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
