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
    public String shacommit = "ca0401b2fef08ac6e674d5151ad6b66fd88fa655";


    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, "mload32bitBound_Msize");
    }


    @Test
    public void stExample() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stCallCreateCallCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stInitCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("StackUnderFlowContractCreation");
        excluded.add("TransactionCreateRandomInitCode");
        excluded.add("TransactionCreateSuicideInInitcode");
        excluded.add("CallRecursiveContract");

        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stLogTests() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stPreCompiledContracts() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stMemoryStressTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("mload32bitBound_return2");
        excluded.add("mload32bitBound_return");
        excluded.add("mload32bitBound2");
        excluded.add("mload32bitBound");
        excluded.add("mload32bitBound_Msize");

        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stMemoryTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        excluded.add("stackLimitPush32_1025");
        excluded.add("stackLimitGas_1025");
        excluded.add("stackLimitPush31_1025");

        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stQuadraticComplexityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("Call50000_identity");
        excluded.add("Return50000");
        excluded.add("Call50000_rip160");
        excluded.add("Call50000_identity2");
        excluded.add("Return50000_2");
        excluded.add("Call1MB1024Calldepth");
        excluded.add("Call50000_ecrec");
        excluded.add("Callcode50000");
        excluded.add("Call50000");

        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stSolidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("TestBlockAndTransactionProperties");

        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stRecursiveCreate() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("recursiveCreate");

        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stRefundTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stSpecialTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }


    @Test
    public void stBlockHashTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json);
    }


    @Test
    public void stSystemOperationsTest() throws IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBomb0_OOG_atMaxCallDepth"); //TODO failing on cpp?
        excluded.add("Call10");
        excluded.add("createWithInvalidOpcode");

        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }



    @Test
    public void stTransactionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("OverflowGasRequire");    //FIXME wont work until we use gaslimit as long

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

}

