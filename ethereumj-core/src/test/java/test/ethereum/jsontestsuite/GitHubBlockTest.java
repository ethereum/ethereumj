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
    public String shacommit = "ee7ea6a4beaf8b40d1ad49d14e2b82f6aac4d48a";

    @Ignore
    @Test
    public void runBCBlockChainTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcBlockChainTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }

    @Ignore
    @Test
    public void runBCInvalidHeaderTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcInvalidHeaderTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }

    @Test
    public void runBCUncleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }




}
