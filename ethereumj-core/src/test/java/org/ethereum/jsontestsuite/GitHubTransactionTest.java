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
import org.ethereum.jsontestsuite.suite.TxTestSuite;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTransactionTest {

    static String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";
    static String treeSHA = "1405e8a09a6f695e843259b9029b04a3fc4da3fa";  // https://github.com/ethereum/tests/tree/develop/TransactionTests/

    static TxTestSuite suite;

    @BeforeClass
    public static void setup() throws IOException {
        suite = new TxTestSuite(treeSHA, commitSHA);
    }

    @After
    public void recover() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @Ignore
    @Test
    public void runSingleTest() throws IOException, ParseException {
        TxTestSuite.runSingle(commitSHA, "ttWrongRLPFrontier/RLPArrayLengthWithFirstZeros.json");
    }

    @Test
    public void ttConstantinople() throws IOException, ParseException {
        suite.runAll("ttConstantinople");
    }

    @Test
    public void ttEip155VitaliksEip158() throws IOException, ParseException {
        suite.runAll("ttEip155VitaliksEip158");
    }

    @Test
    public void ttEip155VitaliksHomesead() throws IOException, ParseException {
        suite.runAll("ttEip155VitaliksHomesead");
    }

    @Test
    public void ttEip158() throws IOException, ParseException {
        suite.runAll("ttEip158");
    }

    @Test
    public void ttFrontier() throws IOException, ParseException {
        suite.run("ttFrontier", new HashSet<>(Arrays.asList(
                "String10MbData"    // too big to run it each time
        )));
    }

    @Test
    public void ttHomestead() throws IOException, ParseException {
        suite.run("ttHomestead", new HashSet<>(Arrays.asList(
                "String10MbData"    // too big to run it each time
        )));
    }

    @Test
    public void ttVRuleEip158() throws IOException, ParseException {
        suite.runAll("ttVRuleEip158");
    }

    @Test
    public void ttWrongRLPFrontier() throws IOException, ParseException {
        suite.runAll("ttWrongRLPFrontier");
    }

    @Test
    public void ttWrongRLPHomestead() throws IOException, ParseException {
        suite.runAll("ttWrongRLPHomestead");
    }
}
