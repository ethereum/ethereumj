package org.ethereum.jsontestsuite;

import java.util.Iterator;
import java.util.List;

import org.ethereum.util.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Test file specific for tests maintained in the GitHub repository 
 * by the Ethereum DEV team. <br/>
 * 
 * @see <a href="https://github.com/ethereum/tests/">https://github.com/ethereum/tests/</a>
 */
public class GitHubJSONTestSuiteTest {

    @Test // testing full suite
    public void testArithmeticFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmArithmeticTest.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testBitwiseLogicOperationFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmBitwiseLogicOperationTest.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testBlockInfoFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmBlockInfoTest.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testEnvironmentalInfoFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmEnvironmentalInfoTest.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testIOandFlowOperationsFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmIOandFlowOperationsTest.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testPushDupSwapFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmPushDupSwapTest.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testShaFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmSha3Test.json");
        runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testSystemOperationsFromGitHub() throws ParseException {

        String json = Utils.getFromUrl("https://raw.githubusercontent.com/ethereum/tests/master/vmtests/vmSystemOperationsTest.json");
        runGitHubJsonTest(json);
    }
    
    private void runGitHubJsonTest(String json) throws ParseException {
        Assume.assumeFalse("Online test suite is no available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject)parser.parse(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestCase> testIterator = testSuite.iterator();

        while (testIterator.hasNext()){

            TestCase testCase = testIterator.next();

            System.out.println("Running: " + testCase.getName());
            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);
            Assert.assertTrue(result.isEmpty());
        }
    }
}
