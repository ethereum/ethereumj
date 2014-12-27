package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;

import org.json.simple.parser.ParseException;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubVMTest {

    @Test
    public void testArithmeticFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmArithmeticTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testBitwiseLogicOperationFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmBitwiseLogicOperationTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testBlockInfoFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmBlockInfoTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testEnvironmentalInfoFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmEnvironmentalInfoTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testIOandFlowOperationsFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmIOandFlowOperationsTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testPushDupSwapFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmPushDupSwapTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testShaFromGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmSha3Test.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testVMGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmtests.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

    @Test // testing full suite
    public void testVMLogGitHub() throws ParseException {

        String json = JSONReader.loadJSON("VMTests/vmLogTest.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }
}
