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
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", "ca0401b2fef08ac6e674d5151ad6b66fd88fa655");
        GitHubJSONTestSuite.runNewStateTest(json, "suicideOrigin");
    }



    @Test
    public void stExample() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stCallCreateCallCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("Callcode1024BalanceTooLow"); //FIXME block limits
        excluded.add("Call1024OOG"); //FIXME block limits
        excluded.add("Callcode1024OOG"); //FIXME block limits
        excluded.add("Call1024BalanceTooLow");
        excluded.add("CallLoseGasOOG"); //FIXME block limits
        excluded.add("CallcodeLoseGasOOG"); //FIXME block limits
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stInitCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("StackUnderFlowContractCreation");
        excluded.add("TransactionCreateRandomInitCode");
        excluded.add("TransactionCreateSuicideInInitcode");
        excluded.add("CallRecursiveContract");

        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stLogTests() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stPreCompiledContracts() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallEcrecover0_BonusGas");
        excluded.add("CallRipemd160_4_gas99");
        excluded.add("CallRipemd160_3");
        excluded.add("CallRipemd160_4");
        excluded.add("CallSha256_5");
        excluded.add("CallRipemd160_2");
        excluded.add("CallEcrecover0_Gas2999");
        excluded.add("CallRipemd160_0");
        excluded.add("CallRipemd160_5");

        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }

    @Ignore //FIXME need to expand VM memory limit to pass these
    @Test
    public void stMemoryStressTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json);
    }


    @Test
    public void stMemoryTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        excluded.add("stackLimitPush32_1025");
        excluded.add("stackLimitGas_1025");
        excluded.add("stackLimitPush31_1025");
        excluded.add("stackLimitPush32_1024");
        excluded.add("stackLimitGas_1024");
        excluded.add("stackLimitPush31_1024");

        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }

    @Ignore
    @Test
    public void stQuadraticComplexityTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
        json = json.replaceAll("//", "data");

        GitHubJSONTestSuite.runNewStateTest(json);
    }


    @Test
    public void stSolidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("TestBlockAndTransactionProperties");
        excluded.add("CallInfiniteLoop");
        excluded.add("CallRecursiveMethods");

        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        json = json.replaceAll("//", "data");

        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stRecursiveCreate() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("recursiveCreate");

        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stRefundTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stSpecialTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("OverflowGasMakeMoney"); //TODO failing on cpp?

        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }


    @Test
    public void stBlockHashTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json);
    }


    @Test
    public void stSystemOperationsTest() throws IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBomb0_OOG_atMaxCallDepth"); //TODO failing on cpp?
        excluded.add("Call10"); //FIXME need to support biginteger in Block class to pass this
        excluded.add("createWithInvalidOpcode");

        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }



    @Test
    public void stTransactionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("HighGasLimit");    //FIXME need to support biginteger in Block class to pass this

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runNewStateTest(json, excluded);
    }

}

