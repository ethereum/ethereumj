package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ethereum.jsontestsuite.JSONReader.getFileNamesForTreeSha;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubVMTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "eecee75336681dc8c0b7a2423997178eb2101f4e";
    //public List<String> vmTestFiles = getFileNamesForTreeSha(shacommit);

    @Test
    public void runSingle() throws ParseException {
        String json = JSONReader.loadJSONFromCommit("VMTests/vmEnvironmentalInfoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, "balance0");
    }

    @Test
    @Ignore
    public void testArithmeticFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("addmod1_overflowDiff");
        excluded.add("addmod1_overflow3");
        String json = JSONReader.loadJSONFromCommit("VMTests/vmArithmeticTest.json", shacommit);
        //String json = JSONReader.getTestBlobForTreeSha(shacommit, "vmArithmeticTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testBitwiseLogicOperationFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmBitwiseLogicOperationTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }


    @Test // testing full suite
    public void testBlockInfoFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmBlockInfoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }


    @Test // testing full suite
    public void testEnvironmentalInfoFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("env1"); //Bug in test runner- this passes if VM logging is on "ALL"
        String json = JSONReader.loadJSONFromCommit("VMTests/vmEnvironmentalInfoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }


    @Test // testing full suite
    public void testIOandFlowOperationsFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmIOandFlowOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore
    @Test // testing random
    public void testvmInputLimitsTest1FromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmInputLimitsTest1.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testVMLogGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmLogTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testPushDupSwapFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmPushDupSwapTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testShaFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmSha3Test.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore
    @Test // testing full suite
    public void testvmSystemOperationsTestGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }


    @Test // testing full suite
    public void testVMGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmtests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testRandomVMGitHub() throws ParseException {

        String sha = "60b921af8bf7bbe38565f8543e52a54e5f465ae8";
        List<String> fileNames = getFileNamesForTreeSha(sha);
        List<String> excludedFiles =
                Arrays.asList(
                        "201501150842LARGE_DATA_IN_CALLCREATE_GOjson" //Badly named file
                );

        for (String fileName : fileNames) {

            if (excludedFiles.contains(fileName)) continue;
            System.out.println("Running: " + fileName);
            String json = JSONReader.loadJSON("VMTests//RandomTests/" + fileName);
            GitHubJSONTestSuite.runGitHubJsonVMTest(json);
        }

    }

}
