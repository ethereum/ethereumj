package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "473f67fcb9f6d3551e4a2db82b84a66c19fe90d5";

    @Ignore
    @Test
    public void runSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonSingleBlockTest(json, "oneUncleGeneration7");
    }


    @Ignore
    @Test
    public void runBCBlockChainTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcInvalidHeaderTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }

    @Ignore
    @Test
    public void runBCInvalidRLPTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcInvalidRLPTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }

    @Ignore
    @Test
    public void runBCJSAPITest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcJS_API_Test.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }


    @Ignore
    @Test
    public void runBCUncleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }

    @Ignore
    @Test
    public void runBCValidBlockTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcValidBlockTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }



}
