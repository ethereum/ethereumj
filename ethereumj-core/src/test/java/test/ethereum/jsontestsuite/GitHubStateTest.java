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
    public String shacommit = "049c4d356783eff8c9d3a4c8a1e158f72e512389";


    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, "mload32bitBound_Msize");
    }

    //@Ignore
    @Test
    public void stExample() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stCallCreateCallCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("createJS_ExampleContract");
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stInitCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stLogTests() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stPreCompiledContracts() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stMemoryStressTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("mload32bitBound_return2");
        excluded.add("mload32bitBound_return");
        excluded.add("mload32bitBound_Msize");// Falls on Cpp
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stMemoryTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stQuadraticComplexityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stSolidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("TestBlockAndTransactionProperties");
        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stRecursiveCreate() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stRefundTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stSpecialTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stBlockHashTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json);
    }

    //@Ignore
    @Test
    public void stSystemOperationsTest() throws IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBomb0_OOG_atMaxCallDepth"); //TODO failing on cpp?
        excluded.add("Call10");

        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    //@Ignore
    @Test
    public void stTransactionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("OverflowGasRequire");    //FIXME wont work until we use gaslimit as long
        excluded.add("EmptyTransaction2"); // Buggy testcase ?

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

}

