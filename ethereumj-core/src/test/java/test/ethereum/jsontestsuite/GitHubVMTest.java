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

    //SHACOMMIT of VMTESTS TREE (not main tree)
    public String shacommit = "a713843af6e6274915bdbbc03d62dc5a0007548b";
    public List<String> vmTestFiles = getFileNamesForTreeSha(shacommit);

    @Test
    public void runSingle() throws ParseException {
        String json = JSONReader.loadJSON("VMTests/vmEnvironmentalInfoTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, "extcodecopy0AddressTooBigRight");
    }

    @Test
    public void testArithmeticFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        // TODO: these are excluded due to bad wrapping behavior in ADDMOD/DataWord.add
        excluded.add("addmod1_overflowDiff");
        excluded.add("addmod1_overflow3");
        String json = JSONReader.getTestBlobForTreeSha(shacommit, "vmArithmeticTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testBitwiseLogicOperationFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmBitwiseLogicOperationTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }


    @Test // testing full suite
    public void testBlockInfoFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmBlockInfoTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testEnvironmentalInfoFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        excluded.add("env1");
        String json = JSONReader.loadJSON("VMTests/vmEnvironmentalInfoTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }


    @Test // testing full suite
    public void testIOandFlowOperationsFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmIOandFlowOperationsTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Ignore
    @Test // testing random
    public void testvmInputLimitsTest1FromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmInputLimitsTest1.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testVMLogGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmLogTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testPushDupSwapFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmPushDupSwapTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testShaFromGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmSha3Test.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testvmSystemOperationsTestGitHub() throws ParseException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmSystemOperationsTest.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testVMGitHub() throws ParseException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSON("VMTests/vmtests.json");
        GitHubJSONTestSuite.runGitHubJsonVMTest(json, excluded);
    }

    @Test // testing full suite
    public void testRandomVMGitHub() throws ParseException {

        String sha = "f8aa9aa1f46995af1b07436db4fa528894914c60";
        List<String> fileNames = getFileNamesForTreeSha(sha);
        List<String> excludedFiles =
                Arrays.asList(
                        "201501150842LARGE_DATA_IN_CALLCREATE_GO.json"
                );

        for (String fileName : fileNames) {

            if (excludedFiles.contains(fileName)) continue;
            System.out.println("Running: " + fileName);
            String json = JSONReader.loadJSON("VMTests//RandomTests/" + fileName);
            GitHubJSONTestSuite.runGitHubJsonVMTest(json);
        }

    }
}
