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
import java.util.Arrays;
import java.util.List;

import static org.ethereum.jsontestsuite.JSONReader.getFileNamesForTreeSha;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "d2ba02fe0507da205e3d17d79612ae15282b35a2";


    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, "stackLimitPush32_1025");
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
        excluded.add("createJS_ExampleContract"); //FIXME Bug on CPP testrunner, storage/SSTORE
        excluded.add("Callcode1024OOG");
        excluded.add("Call1024OOG");
        excluded.add("callcodeWithHighValue");
        excluded.add("callWithHighValue");
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stInitCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
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
        excluded.add("mload32bitBound_return2");//FIXME memorySave must support long
        excluded.add("mload32bitBound_return"); //FIXME memorySave must support long
        excluded.add("mload32bitBound_Msize"); //FIXME memoryChunk must support long
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stMemoryTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Ignore
    @Test
    public void stQuadraticComplexityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stSolidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("TestBlockAndTransactionProperties"); //TODO proper BigInt block support needed
        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stRecursiveCreate() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
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

    //@Ignore
    @Test
    public void stSystemOperationsTest() throws IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBomb0_OOG_atMaxCallDepth"); //FIXME hitting VM limits
        excluded.add("Call10"); //FIXME gaslimit as biginteger
        excluded.add("createNameRegistratorZeroMem2"); // FIXME: Heap ???
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stTransactionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("OverflowGasRequire");    //FIXME wont work until we use gaslimit as long
        excluded.add("EmptyTransaction2"); // Buggy testcase
        excluded.add("TransactionSendingToEmpty");
        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test // testing full suite
    public void testRandomStateGitHub() throws ParseException, IOException {

        String sha = "99db6f4f5fea3aa5cfbe8436feba8e213d06d1e8";
        List<String> fileNames = getFileNamesForTreeSha(sha);
        List<String> includedFiles =
                Arrays.asList(
                        "st201504081841JAVA.json",
                        "st201504081842JAVA.json",
                        "st201504081843JAVA.json"
                );

        for (String fileName : fileNames) {
            if (includedFiles.contains(fileName)) {
              System.out.println("Running: " + fileName);
              String json = JSONReader.loadJSON("StateTests//RandomTests/" + fileName);
              GitHubJSONTestSuite.runStateTest(json);
            }
        }

    }
}

