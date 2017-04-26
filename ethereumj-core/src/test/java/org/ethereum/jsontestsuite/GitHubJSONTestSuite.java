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

import org.ethereum.jsontestsuite.suite.BlockTestCase;
import org.ethereum.jsontestsuite.suite.BlockTestSuite;
import org.ethereum.jsontestsuite.suite.StateTestCase;
import org.ethereum.jsontestsuite.suite.StateTestSuite;
import org.ethereum.jsontestsuite.suite.TestCase;
import org.ethereum.jsontestsuite.suite.TestRunner;
import org.ethereum.jsontestsuite.suite.TestSuite;
import org.ethereum.jsontestsuite.suite.TransactionTestCase;
import org.ethereum.jsontestsuite.suite.TransactionTestSuite;
import org.ethereum.jsontestsuite.suite.runners.StateTestRunner;
import org.ethereum.jsontestsuite.suite.runners.TransactionTestRunner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.junit.Assert;
import org.junit.Assume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Test file specific for tests maintained in the GitHub repository
 * by the Ethereum DEV team. <br/>
 *
 * @see <a href="https://github.com/ethereum/tests/">https://github.com/ethereum/tests/</a>
 */
public class GitHubJSONTestSuite {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");


    protected static void runGitHubJsonVMTest(String json, String testName) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestCase> testIterator = testSuite.iterator();

        for (TestCase testCase : testSuite.getAllTests()) {

            String prefix = "    ";
            if (testName.equals(testCase.getName())) prefix = " => ";

            logger.info(prefix + testCase.getName());
        }

        while (testIterator.hasNext()) {

            TestCase testCase = testIterator.next();
            if (testName.equals((testCase.getName()))) {
                TestRunner runner = new TestRunner();
                List<String> result = runner.runTestCase(testCase);
                Assert.assertTrue(result.isEmpty());
                return;
            }
        }
    }

    protected static void runGitHubJsonVMTest(String json) throws ParseException {
        Set<String> excluded = new HashSet<>();
        runGitHubJsonVMTest(json, excluded);
    }


    protected static void runGitHubJsonVMTest(String json, Set<String> excluded) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestCase> testIterator = testSuite.iterator();

        for (TestCase testCase : testSuite.getAllTests()) {

            String prefix = "    ";
            if (excluded.contains(testCase.getName())) prefix = "[X] ";

            logger.info(prefix + testCase.getName());
        }


        while (testIterator.hasNext()) {

            TestCase testCase = testIterator.next();
            if (excluded.contains(testCase.getName()))
                continue;

            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);
            try {
                Assert.assertTrue(result.isEmpty());
            } catch (AssertionError e) {
                System.out.println(String.format("Error on running testcase %s : %s", testCase.getName(), result.get(0)));
                throw e;
            }
        }
    }


    protected static void runGitHubJsonSingleBlockTest(String json, String testName) throws ParseException, IOException {

        BlockTestSuite testSuite = new BlockTestSuite(json);
        Set<String> testCollection = testSuite.getTestCases().keySet();

        for (String testCase : testCollection) {
            if (testCase.equals(testName))
                logger.info(" => " + testCase);
            else
                logger.info("    " + testCase);
        }

        runSingleBlockTest(testSuite, testName);
    }


    protected static void runGitHubJsonBlockTest(String json, Set<String> excluded) throws ParseException, IOException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        BlockTestSuite testSuite = new BlockTestSuite(json);
        Set<String> testCases = testSuite.getTestCases().keySet();
        Map<String, Boolean> summary = new HashMap<>();

        for (String testCase : testCases)
            if ( excluded.contains(testCase))
                logger.info(" [X] " + testCase);
            else
                logger.info("     " + testCase);


        for (String testName : testCases) {

            if ( excluded.contains(testName)) {
                logger.info(" Not running: " + testName);
                continue;
            }

            List<String> result = runSingleBlockTest(testSuite, testName);

            if (!result.isEmpty())
                summary.put(testName, false);
            else
                summary.put(testName, true);
        }


        logger.info("");
        logger.info("");
        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "OK" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" - Total: Pass: {}, Failed: {} - ", pass, fails);

        Assert.assertTrue(fails == 0);

    }

    protected static void runGitHubJsonBlockTest(String json) throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        runGitHubJsonBlockTest(json, excluded);
    }

    private static List<String> runSingleBlockTest(BlockTestSuite testSuite, String testName){

        BlockTestCase blockTestCase =  testSuite.getTestCases().get(testName);
        TestRunner runner = new TestRunner();

        logger.info("\n\n ***************** Running test: {} ***************************** \n\n", testName);
        List<String> result = runner.runTestCase(blockTestCase);

        logger.info("--------- POST Validation---------");
        if (!result.isEmpty())
            for (String single : result)
                logger.info(single);


        return result;
    }


    public static void runStateTest(String jsonSuite) throws IOException {
        runStateTest(jsonSuite, new HashSet<String>());
    }


    public static void runStateTest(String jsonSuite, String testName) throws IOException {

        StateTestSuite stateTestSuite = new StateTestSuite(jsonSuite);
        Map<String, StateTestCase> testCases = stateTestSuite.getTestCases();

        for (String testCase : testCases.keySet()) {
            if (testCase.equals(testName))
                logger.info("  => " + testCase);
            else
                logger.info("     " + testCase);
        }

        StateTestCase testCase = testCases.get(testName);
        if (testCase != null){
            String output = String.format("*  running: %s  *", testName);
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);
            List<String> fails = StateTestRunner.run(testCases.get(testName));

            Assert.assertTrue(fails.isEmpty());

        } else {
            logger.error("Sorry test case doesn't exist: {}", testName);
        }
    }

    public static void runStateTest(String jsonSuite, Set<String> excluded) throws IOException {

        StateTestSuite stateTestSuite = new StateTestSuite(jsonSuite);
        Map<String, StateTestCase> testCases = stateTestSuite.getTestCases();
        Map<String, Boolean> summary = new HashMap<>();


        for (String testCase : testCases.keySet()) {
            if ( excluded.contains(testCase))
                logger.info(" [X] " + testCase);
            else
                logger.info("     " + testCase);
        }

        Set<String> testNames = stateTestSuite.getTestCases().keySet();
        for (String testName : testNames){

            if (excluded.contains(testName)) continue;
            String output = String.format("*  running: %s  *", testName);
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);

            List<String> result = StateTestRunner.run(testCases.get(testName));
            if (!result.isEmpty())
                summary.put(testName, false);
            else
                summary.put(testName, true);
        }

        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "OK" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" - Total: Pass: {}, Failed: {} - ", pass, fails);

        Assert.assertTrue(fails == 0);
    }

    public static void runGitHubJsonTransactionTest(String json, Set<String> excluded) throws IOException, ParseException {

        TransactionTestSuite transactionTestSuite = new TransactionTestSuite(json);
        Map<String, TransactionTestCase> testCases = transactionTestSuite.getTestCases();
        Map<String, Boolean> summary = new HashMap<>();


        for (String testCase : testCases.keySet()) {
            if ( excluded.contains(testCase))
                logger.info(" [X] " + testCase);
            else
                logger.info("     " + testCase);
        }

        Set<String> testNames = transactionTestSuite.getTestCases().keySet();
        for (String testName : testNames){

            if (excluded.contains(testName)) continue;
            String output = String.format("*  running: %s  *", testName);
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);

            logger.info("==> Running test case: {}", testName);
            List<String> result = TransactionTestRunner.run(testCases.get(testName));
            if (!result.isEmpty())
                summary.put(testName, false);
            else
                summary.put(testName, true);
        }

        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "OK" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" - Total: Pass: {}, Failed: {} - ", pass, fails);

        Assert.assertTrue(fails == 0);
    }

}
