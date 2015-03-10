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

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "b7021c7898ec1028405d70394c7ddf2445bfde6c";

    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, "CallRecursiveBombLog2");
    }

    @Test // this method is mostly for hands-on convenient testing
    public void runWithExcludedTest() throws ParseException {

        Set<String> excluded = new HashSet<>();
        excluded.add("CallRipemd160_5");
        excluded.add("CallSha256_5");
        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Test
    public void stExample() throws ParseException {  // [V]

        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test // todo: fix: excluded test
    public void stInitCodeTest() throws ParseException { // [V]

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Test
    public void stLogTests() throws ParseException { // [V]

        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stPreCompiledContracts() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallRipemd160_5");
        excluded.add("CallSha256_5");
        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Test
    public void stRecursiveCreate() throws ParseException { // [V]

        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stRefundTest() throws ParseException { // [V]
        Set<String> excluded = new HashSet<>();
        excluded.add("refund_CallA");
        excluded.add("refund_CallA2");
        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Test
    public void stSpecialTest() throws ParseException { // [V]

        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Test
    public void stBlockHashTest() throws ParseException {

        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    @Ignore //Input error (too large / badly formatted input)
    @Test
    public void stSystemOperationsTest() throws ParseException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Test // todo: fix: excluded test
    public void stTransactionTest() throws ParseException {

        Set<String> excluded = new HashSet<>();

        //TODO: This is going to stay excluded until we refactor 
        //      the codebase to use bigintegers instead of longs
        excluded.add("HighGasLimit");

        excluded.add("UserTransactionGasLimitIsTooLowWhenZeroCost");
        excluded.add("SuicidesAndInternlCallSuicides");
        excluded.add("SuicidesMixingCoinbase");
        excluded.add("CreateTransactionReverted");
        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

}

