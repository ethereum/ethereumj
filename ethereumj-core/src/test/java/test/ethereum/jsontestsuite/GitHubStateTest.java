package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    @Test
    public void stSingleTest() throws ParseException {

    	String json = JSONReader.loadJSON("StateTests/stSystemOperationsTest.json");
    	GitHubJSONTestSuite.runGitHubJsonStateTest(json, "CallRecursiveBombLog");
    }

    @Test
    public void stExample() throws ParseException {

        String json = JSONReader.loadJSON("StateTests/stExample.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stInitCodeTest() throws ParseException { // [V]

        String json = JSONReader.loadJSON("StateTests/stInitCodeTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stLogTests() throws ParseException { // [V]

        String json = JSONReader.loadJSON("StateTests/stLogTests.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stPreCompiledContracts() throws ParseException {

        String json = JSONReader.loadJSON("StateTests/stPreCompiledContracts.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stRecursiveCreate() throws ParseException { // [V]

        String json = JSONReader.loadJSON("StateTests/stRecursiveCreate.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stRefundTest() throws ParseException { // [V]

        String json = JSONReader.loadJSON("StateTests/stRefundTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }


    @Test
    public void stSpecialTest() throws ParseException {

        String json = JSONReader.loadJSON("StateTests/stSpecialTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stSystemOperationsTest() throws ParseException {

        String json = JSONReader.loadJSON("StateTests/stSystemOperationsTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }


    @Test
    public void stTransactionTest() throws ParseException {

        String json = JSONReader.loadJSON("StateTests/stTransactionTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }



    
}

