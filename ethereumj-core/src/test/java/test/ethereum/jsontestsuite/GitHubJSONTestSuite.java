package test.ethereum.jsontestsuite;

import java.util.Iterator;
import java.util.List;

import org.ethereum.jsontestsuite.TestCase;
import org.ethereum.jsontestsuite.TestRunner;
import org.ethereum.jsontestsuite.TestSuite;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
    
    protected static void runGitHubJsonTest(String json) throws ParseException {
        Assume.assumeFalse("Online test is not available", json.equals(""));

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject)parser.parse(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestCase> testIterator = testSuite.iterator();

        while (testIterator.hasNext()){

            TestCase testCase = testIterator.next();
            
            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);
            Assert.assertTrue(result.isEmpty());
        }
    }
}
