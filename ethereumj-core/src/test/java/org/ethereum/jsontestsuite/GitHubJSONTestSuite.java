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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.*;
import org.ethereum.config.net.BaseNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.BlockHeader;
import org.ethereum.jsontestsuite.suite.*;
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

import static org.junit.Assert.assertEquals;

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

    public static void runGitHubJsonVMTest(String json) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestCase> testIterator = testSuite.iterator();

        while (testIterator.hasNext()) {

            TestCase testCase = testIterator.next();

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

    public static void runGitHubJsonTransactionTest(String json) throws IOException {

        TransactionTestSuite transactionTestSuite = new TransactionTestSuite(json);
        Map<String, TransactionTestCase> testCases = transactionTestSuite.getTestCases();
        Map<String, Boolean> summary = new HashMap<>();

        Set<String> testNames = transactionTestSuite.getTestCases().keySet();
        for (String testName : testNames){

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

    static void runDifficultyTest(BlockchainNetConfig config, String file, String commitSHA) throws IOException {

        String json = JSONReader.loadJSONFromCommit(file, commitSHA);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        SystemProperties.getDefault().setBlockchainConfig(config);

        try {
            for (DifficultyTestCase testCase : testSuite.getTestCases()) {

                logger.info("Running {}\n", testCase.getName());

                BlockHeader current = testCase.getCurrent();
                BlockHeader parent = testCase.getParent();

                assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty
                        (SystemProperties.getDefault().getBlockchainConfig(), parent));
            }
        } finally {
            SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
        }
    }

    static void runCryptoTest(String file, String commitSHA) throws IOException {
        String json = JSONReader.loadJSONFromCommit(file, commitSHA);

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, CryptoTestCase.class);


        HashMap<String , CryptoTestCase> testSuite =
                mapper.readValue(json, type);

        for (String key : testSuite.keySet()){
            logger.info("executing: " + key);
            testSuite.get(key).execute();
        }
    }

    static void runTrieTest(String file, String commitSHA, boolean secure) throws IOException {

        String json = JSONReader.loadJSONFromCommit(file, commitSHA);

        TrieTestSuite testSuite = new TrieTestSuite(json);

        for (TrieTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            String expectedRoot = testCase.getRoot();
            String actualRoot = testCase.calculateRoot(secure);

            assertEquals(expectedRoot, actualRoot);
        }
    }

    static void runABITest(String file, String commitSHA) throws IOException {

        String json = JSONReader.loadJSONFromCommit(file, commitSHA);

        ABITestSuite testSuite = new ABITestSuite(json);

        for (ABITestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            String expected = testCase.getResult();
            String actual = testCase.getEncoded();

            assertEquals(expected, actual);
        }
    }

    public enum Network {

        Frontier,
        Homestead,
        EIP150,
        EIP158,
        Byzantium,
        Constantinople,

        // Transition networks
        FrontierToHomesteadAt5,
        HomesteadToDaoAt5,
        HomesteadToEIP150At5,
        EIP158ToByzantiumAt5;

        public BlockchainNetConfig getConfig() {
            switch (this) {

                case Frontier:  return new FrontierConfig();
                case Homestead: return new HomesteadConfig();
                case EIP150:    return new Eip150HFConfig(new DaoHFConfig());
                case EIP158:    return new Eip160HFConfig(new DaoHFConfig());
                case Byzantium:    return new ByzantiumConfig(new DaoHFConfig());

                case FrontierToHomesteadAt5: return new BaseNetConfig() {{
                    add(0, new FrontierConfig());
                    add(5, new HomesteadConfig());
                }};

                case HomesteadToDaoAt5: return new BaseNetConfig() {{
                    add(0, new HomesteadConfig());
                    add(5, new DaoHFConfig(new HomesteadConfig(), 5));
                }};

                case HomesteadToEIP150At5: return new BaseNetConfig() {{
                    add(0, new HomesteadConfig());
                    add(5, new Eip150HFConfig(new HomesteadConfig()));
                }};

                case EIP158ToByzantiumAt5: return new BaseNetConfig() {{
                    add(0, new Eip160HFConfig(new HomesteadConfig()));
                    add(5, new ByzantiumConfig(new HomesteadConfig()));
                }};

                default: throw new IllegalArgumentException("Unknown network value: " + this.name());
            }
        }
    }
}
