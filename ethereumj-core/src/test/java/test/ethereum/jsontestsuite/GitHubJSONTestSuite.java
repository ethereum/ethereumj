package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.*;

import org.ethereum.jsontestsuite.runners.StateTestRunner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
@RunWith(Suite.class)
@SuiteClasses({
        GitHubVMTest.class,
})
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
                assertTrue(result.isEmpty());
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
            assertTrue(result.isEmpty());
        }
    }

    protected static void runGitHubJsonStateTest(String json, String testName) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        StateTestSuite testSuite = new StateTestSuite(testSuiteObj);

        for (StateTestCase testCase : testSuite.getAllTests()) {
            if (testCase.getName().equals(testName))
                logger.info(" => " + testCase.getName());
            else
                logger.info("    " + testCase.getName());
        }

        StateTestCase testCase = testSuite.getTestCase(testName);
        TestRunner runner = new TestRunner();
        List<String> result = runner.runTestCase(testCase);

        if (!result.isEmpty())
            for (String single : result)
                logger.info(single);

        assertTrue(result.isEmpty());
    }

    protected static void runGitHubJsonStateTest(String json, Set<String> excluded) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        StateTestSuite testSuite = new StateTestSuite(testSuiteObj);
        Collection<StateTestCase> testCollection = testSuite.getAllTests();

        for (StateTestCase testCase : testSuite.getAllTests()) {

            String prefix = "    ";
            if (excluded.contains(testCase.getName())) prefix = "[X] ";

            logger.info(prefix + testCase.getName());
        }

        for (StateTestCase testCase : testCollection) {

            if (excluded.contains(testCase.getName())) continue;
            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);

            if (!result.isEmpty())
                for (String single : result)
                    logger.info(single);

            assertTrue(result.isEmpty());
        }
    }


    protected static void runGitHubJsonStateTest(String json) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(json);

        StateTestSuite testSuite = new StateTestSuite(testSuiteObj);
        Collection<StateTestCase> testCollection = testSuite.getAllTests();


        for (StateTestCase testCase : testCollection) {

            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);

            if (!result.isEmpty())
                for (String single : result)
                    logger.info(single);

            assertTrue(result.isEmpty());
            logger.info(" *** Passed: " + testCase.getName());
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


    protected static void runGitHubJsonBlockTest(String json) throws ParseException, IOException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        BlockTestSuite testSuite = new BlockTestSuite(json);
        Set<String> testCollection = testSuite.getTestCases().keySet();

        for (String testName : testCollection) {
            runSingleBlockTest(testSuite, testName);
        }
    }

    private static void runSingleBlockTest(BlockTestSuite testSuite, String testName){

        BlockTestCase blockTestCase =  testSuite.getTestCases().get(testName);
        TestRunner runner = new TestRunner();

        logger.info("Running test: {}", testName);
        List<String> result = runner.runTestCase(blockTestCase);

        if (!result.isEmpty())
            for (String single : result)
                logger.info(single);

        assertTrue(result.isEmpty());
        logger.info(" *** Passed: " + testName);

    }


    public static void runNewStateTest(String jsonSuite) throws IOException {
        runNewStateTest(jsonSuite, new HashSet<String>());
    }


    public static void runNewStateTest(String jsonSuite, String testName) throws IOException {

        StateTestSuite2 stateTestSuite2 = new StateTestSuite2(jsonSuite);
        Map<String, StateTestCase2> testCases = stateTestSuite2.getTestCases();

        for (String testCase : testCases.keySet()) {
            if (testCase.equals(testName))
                logger.info("  => " + testCase);
            else
                logger.info("     " + testCase);
        }

        StateTestCase2 testCase = testCases.get(testName);
        if (testCase != null){
            String output = String.format("*  running: %s  *", testName);
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);
            StateTestRunner.run(testCases.get(testName));

        } else {
            logger.error("Sorry test case doesn't exist: {}", testName);
        }
    }

    public static void runNewStateTest(String jsonSuite, Set<String> excluded) throws IOException {

        StateTestSuite2 stateTestSuite2 = new StateTestSuite2(jsonSuite);
        Map<String, StateTestCase2> testCases = stateTestSuite2.getTestCases();
        Map<String, Boolean> summary = new HashMap<>();


        for (String testCase : testCases.keySet()) {
            if ( excluded.contains(testCase))
                logger.info(" [X] " + testCase);
            else
                logger.info("     " + testCase);
        }

        Set<String> testNames = stateTestSuite2.getTestCases().keySet();
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
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "PASS" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" Total: Pass: {}, Failed: {}", pass, fails);

        assertTrue(fails == 0);
    }


}
