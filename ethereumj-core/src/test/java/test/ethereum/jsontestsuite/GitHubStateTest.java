package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;

import org.json.simple.parser.ParseException;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashSet;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException {
        String json = JSONReader.loadJSON("StateTests/stSystemOperationsTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, "CallRecursiveBombLog2");
    }

    @Test // this method is mostly for hands-on convenient testing
    public void runWithExcludedTest() throws ParseException {

        Set<String> excluded = new HashSet<>();


        String json = JSONReader.loadJSON("StateTests/stPreCompiledContracts.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }


    @Test
    public void stExample() throws ParseException {  // [V]

        String json = JSONReader.loadJSON("StateTests/stExample.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test // todo: fix: excluded test
    public void stInitCodeTest() throws ParseException { // [V]

        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSON("StateTests/stInitCodeTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
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
    public void stSpecialTest() throws ParseException { // [V]

        String json = JSONReader.loadJSON("StateTests/stSpecialTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }


    @Test
    public void stBlockHashTest() throws ParseException {

        String json = JSONReader.loadJSON("StateTests/stBlockHashTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }


    @Test
    public void stSystemOperationsTest() throws ParseException {

        Set<String> excluded = new HashSet<>();
        excluded.add("createNameRegistratorZeroMem2");
        excluded.add("testVMtest");
        excluded.add("createWithInvalidOpcode");
        excluded.add("testRandomTest");


        String json = JSONReader.loadJSON("StateTests/stSystemOperationsTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Test // todo: fix: excluded test
    public void stTransactionTest() throws ParseException {

        Set<String> excluded = new HashSet<>();
        //todo:    it goes OOG, because no gasLimit is given. So it does not change the state.

        excluded.add("TransactionFromCoinbaseHittingBlockGasLimit1");

        String json = JSONReader.loadJSON("StateTests/stTransactionTest.json");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }


}

