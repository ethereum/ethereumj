package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;

import org.json.simple.parser.ParseException;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    //Last known good commit: 5af1002b96f34cd2c9252c1a6636826d47411ccd
    public String shacommit = "5af1002b96f34cd2c9252c1a6636826d47411ccd";


    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", "ca0401b2fef08ac6e674d5151ad6b66fd88fa655");
        GitHubJSONTestSuite.runNewStateTest(json, "suicideOrigin");
    }

    @Test
    public void newTypeTest() throws IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("Call10");  // value overflow long
        excluded.add("CallRecursiveBomb0_OOG_atMaxCallDepth"); // value overflow long
        excluded.add("createWithInvalidOpcode");

        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", "ca0401b2fef08ac6e674d5151ad6b66fd88fa655");
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    //@Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void runWithExcludedTest() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stExample() throws ParseException {  // [V]
        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    //@Ignore
    @Test
    public void stCallCreateCallCodeTest() throws ParseException { // [V]
        Set<String> excluded = new HashSet<>();
        excluded.add("Callcode1024BalanceTooLow"); //FIXME block limits
        excluded.add("Call1024OOG"); //FIXME block limits
        excluded.add("Callcode1024OOG"); //FIXME block limits
        excluded.add("Call1024BalanceTooLow");
        excluded.add("CallLoseGasOOG"); //FIXME block limits
        excluded.add("CallcodeLoseGasOOG"); //FIXME block limits
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stInitCodeTest() throws ParseException { // [V]
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stLogTests() throws ParseException { // [V]
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stPreCompiledContracts() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    @Ignore //FIXME need to expand VM memory limit to pass these
    @Test
    public void stMemoryStressTest() throws ParseException { // [V]
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    //@Ignore
    @Test
    public void stMemoryTest() throws ParseException { // [V]
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        excluded.add("stackLimitPush32_1025");
        excluded.add("stackLimitGas_1025");
        excluded.add("stackLimitPush31_1025");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json,excluded);
    }

    //@Ignore
    @Test
    public void stQuadraticComplexityTest() throws ParseException { // [V]
        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    //@Ignore
    @Test
    public void stSolidityTest() throws ParseException { // [V]
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        excluded.add("TestBlockAndTransactionProperties");
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stRecursiveCreate() throws ParseException { // [V]
        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    //@Ignore
    @Test
    public void stRefundTest() throws ParseException { // [V]
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stSpecialTest() throws ParseException { // [V]

        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    //@Ignore
    @Test
    public void stBlockHashTest() throws ParseException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json);
    }

    //@Ignore
    @Test
    public void stSystemOperationsTest() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBomb0_OOG_atMaxCallDepth"); //TODO failing on cpp?
        excluded.add("Call10"); //FIXME need to support biginteger in Block class to pass this
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stTransactionTest() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("HighGasLimit");  //FIXME need to support biginteger in Block class to pass this
        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonStateTest(json, excluded);
    }

}

