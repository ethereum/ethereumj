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
    public String shacommit = "ca6dfa9c0155b46ea205ce4edc5178f5772d28e3";
    //public List<String> vmTestFiles = getFileNamesForTreeSha(shacommit);

    //@Ignore
    @Test
    public void runSingle() throws ParseException {
        String json = JSONReader.loadJSONFromCommit("VMTests/vmEnvironmentalInfoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, "extcodecopy0AddressTooBigRight");
    }

    //@Ignore
    @Test
    public void testArithmeticFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        // TODO: these are excluded due to bad wrapping behavior in ADDMOD/DataWord.add
        excluded.add("addmod1_overflowDiff");
        excluded.add("addmod1_overflow3");
        excluded.add("addmodBigIntCast");
        String json = JSONReader.loadJSONFromCommit("VMTests/vmArithmeticTest.json", shacommit);
        //String json = JSONReader.getTestBlobForTreeSha(shacommit, "vmArithmeticTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testBitwiseLogicOperationFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmBitwiseLogicOperationTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testBlockInfoFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmBlockInfoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testEnvironmentalInfoFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("env1");
        String json = JSONReader.loadJSONFromCommit("VMTests/vmEnvironmentalInfoTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testIOandFlowOperationsFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmIOandFlowOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore  //FIXME - 60M testfile
    @Test
    public void testvmInputLimitsTest1FromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmInputLimits1.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore //FIXME - 50M testfile
    @Test
    public void testvmInputLimitsTest2FromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmInputLimits2.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore //FIXME - 20M testfile
    @Test
    public void testvmInputLimitsLightTestFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmInputLimitsLight.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testVMLogGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmLogTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testPerformanceFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmPerformanceTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testPushDupSwapFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmPushDupSwapTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testShaFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmSha3Test.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testvmSystemOperationsTestGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBomb1");
        excluded.add("CallRecursiveBomb2");
        excluded.add("CallRecursiveBomb3");
        String json = JSONReader.loadJSONFromCommit("VMTests/vmSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    //@Ignore
    @Test // testing full suite
    public void testVMGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("VMTests/vmtests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore //FIXME
    @Test // testing full suite
    public void testRandomVMGitHub() throws ParseException {

        String sha = "c5eafb85390eee59b838a93ae31bc16a5fd4f7b1";
        List<String> fileNames = getFileNamesForTreeSha(sha);
        List<String> excludedFiles =
                Arrays.asList(
                        "" //Badly named file 
                );

        for (String fileName : fileNames) {

            if (excludedFiles.contains(fileName)) continue;
            System.out.println("Running: " + fileName);
            String json = JSONReader.loadJSON("VMTests//RandomTests/" + fileName);
            GitHubJSONTestSuite.runGitHubJsonVMTest(json);
        }

    }
}
